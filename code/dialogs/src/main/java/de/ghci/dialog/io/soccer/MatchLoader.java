package de.ghci.dialog.io.soccer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.io.Resources;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.soccer.*;
import de.ghci.dialog.process.CoReferenceResolution;
import de.ghci.dialog.process.RelationParser;
import de.ghci.dialog.process.soccer.CardParser;
import de.ghci.dialog.process.soccer.ScoringParser;
import de.ghci.dialog.process.soccer.SoccerEntityClassifier;
import de.ghci.dialog.process.soccer.TeamsHelper;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;

import java.io.*;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dominik
 */
@Singleton
public class MatchLoader {

    private static final String NEWS_FOLDER = "matches";
    private static final String STORAGE_FILE_NAME = "matches.store";

    private TeamsHelper teamsHelper;
    private ScoringParser scoringParser;
    private CardParser cardParser;
    private CoReferenceResolution coReferenceResolution;
    private RelationParser relationParser;

    private File newsDirectory;
    private File storageFile;

    @Inject
    public MatchLoader(TeamsHelper teamsHelper, ScoringParser scoringParser, CardParser cardParser,
                       CoReferenceResolution coReferenceResolution, RelationParser relationParser) {
        this.teamsHelper = teamsHelper;
        this.scoringParser = scoringParser;
        this.cardParser = cardParser;
        this.coReferenceResolution = coReferenceResolution;
        this.relationParser = relationParser;
        newsDirectory = new File(Resources.getResourcesPath() + NEWS_FOLDER);
        storageFile = new File(Resources.getResourcesPath() + STORAGE_FILE_NAME);
    }

    public void parseMatches(SoccerInformation worldInformation) throws IOException, ClassNotFoundException {
        System.out.println("Parse new matches...");
        List<MatchInformation> matches = loadMatches();
        for(File file : newsDirectory.listFiles()) {
            if(file.isFile() && !matchAlreadyParsed(matches, file)) {
                matches.add(parseMatch(file, worldInformation));
            }
        }
        storeMatches(matches);
        System.out.println("Parsing done.");
    }

    public MatchInformation reloadMatch(String fileName, SoccerInformation worldInformation)
            throws IOException, ClassNotFoundException {
        List<MatchInformation> matches = loadMatches();
        List<MatchInformation> newMatches = matches.stream()
                .filter(mi -> !mi.getFileName().equals(fileName))
                .collect(Collectors.toList());
        MatchInformation mi = null;
        for(File file : new File(NEWS_FOLDER).listFiles()) {
            if(file.isFile() && file.getName().equals(fileName)) {
                mi = parseMatch(file, worldInformation);
                newMatches.add(mi);
            }
        }
        storeMatches(newMatches);
        return mi;
    }

    private boolean matchAlreadyParsed(List<MatchInformation> matches, File file) {
        for(MatchInformation match : matches) {
            if(match.getFileName().equals(file.getName())) {
                return true;
            }
        }
        return false;
    }

    private MatchInformation parseMatch(File file, SoccerInformation worldInformation) throws IOException {
        System.out.println("Parse: " + file.getName());
        String informationText = new String(Files.readAllBytes(file.toPath()));
        ExContent exContent = relationParser.parse(informationText);
        Set<Person> people = new SoccerEntityClassifier().getPersons(informationText);

        MatchInformation matchInformation = new MatchInformation(exContent, people,
                coReferenceResolution.doCoreference(informationText), worldInformation,
                extractDate(file.getName()), file.getName(), extractHomeTeam(file.getName()),
                extractAwayTeam(file.getName()), getEvents(informationText, worldInformation, exContent, people));
        worldInformation.addMatch(matchInformation);
        return matchInformation;
    }

    private List<Event> getEvents(String text, SoccerInformation worldInformation, ExContent exContent,
                                  Collection<Person> people) {
        List<Event> events = new ArrayList<>();
        addScoreEvents(text, worldInformation, exContent, people, events);
        addCardEvents(text, worldInformation, exContent, people, events);
        return events;
    }

    private List<ScoreEvent> addScoreEvents(String text, SoccerInformation worldInformation, ExContent exContent,
                                            Collection<Person> people, List<Event> events) {
        List<ScoreEvent> scores = scoringParser.getScores(text);
        for (ScoreEvent score : scores) {
            score.getPlayer().completeName(people);
            score.getPlayer().completeName(worldInformation.getPlayers());
            Player fromName = Person.getPersonFromName(worldInformation.getPlayers(), score.getPlayer().getName());
            if (fromName != null) {
                score.setPlayer(fromName);
                events.add(score);
            }
        }
        System.out.println("Scores: " + scores);
        return scores;
    }

    private List<CardEvent> addCardEvents(String text, SoccerInformation worldInformation, ExContent exContent,
                                          Collection<Person> people, List<Event> events) {
        List<CardEvent> cardEvents = cardParser.getCards(text, exContent, worldInformation);
        for (CardEvent cardEvent : cardEvents) {
            cardEvent.getPlayer().completeName(people);
            cardEvent.getPlayer().completeName(worldInformation.getPlayers());
            Player fromName = Person.getPersonFromName(worldInformation.getPlayers(), cardEvent.getPlayer().getName());
            if (fromName != null) {
                cardEvent.setPlayer(fromName);
                events.add(cardEvent);
            }
        }
        System.out.println("Cards: " + cardEvents);
        return cardEvents;
    }

    private Team getTeamFromName(String token) {
        try {
            return teamsHelper.getTeam(TeamLoader.loadTeams(), token);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Team extractHomeTeam(String fileName) {
        return getTeamFromName(fileName.split("_")[3]);
    }

    private Team extractAwayTeam(String fileName) {
        return getTeamFromName(fileName.split("_")[4]);
    }

    private LocalDate extractDate(String name) {
        String[] tokens = name.split("_");
        try {
            return LocalDate.of(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        } catch (DateTimeException e) {
            System.err.println("Parsing problem: " + e.getMessage());
            return LocalDate.now();
        }
    }

    private void storeMatches(List<MatchInformation> matches) throws IOException {
        FileOutputStream fout = new FileOutputStream(storageFile);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(matches);
    }

    public List<MatchInformation> loadMatches() throws IOException, ClassNotFoundException {
        try(FileInputStream fin = new FileInputStream(storageFile)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (List<MatchInformation>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

}
