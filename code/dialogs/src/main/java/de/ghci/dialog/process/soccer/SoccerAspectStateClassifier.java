package de.ghci.dialog.process.soccer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import de.ghci.dialog.io.soccer.SoccerDataLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.soccer.MatchInformation;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.soccer.SoccerContext;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.model.soccer.*;
import de.ghci.dialog.model.soccer.TeamAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.ContainsMatcher;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.SentenceFinder;
import de.ghci.dialog.process.SentimentClassifier;
import de.ghci.dialog.process.opinion.AspectStateClassifier;
import org.nd4j.linalg.io.Assert;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Dominik
 */
public class SoccerAspectStateClassifier implements AspectStateClassifier {

    private static final int LAST_FEW_CONSTANT = 4;
    public static final HashSet<String> DAYS_OF_WEEK = new HashSet<>();
    private static final double TEAM_GOOD_THRESHOLD = 1.2;
    private static final double AVERAGE_GOOD_THRESHOLD = 1.3;
    public static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    static {
        DAYS_OF_WEEK.add("monday");
        DAYS_OF_WEEK.add("tuesday");
        DAYS_OF_WEEK.add("wednesday");
        DAYS_OF_WEEK.add("thursday");
        DAYS_OF_WEEK.add("friday");
        DAYS_OF_WEEK.add("saturday");
        DAYS_OF_WEEK.add("sunday");
    }

    @Inject
    private ContainsMatcher containsMatcher;
    @Inject
    private TeamsHelper teamsHelper;
    @Inject
    private SentimentClassifier sentimentClassifier;
    @Inject
    private SoccerDataLoader soccerDataLoader;
    @Inject
    private SentenceFinder sentenceFinder;

    @Override
    public AspectState getAspectState(Information information, Entity entity, OpinionAspect aspect,
                                      OpinionContext context) {
        Assert.isTrue(information instanceof SoccerInformation);
        Assert.isTrue(aspect instanceof TeamAspect || aspect instanceof PersonAspect);
        Assert.isTrue(context instanceof SoccerContext);

        if(entity instanceof Team) {
            return getAspectStateOfTeam(((SoccerInformation) information).getCompetitions(), (Team) entity,
                    (TeamAspect) aspect, (SoccerContext) context);
        } else if(entity instanceof Person) {
            return getAspectStateOfPlayer((SoccerInformation) information, (Person) entity, aspect,
                    (SoccerContext) context);
        }
        return null;
    }

    private AspectState getAspectStateOfTeam(List<Competition> competitions, Team team,
                                             TeamAspect aspect, SoccerContext context) {
        List<Fixture> fixtures = getAllFixtures(competitions);
        double overallAverage, teamAverage, teamContext;
        SoccerContext averageContext = SoccerContext.ALL_TIME;
        if (context == SoccerContext.MATCH || context == SoccerContext.MATCHES) {
            averageContext = SoccerContext.SEASON;
            averageContext.setInformation("" + (LocalDate.now().getYear() - 1));
        }
        switch (aspect) {
            case PERFORMANCE:
                overallAverage = getOverallAverage(fixtures,
                        Fixture::getHomePerformance, Fixture::getAwayPerformance);
                teamAverage = getTeamTimeAverage(competitions, team, averageContext,
                        Fixture::getHomePerformance, Fixture::getAwayPerformance);
                teamContext = getTeamTimeAverage(competitions, team, context,
                        Fixture::getHomePerformance, Fixture::getAwayPerformance);
                break;
            case WINS:
                overallAverage = getOverallAverage(fixtures,
                        Fixture::getHomeWins, Fixture::getAwayWins);
                teamAverage = getTeamTimeAverage(competitions, team, averageContext,
                        Fixture::getHomeWins, Fixture::getAwayWins);
                teamContext = getTeamTimeAverage(competitions, team, context,
                        Fixture::getHomeWins, Fixture::getAwayWins);
                break;
            case GOALS:
                overallAverage = getOverallAverage(fixtures,
                        Fixture::getFullTimeHomeGoals, Fixture::getFullTimeAwayGoals);
                teamAverage = getTeamTimeAverage(competitions, team, averageContext,
                        Fixture::getFullTimeHomeGoals, Fixture::getFullTimeAwayGoals);
                teamContext = getTeamTimeAverage(competitions, team, context,
                        Fixture::getFullTimeHomeGoals, Fixture::getFullTimeAwayGoals);
                break;
            case CHANCES:
                overallAverage = getOverallAverage(fixtures,
                        Fixture::getHomeChances, Fixture::getAwayChances);
                teamAverage = getTeamTimeAverage(competitions, team, averageContext,
                        Fixture::getHomeChances, Fixture::getAwayChances);
                teamContext = getTeamTimeAverage(competitions, team, context,
                        Fixture::getHomeChances, Fixture::getAwayChances);
                break;
            case FAIRNESS:
                overallAverage = 1 / getOverallAverage(fixtures,
                        Fixture::getHomeFairness, Fixture::getAwayFairness);
                teamAverage = 1 / getTeamTimeAverage(competitions, team, averageContext,
                        Fixture::getHomeFairness, Fixture::getAwayFairness);
                teamContext = 1 / getTeamTimeAverage(competitions, team, context,
                        Fixture::getHomeFairness, Fixture::getAwayFairness);
                break;
            case UNKNOWN:
            default:
                return AspectState.AVERAGE;
        }

        if (aspect != TeamAspect.FAIRNESS) {
            if (teamContext > teamAverage * TEAM_GOOD_THRESHOLD) {
                return AspectState.GOOD;
            } else if (teamContext * TEAM_GOOD_THRESHOLD < teamAverage) {
                return AspectState.BAD;
            }
        }

        if (teamContext > overallAverage * AVERAGE_GOOD_THRESHOLD) {
            return AspectState.GOOD;
        } else if (teamContext * AVERAGE_GOOD_THRESHOLD < overallAverage) {
            return AspectState.BAD;
        } else {
            return AspectState.AVERAGE;
        }
    }

