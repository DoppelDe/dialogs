package de.ghci.dialog.model.soccer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.io.soccer.MatchLoader;
import de.ghci.dialog.io.soccer.PersonLoader;
import de.ghci.dialog.io.soccer.SoccerDataLoader;
import de.ghci.dialog.io.soccer.TeamLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;

import java.io.IOException;
import java.util.*;

/**
 * @author Dominik
 */
@Singleton
public class SoccerInformation implements Information {

    private static final long serialVersionUID = 6089588793534225221L;

    public static final String MAIN_FILE = "liverpool_roma";

    private List<MatchInformation> matches;
    private Collection<Player> players;
    private Collection<Referee> referees;
    private MatchInformation currentMatch;
    private List<Competition> competitions;
    private Set<Team> teams;
    private List<Team> handledTeams;
    private Team askedTarget;

    @Inject
    public SoccerInformation(SoccerDataLoader soccerDataLoader, MatchLoader matchLoader)
            throws IOException, ClassNotFoundException {
        handledTeams = new ArrayList<>();
        loadCompetitionsAndTeams(soccerDataLoader);
        loadPersons();
        loadMatches(matchLoader, MAIN_FILE);
    }

    private void loadCompetitionsAndTeams(SoccerDataLoader soccerDataLoader) {
        if (competitions == null) {
            try {
                competitions = soccerDataLoader.getAllBundesligaData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (teams == null) {
            teams = new HashSet<>();
            try {
                teams.addAll(TeamLoader.loadTeams());
            } catch (IOException e) {
                if (competitions != null) {
                    for (Competition competition : competitions) {
                        teams.addAll(competition.getTeams());
                    }
                } else {
                    teams = null;
                }
                e.printStackTrace();
            }
        }
    }

    private void loadPersons() throws IOException {
        Collection<Person> persons = PersonLoader.loadPersons();
        players = new HashSet<>();
        referees = new HashSet<>();
        for(Person person : persons) {
            if(person instanceof Player) {
                players.add((Player) person);
            } else if (person instanceof Referee) {
                referees.add((Referee) person);
            }
        }
    }

    private void loadMatches(MatchLoader matchLoader, String mainFile) throws IOException, ClassNotFoundException {
        matches = matchLoader.loadMatches();
        matchLoader.parseMatches(this);

        MatchInformation matchInformation = findMatch(this, mainFile);
//        matchInformation = matchLoader.reloadMatch(matchInformation.getFileName(), worldInformation);
        currentMatch = matchInformation;
    }

    private static MatchInformation findMatch(SoccerInformation worldInformation, String fileName) {
        for(MatchInformation mi : worldInformation.getMatches()) {
            if(mi.getFileName().contains(fileName)) {
                System.out.println("Talking about: " + mi.getFileName());
                return mi;
            }
        }
        return null;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public Collection<Referee> getReferees() {
        return referees;
    }

    public Collection<Person> getPeople() {
        HashSet<Person> people = new HashSet<>();
        people.addAll(players);
        people.addAll(referees);
        return people;
    }

    public List<MatchInformation> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchInformation> matches) {
        this.matches = matches;
    }

    public List<Competition> getCompetitions() {
        return competitions;
    }

    public void setCurrentMatch(MatchInformation currentMatch) {
        this.currentMatch = currentMatch;
    }

    public MatchInformation getCurrentMatch() {
        return currentMatch;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public List<Team> getHandledTeams() {
        return handledTeams;
    }

    public void addHandledTeam(Team team) {
        handledTeams.add(team);
    }

    public void addMatch(MatchInformation match) {
        matches.add(match);
    }

    @Override
    public Collection<Event> getEvents() {
        return currentMatch.getEvents();
    }

    @Override
    public Collection<Entity> getEntities() {
        ArrayList<Entity> entities = new ArrayList<>();
        entities.addAll(currentMatch.getPeople());
        return entities;
    }

    public Team getAskedTarget() {
        return askedTarget;
    }

    public void setAskedTarget(Team askedTarget) {
        this.askedTarget = askedTarget;
    }
}
