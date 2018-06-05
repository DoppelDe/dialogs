package de.ghci.dialog.process.opinion;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.statement.*;
import de.ghci.dialog.process.*;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;

import java.util.List;

/**
 * @author Dominik
 */
public class StatementParser {

    private DomainStatementParser parser;
    private RelationParser relationParser;
    private EntityClassifier entityClassifier;

    @Inject
    public StatementParser(DomainStatementParser parser, RelationParser relationParser,
                           EntityClassifier entityClassifier) {
        this.parser = parser;
        this.relationParser = relationParser;
        this.entityClassifier = entityClassifier;
    }

    public OpinionStatement parse(String sentence, Information information) {

        String targetString = getTargetString(sentence);

        System.out.println("OS classify: " + sentence);
        if(targetString == null) {
            System.out.println("OS: null");
            return null;
        }
        Entity entity = entityClassifier.getEntity(information, targetString);

        OpinionAspect aspect = parser.getOpinionAspect(sentence, entity);
        OpinionContext context = parser.getOpinionContext(sentence, entity);

        OpinionStatement statement = new OpinionStatement(sentence, targetString, context, aspect);
        System.out.println("OS: " + statement);
        return statement;
    }

    private String getTargetString(String sentence) {
        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(sentence);
        if (namedEntities.size() == 0) {
            return null;
        } else if (namedEntities.size() == 1) {
            return namedEntities.get(0).getName();
        } else {
            return getTargetForMultipleEntities(sentence, namedEntities);
        }
    }

    private String getTargetForMultipleEntities(String sentence, List<NamedEntity> namedEntities) {
        ExContent exContent = relationParser.parse(sentence);
        if (exContent != null) {
            for (ExElement element : exContent.getElements()) {
                if (element.getSpo().isPresent()) {
                    List<NamedEntity> nes = NamedEntityClassifier
                            .getNamedEntities(element.getSpo().get().getSubject());
                    if (nes.size() >= 1) {
                        return nes.stream().map(NamedEntity::getName).reduce((s1, s2) -> s1 + " and " + s2).get();
                    }
                }
            }
        }
        return namedEntities.get(0).getName();
    }
}
