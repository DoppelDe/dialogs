package de.ghci.dialog.process.soccer;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.soccer.Person;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.model.soccer.Team;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.RelationParser;
import de.ghci.dialog.process.opinion.EntityClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.Triple;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;
import org.nd4j.linalg.io.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dominik
 */
public class SoccerEntityClassifier implements EntityClassifier {

    @Inject
    private TeamsHelper teamsHelper;

    @Override
    public Entity getEntity(Information information, String text) {
        Assert.isTrue(information instanceof SoccerInformation);
        if (text == null) {
            return null;
        }
        SoccerInformation soccerInformation = (SoccerInformation) information;
        Team team = teamsHelper.getTeam(soccerInformation.getTeams(), text);
        if (team != null) {
            return team;
        } else {
            return Person.getPersonFromName(soccerInformation.getPeople(), text);
        }
    }

    @Override
    public Set<Entity> getEntities(Information information, String text) {
        Set<Entity> entities = new HashSet<>();
        for (Entity entity : getPersons(text)) {
            Entity en = Person.getPersonFromName(((SoccerInformation) information).getPeople(), entity.getLabel());
            if (en != null) {
                entities.add(en);
            }
        }
        return entities;
    }

    public Set<Person> getPersons(String inputText) {
        Set<Person> people = new HashSet<>();
        List<Triple<String, Integer, Integer>> triples = CRFClassifier.getDefaultClassifier()
                .classifyToCharacterOffsets(inputText);
        for (Triple<String, Integer, Integer> triple : triples) {
            if (triple.first().equals("PERSON")) {
                people.add(Person.createFromName(inputText.substring(triple.second(), triple.third())));
            }
        }

        Set<Person> filteredPeople = new HashSet<>();
        for (Person person : people) {
            if (!person.onlyLastName() || !containsSamePlayer(people, person)) {
                filteredPeople.add(person);
            }
        }
        return filteredPeople;
    }

    private static boolean containsSamePlayer(Set<Person> people, Person onlyLastName) {
        for (Person person : people) {
            if (onlyLastName.getLastName().equals(person.getLastName()) && !person.onlyLastName()) {
                return true;
            }
        }
        return false;
    }

}
