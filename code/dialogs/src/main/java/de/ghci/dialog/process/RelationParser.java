package de.ghci.dialog.process;

import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;

/**
 * @author Dominik
 */
@Singleton
public class RelationParser {

    private Graphene graphene;

    public RelationParser() {
        Config config = ConfigFactory.load();
        graphene = new Graphene(config);
    }

    public ExContent parse(String input) {
        try {
            return graphene.doRelationExtraction(input, true);
        } catch (RuntimeException e) {
            if(e.getMessage().equals("The response contained no content.")) {
                System.err.println("The response contained no content. Input: " + input);
                return null;
            } else {
                throw e;
            }
        }
    }


    public String doCoreference(String input) {
        return graphene.doCoreference(input).getSubstitutedText().replaceAll("\\s+", " ");
    }

}
