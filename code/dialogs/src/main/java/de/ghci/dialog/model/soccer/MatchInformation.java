package de.ghci.dialog.model.soccer;

import de.ghci.dialog.model.Event;
import de.ghci.dialog.process.soccer.CardParser;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.soccer.ScoringParser;
import de.ghci.dialog.process.soccer.SoccerEntityClassifier;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Dominik
 */
public class MatchInformation implements Serializable {

    private static final long serialVersionUID = -5184447761660829640L;

    private transient ExContent exContent;
    private String text;
    private Set<Person> people;
    private Set<Player> players;
    private Referee referee;
    private List<Event> events;
    private LocalDate matchDate;
    private String fileName;
    private Team homeTeam;
    private Team awayTeam;

    public MatchInformation(ExContent exContent, Set<Person> people, String text, SoccerInformation worldInformation,
                            LocalDate matchDate, String fileName, Team homeTeam, Team awayTeam,
                            List<Event> events) {
        this.exContent = exContent;
        this.text = text;
        this.people = people;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.fileName = fileName;
        determinePlayers(worldInformation);
        determineReferee(worldInformation);
        this.events = events;
    }

    private void determinePlayers(SoccerInformation worldInformation) {
        players = new HashSet<>();
        validate(worldInformation, people, players);
    }

    private <T extends Person> void validate(SoccerInformation worldInformation, Collection<T> oldPeople,
                                             Collection<Player> newPeople) {
        for (Person person : oldPeople) {
            person.completeName(people);
            person.completeName(worldInformation.getPlayers());
            Player fromName = Person.getPersonFromName(worldInformation.getPlayers(), person.getName());
            if (fromName != null) {
                newPeople.add(fromName);
            }
        }
    }

    public ExContent getExContent() {
        return exContent;
    }

    public String getText() {
        return text;
    }

    public Set<Person> getPeople() {
        return people;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public List<Person> getScorers() {
        List<Person> scorers = new ArrayList<>();
        for(Event event : events) {
            if(event instanceof ScoreEvent) {
                scorers.add(((ScoreEvent) event).getPlayer());
            }
        }
        return scorers;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Referee getReferee() {
        return referee;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public String getMatchDatePrintable() {
        return matchDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public void determineReferee(SoccerInformation worldInformation) {
        for (Person person : people) {
            Referee fromName = Person.getPersonFromName(worldInformation.getReferees(),
                    person.getFirstName(), person.getLastName());
            if(fromName != null) {
                referee = fromName;
                break;
            }
        }
    }

    public boolean didParticipate(Person player) {
        return people.contains(player);
    }

    @Override
    public String toString() {
        return "MatchInformation{" +
                "fileName=" + fileName +
                ", players=" + players +
                ", referee=" + referee +
                ", events=" + events +
                ", matchDate=" + matchDate +
                ", people='" + people + '\'' +
                '}';
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(exContent.serializeToJSON());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String json = (String) in.readObject();
        exContent = ExContent.deserializeFromJSON(json, ExContent.class);
    }

}