    private AspectState getAspectStateOfPlayer(SoccerInformation information, Person player,
                                               OpinionAspect aspect, SoccerContext context) {
        List<MatchInformation> relevantMatches = getMatchesBasedOnContext(information, context, player);
        float scoreSum = 0.0f;
        float count = 0;
        for(MatchInformation mi : relevantMatches) {
            scoreSum += getPersonSentimentScore(player, mi);
            count++;
        }
        float average = scoreSum / count;
        return sentimentClassifier.getAspectStateOfSentimentScore(average);
    }

    private float getPersonSentimentScore(Person player, MatchInformation mi) {
        List<String> sentences = sentenceFinder.getPossibleSentences(mi.getExContent(),
                player.getLastName());
        return sentimentClassifier.getSentimentScoreOfSentences(sentences);
    }

    private List<Fixture> getAllFixtures(List<Competition> competitions) {
        List<Fixture> fixtures = new ArrayList<>();
        for (Competition competition : competitions) {
            fixtures.addAll(competition.getFixtures());
        }
        return fixtures;
    }

    private double getTeamTimeAverage(List<Competition> competitions, Team team, SoccerContext context,
                                             Function<Fixture, Integer> homeFunction,
                                             Function<Fixture, Integer> awayFunction) {
        List<Fixture> fixtures = getFixturesBasedOnContext(competitions, context, team);
        double aspect = 0;
        double games = 0;
        for (Fixture fixture : fixtures) {
            if (team.equals(fixture.getHomeTeam())) {
                aspect += homeFunction.apply(fixture);
                games++;
            } else if (team.equals(fixture.getAwayTeam())) {
                aspect += awayFunction.apply(fixture);
                games++;
            }
        }
        return aspect / games;
    }

    private List<Fixture> getFixturesBasedOnContext(List<Competition> competitions,
                                                    SoccerContext context, Team team) {
        switch (context) {
            case UNKNOWN:
            case ALL_TIME:
                return getAllFixtures(competitions);
            case SEASONS:
                return getLastSeasonsFixtures(competitions);
            case SEASON:
                return getSeasonFixtures(competitions, context);
            case COMPETITION:
                return getFixturesOfCompetition(competitions, context);
            case MATCHES:
                return getFixturesOfMatches(competitions, team);
            case MATCH:
                return getFixtureListOfMatch(competitions, context, team);
            default:
                throw new IllegalStateException("unreachable case");
        }
    }

    private List<Fixture> getFixturesOfMatches(List<Competition> competitions, Team team) {
        List<Fixture> teamFixtures = getTeamFixturesSorted(getAllFixtures(competitions), team);
        if (teamFixtures.size() <= LAST_FEW_CONSTANT) {
            return teamFixtures;
        }
        return teamFixtures.subList(0, LAST_FEW_CONSTANT);
    }

