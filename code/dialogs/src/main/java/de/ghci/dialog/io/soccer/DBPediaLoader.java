package de.ghci.dialog.io.soccer;

import de.ghci.dialog.model.soccer.Person;
import de.ghci.dialog.model.soccer.Player;
import de.ghci.dialog.model.soccer.Referee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dominik
 */
public class DBPediaLoader {

    private static final String DBPEDIA_FOLDER = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\libraries\\dbpedia\\";
    private static final String PERSON_DATA_FILE = "persondata_en.ttl";
    private static final String INSTANCE_TYPES_FILE = "instance_types_en.ttl";

    private static final String SOCCER_PLAYER_LABEL = "<http://dbpedia.org/ontology/SoccerPlayer>";
    private static final String NAME_LABEL = "<http://xmlns.com/foaf/0.1/name>";
    private static final String GIVEN_NAME_LABEL = "<http://xmlns.com/foaf/0.1/givenName>";
    private static final String LAST_NAME_LABEL = "<http://xmlns.com/foaf/0.1/surname>";
    private static final String BIRTH_DATE_LABEL = "<http://dbpedia.org/ontology/birthDate>";
    private static final String BIRTH_PLACE_LABEL = "<http://dbpedia.org/ontology/birthPlace>";

    private static final String DESCRIPTION_LABEL = "<http://purl.org/dc/terms/description>";
    private static final String DESCRIPTION_SOCCER_REFEREE_1 = "football referee";
    private static final String DESCRIPTION_SOCCER_REFEREE_2 = "soccer referee";

    public static Collection<Person> getAllPersons() throws IOException {
        Map<String, Person> persons = new HashMap<>();
        persons.putAll(getPlayerIds());
        persons.putAll(getRefereeIds());
        fillPersonsWithInformation(persons);
        return persons.values();
    }

    public static Collection<Player> getAllSoccerPlayers() throws IOException {
        Map<String, Player> players = getPlayerIds();
        fillPersonsWithInformation(players);
        return players.values();
    }

    public static Collection<Referee> getAllReferees() throws IOException {
        Map<String, Referee> referees = getRefereeIds();
        fillPersonsWithInformation(referees);
        return referees.values();
    }

    private static <T extends Person> void fillPersonsWithInformation(Map<String, T> persons) {
        try (BufferedReader reader = new BufferedReader(new FileReader(DBPEDIA_FOLDER + PERSON_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ", 3);
                T person = persons.get(tokens[0]);

                if (person != null) {
                    setPersonProperty(tokens[1], tokens[2].replace(" .", ""), person);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setPersonProperty(String type, String value, Person person) {
        switch (type) {
            case DESCRIPTION_LABEL:
                person.setNationality(extractNationality(value));
                break;
            case NAME_LABEL:
                person.addNickName(extractName(value));
                break;
            case GIVEN_NAME_LABEL:
                person.setFirstName(extractName(value));
                break;
            case LAST_NAME_LABEL:
                person.setLastName(extractName(value));
                break;
            case BIRTH_DATE_LABEL:
                person.setBirthDate(extractDate(value));
                break;
            case BIRTH_PLACE_LABEL:
                person.setBirthPlaceIfNull(extractPlace(value));
                break;
        }
    }

    private static String extractNationality(String value) {
        return value.split(" ")[0];
    }

    private static String extractPlace(String value) {
        String trim = value.replace(">", "").replace("<http://dbpedia.org/resource/", "")
                .replace("_", " ").replace("-", " ");
        if (trim.contains("football")) {
            return null;
        } else {
            return trim;
        }
    }

    private static LocalDate extractDate(String value) {
        String trim = value.replace("\"", "").replace("^^<http://www.w3.org/2001/XMLSchema#date>", "");
        String[] tokens = trim.split("-");
        int month = Math.max(1, Integer.parseInt(tokens[1]));
        int dayOfMonth = Math.max(1, Integer.parseInt(tokens[2]));
        try {
            return LocalDate.of(Integer.parseInt(tokens[0]), month, dayOfMonth);
        } catch (DateTimeException e) {
            System.err.println("Parsing problem: " + e.getMessage());
            return null;
        }
    }

    private static String extractName(String token) {
        return token.replace("\"@en", "").replace("\"", "");
    }

    private static Map<String, Player> getPlayerIds() {
        Map<String, Player> players = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DBPEDIA_FOLDER + INSTANCE_TYPES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens.length > 2 && tokens[2].equals(SOCCER_PLAYER_LABEL)) {
                    players.put(tokens[0], new Player(tokens[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return players;
    }

    private static Map<String, Referee> getRefereeIds() {
        Map<String, Referee> referees = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DBPEDIA_FOLDER + PERSON_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ", 3);
                if (tokens.length > 2 && tokens[1].equals(DESCRIPTION_LABEL) &&
                        (tokens[2].contains(DESCRIPTION_SOCCER_REFEREE_1) ||
                                tokens[2].contains(DESCRIPTION_SOCCER_REFEREE_2))) {
                    referees.put(tokens[0], new Referee(tokens[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return referees;
    }


}
