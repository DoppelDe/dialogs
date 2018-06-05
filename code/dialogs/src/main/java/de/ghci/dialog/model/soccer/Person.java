package de.ghci.dialog.model.soccer;

import de.ghci.dialog.io.LocalDateAdapter;
import de.ghci.dialog.model.Entity;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Dominik
 */
public abstract class Person implements Entity {

    private static final long serialVersionUID = -1257296924848363517L;
    private String id;
    private String firstName;
    private String lastName;
    private String birthPlace;
    private Set<String> nickNames;
    private LocalDate birthDate;
    private String type;
    private String nationality;

    public Person() {
        nickNames = new HashSet<>();
        type = getClass().getSimpleName();
    }

    public Person(String id) {
        this();
        this.id = id;
    }

    public Person(String firstName, String lastName) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean onlyLastName() {
        return firstName == null && lastName != null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public Set<String> getPossibleNames() {
        HashSet<String> strings = new HashSet<>(getNickNames());
        strings.add(getLastName());
        return strings;
    }

    public Set<String> getNickNames() {
        HashSet<String> strings = new HashSet<>(nickNames);
        strings.add(getName());
        return strings;
    }

    public void addNickName(String name) {
        nickNames.add(name);
        if (firstName == null || lastName == null) {
            Person fromName = createFromName(name, nationality);
            if (lastName == null) {
                this.lastName = fromName.lastName;
            }
            if (firstName == null) {
                this.firstName = fromName.firstName;
            }
        }
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    public void setBirthPlaceIfNull(String birthPlace) {
        if (this.birthPlace == null) {
            this.birthPlace = birthPlace;
        }
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    public String getName() {
        if (!nickNames.isEmpty()) {
            return nickNames.iterator().next();
        }
        if (firstName == null) {
            return lastName;
        } else {
            return firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        if (lastName == null) {
            return id;
        } else {
            return getName();
        }
    }

    public <T extends Person> void completeName(Collection<T> people) {
        if (onlyLastName()) {
            for (Person person : people) {
                if (lastName.equals(person.getLastName()) && !person.onlyLastName()) {
                    firstName = person.getFirstName();
                }
            }
        }
    }

    public static Person createFromName(String name) {
        return createPlayerFromName(name);
    }

    protected static Player createPlayerFromName(String name) {
        String[] tokens = name.split(" ");
        String lastName = tokens[tokens.length - 1];

        String firstName = null;
        if (tokens.length > 1) {
            firstName = "";
            for (int i = 0; i < tokens.length - 1; i++) {
                firstName += tokens[i];
                if (i != tokens.length - 2) {
                    firstName += " ";
                }
            }
        }
        return new Player(firstName, lastName);
    }

    public static Person createFromName(String name, String nationality) {
        if (nationality != null && nationality.equals("Spanish")) {
            String[] tokens = name.split(" ");
            if (tokens.length < 3) {
                return createFromName(name);
            }
            String lastName = tokens[tokens.length - 1] + tokens[tokens.length - 2];

            String firstName = "";
            for (int i = 0; i < tokens.length - 2; i++) {
                firstName += tokens[i];
                if (i != tokens.length - 3) {
                    firstName += " ";
                }
            }
            return new Player(firstName, lastName);
        } else {
            return createFromName(name);
        }
    }

    public static <T extends Person> T getPersonFromName(Collection<T> persons, String name) {
        Person fromName = createFromName(name);
        T personFromName = getPersonFromName(persons, fromName.getFirstName(), fromName.getLastName());
        if (personFromName == null) {
            return getPersonFromNickname(persons, name);
        } else {
            return personFromName;
        }
    }

    private static <T extends Person> T getPersonFromNickname(Collection<T> persons, String name) {
        for (T person : persons) {
            for (String nickName : person.getNickNames()) {
                if (somehowMatches(name, nickName)) {
                    return person;
                }
            }
        }
        return null;
    }

    public static <T extends Person> T getPersonFromName(Collection<T> persons, String firstName, String lastName) {
        for (T person : persons) {
            if (Objects.equals(firstName, person.getFirstName()) && Objects.equals(lastName, person.getLastName())) {
                return person;
            }
        }
        for (T person : persons) {
            if (somehowMatches(firstName, person.getFirstName()) && somehowMatches(lastName, person.getLastName())) {
                return person;
            }
        }
        if (firstName == null) {
            List<T> possiblePersons = getPossiblePersonsByLastname(persons, lastName);
            if (possiblePersons.size() == 1) {
                return possiblePersons.get(0);
            }
        }
        return null;
    }

    private static <T extends Person> List<T> getPossiblePersonsByLastname(Collection<T> persons, String lastName) {
        List<T> possiblePersons = new ArrayList<>();
        for (T person : persons) {
            if (somehowMatches(lastName, person.getLastName())) {
                possiblePersons.add(person);
            }
        }
        return possiblePersons;
    }

    private static boolean somehowMatches(String keyword, String match) {
        if (match == null || keyword == null) {
            return false;
        } else {
            return simplify(match).equals(simplify(keyword));
        }
    }

    private static String simplify(String text) {
        return Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replace(".", "").replace(",","").replace(":","").replace(";","");
    }

    @Override
    public String getLabel() {
        return getName();
    }
}
