package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.products.CellPhone;
import de.ghci.dialog.model.products.ProductsInformation;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.StanfordCache;
import de.ghci.dialog.process.opinion.EntityClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.nd4j.linalg.io.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dominik
 */
public class ProductsEntityClassifier implements EntityClassifier {

    @Inject
    private StanfordCache stanfordCache;
    @Inject
    private ProductsHelper productsHelper;

    @Override
    public Entity getEntity(Information information, String text) {
        Assert.isTrue(information instanceof ProductsInformation);
        if(text == null) {
            return null;
        }
        ProductsInformation productsInformation = (ProductsInformation) information;
        CellPhone product = productsHelper.getProduct(productsInformation.getCellPhones(), text);
        if(product != null) {
            return product;
        } else {
            return productsHelper.getProduct(productsInformation.getProducts().values(), text);
        }
    }

    @Override
    public Set<Entity> getEntities(Information information, String text) {
        Set<Entity> entities = new HashSet<>();
        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(text);
        for(NamedEntity namedEntity : namedEntities) {
            entities.add(getEntity(information, namedEntity.getName()));
        }
        return entities;
    }
}