    private List<Fixture> getFixtureListOfMatch(List<Competition> competitions,
                                                SoccerContext context, Team team) {

        List<Fixture> teamFixtures = getTeamFixturesSorted(getAllFixtures(competitions), team);
        if (teamFixtures.size() <= 1) {
            return teamFixtures;
        } else {
            Fixture fixture = getFixtureOfMatch(context, teamFixtures);
            if (fixture.getMatchDate() != null) {
                context.setInformation("on " + fixture.getMatchDate()
                        .format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)));
            }
            return Collections.singletonList(fixture);
        }
    }

    private Fixture getFixtureOfMatch(SoccerContext context, List<Fixture> teamFixtures) {
        if (context.getInformation() != null && !context.getInformation().isEmpty()) {

            List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(context.getInformation());
            Fixture againstFixture = getFixtureOfMatchWithOpponent(teamFixtures, namedEntities);
            if (againstFixture != null) {
                return againstFixture;
            }

            Fixture dateFixture = getFixtureOfMatchWithDate(teamFixtures, context.getInformation());
            if (dateFixture != null) {
                return dateFixture;
            }
        }
        return teamFixtures.get(0);
    }

    private Fixture getFixtureOfMatchWithDate(List<Fixture> teamFixtures, String information) {
        LocalDate date = getDateOfInformation(information);
        if (date != null) {
            for (Fixture fixture : teamFixtures) {
                if (fixture.getMatchDate().isEqual(date)) {
                    return fixture;
                }
            }
        }
        return null;
    }

    private LocalDate getDateOfInformation(String information) {
        LocalDate date = null;
        if (containsMatcher.matches("day before yesterday", information)) {
            date = LocalDate.now().minusDays(2);
        } else if (containsMatcher.matches("yesterday", information)) {
            date = LocalDate.now().minusDays(1);
        } else if (containsMatcher.matches("today", information)) {
            date = LocalDate.now();
        } else if (containsMatcher.matches("day", information)) {
            date = parseDayOfWeek(extractOnlyDayText(information));
        } else {
            try {
                return LocalDate.parse(information, DATE_PATTERN);
            } catch (DateTimeParseException ignored) {
            }
        }
        return date;
    }

    private String extractOnlyDayText(String information) {
        for (String token : information.split(" ")) {
            if (DAYS_OF_WEEK.contains(token.toLowerCase())) {
                return token;
            }
        }
        return null;
    }

    private LocalDate parseDayOfWeek(String day) {
        if (day == null) {
            return null;
        }
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("E-ww-YYYY", Locale.ENGLISH);
        DateTimeFormatter weekYearFormat = DateTimeFormatter.ofPattern("-ww-YYYY", Locale.ENGLISH);
        String dayString = day.substring(0, 1).toUpperCase() + day.substring(1, 3).toLowerCase();
        LocalDate date = LocalDate.parse(dayString + LocalDate.now().format(weekYearFormat), dayFormat);
        if (date.isAfter(LocalDate.now().minusDays(1))) {
            date = date.minusWeeks(1);
        }
        return date;
    }

    private Fixture getFixtureOfMatchWithOpponent(List<Fixture> teamFixtures, List<NamedEntity> namedEntities) {
        try {
            for (NamedEntity ne : namedEntities) {
                Team t = teamsHelper.getTeam(soccerDataLoader.getAllTeams(), ne.getName());
                if (t != null) {
                    List<Fixture> againstFixtures = getTeamFixturesSorted(teamFixtures, t);
                    if (!againstFixtures.isEmpty()) {
                        return againstFixtures.get(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Fixture> getTeamFixturesSorted(List<Fixture> fixtures, Team team) {
        List<Fixture> teamFixtures = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            if (team.equals(fixture.getHomeTeam()) || team.equals(fixture.getAwayTeam())) {
                teamFixtures.add(fixture);
            }
        }
        teamFixtures.sort(Comparator.comparing(Fixture::getMatchDate));
        Collections.reverse(teamFixtures);
        return teamFixtures;
    }

    private List<Fixture> getLastSeasonsFixtures(List<Competition> competitions) {
        List<Fixture> fixtures = new ArrayList<>();
        for (Competition competition : competitions) {
            if (competition.getYear() > soccerDataLoader.CURRENT_YEAR - LAST_FEW_CONSTANT) {
                fixtures.addAll(competition.getFixtures());
            }
        }
        return fixtures;
    }

    private List<Fixture> getSeasonFixtures(List<Competition> competitions, SoccerContext context) {
        List<Fixture> fixtures = new ArrayList<>();
        int seasonYear = getSeasonYearFromContext(context);
        for (Competition competition : competitions) {
            if (competition.getYear() == seasonYear) {
                fixtures.addAll(competition.getFixtures());
            }
        }
        return fixtures;
    }

    private int getSeasonYearFromContext(SoccerContext context) {
        int seasonYear = soccerDataLoader.CURRENT_YEAR;
        for (int year = soccerDataLoader.DATA_START_YEAR; year <= soccerDataLoader.CURRENT_YEAR; year++) {
            if (context.getInformation() != null &&
                    context.getInformation().contains(String.format("%02d", year % 100))) {
                seasonYear = year;
                break;
            }
        }
        return seasonYear;
    }

    private List<Fixture> getFixturesOfCompetition(List<Competition> competitions,
                                                          SoccerContext context) {
        for (Competition competition : competitions) {
            if (containsMatcher.matches(competition.getLeague(), context.getInformation())) {
                return competition.getFixtures();
            }
        }
        competitions.sort((c1, c2) -> c2.getYear() - c1.getYear());
        return competitions.get(0).getFixtures();
    }

    private double getOverallAverage(List<Fixture> fixtures, Function<Fixture, Integer> homeFunction,
                                            Function<Fixture, Integer> awayFunction) {
        double aspect = 0;
        double games = 0;
        for (Fixture fixture : fixtures) {
            aspect += homeFunction.apply(fixture) + awayFunction.apply(fixture);
            games++;
        }
        return aspect / games / 2;
    }

    private double getSingleAverage(List<Fixture> fixtures, Function<Fixture, Integer> function) {
        double aspect = 0;
        double games = 0;
        for (Fixture fixture : fixtures) {
            aspect += function.apply(fixture);
            games++;
        }
        return aspect / games;
    }

    private List<MatchInformation> getMatchesBasedOnContext(SoccerInformation information,
                                                            SoccerContext context, Person player) {
        switch (context) {
            case ALL_TIME:
                return getAllMatches(information, player);
            case SEASONS:
                return getLastSeasonsMatches(information, player);
            case SEASON:
            case COMPETITION:
                return getSeasonMatches(information, player);
            case MATCHES:
                return getLastMatches(information, player);
            case UNKNOWN:
            case MATCH:
                return Lists.newArrayList(information.getCurrentMatch());
            default:
                throw new IllegalStateException("unreachable case");
        }
    }

    private List<MatchInformation> getAllMatches(SoccerInformation information, Person player) {
        List<MatchInformation> matches = new ArrayList<>();
        for (MatchInformation mi : information.getMatches()) {
            if (mi.getPlayers().contains(player)) {
                matches.add(mi);
            }
        }
        return matches;
    }

    private List<MatchInformation> getLastSeasonsMatches(SoccerInformation information, Person player) {
        List<MatchInformation> matches = new ArrayList<>();
        for (MatchInformation mi : information.getMatches()) {
            if (mi.getPlayers().contains(player) && mi.getMatchDate()
                    .isAfter(getCurrentSeasonStart().minusYears(LAST_FEW_CONSTANT))) {
                matches.add(mi);
            }
        }
        return matches;
    }

    private List<MatchInformation> getSeasonMatches(SoccerInformation information, Person player) {
        List<MatchInformation> matches = new ArrayList<>();
        for (MatchInformation mi : information.getMatches()) {
            if (mi.getPlayers().contains(player) && mi.getMatchDate().isAfter(getCurrentSeasonStart())) {
                matches.add(mi);
            }
        }
        return matches;
    }

    private LocalDate getCurrentSeasonStart() {
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 7, 1);
        if(startDate.isBefore(LocalDate.now())) {
            return startDate;
        } else {
            return LocalDate.of(LocalDate.now().getYear() - 1, 7, 1);
        }
    }

    private List<MatchInformation> getLastMatches(SoccerInformation information, Person player) {
        List<MatchInformation> allMatches = getAllMatches(information, player);
        allMatches.sort(Comparator.comparing(MatchInformation::getMatchDate));
        Collections.reverse(allMatches);
        if (allMatches.size() <= LAST_FEW_CONSTANT) {
            return allMatches;
        }
        return allMatches.subList(0, LAST_FEW_CONSTANT);
    }

}
