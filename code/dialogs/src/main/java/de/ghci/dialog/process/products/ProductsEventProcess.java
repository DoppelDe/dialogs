package de.ghci.dialog.process.products;

import com.google.inject.Inject;
import de.ghci.dialog.io.products.ProductsLoader;
import de.ghci.dialog.model.Event;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.NamedEntity;
import de.ghci.dialog.model.products.FonoProduct;
import de.ghci.dialog.model.products.ReleaseEvent;
import de.ghci.dialog.process.EventProcess;
import de.ghci.dialog.process.NamedEntityClassifier;
import de.ghci.dialog.process.opinion.EntityClassifier;
import org.nd4j.linalg.api.ops.impl.accum.Prod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Dominik
 */
public class ProductsEventProcess implements EventProcess {

    private ProductsLoader productsLoader;
    private EntityClassifier entityClassifier;

    @Inject
    public ProductsEventProcess(ProductsLoader productsLoader, EntityClassifier entityClassifier) {
        this.productsLoader = productsLoader;
        this.entityClassifier = entityClassifier;
    }

    @Override
    public Optional<Event> getRelatedEvent(String text, List<Event> mentionedEvents, Information information) {
        List<NamedEntity> namedEntities = NamedEntityClassifier.getNamedEntities(text);
        List<FonoProduct> fonoProducts = new ArrayList<>();
        for(NamedEntity namedEntity : namedEntities) {
            fonoProducts.addAll(productsLoader.getFonoProducts(namedEntity.getName()));
        }
        Optional<FonoProduct> product = fonoProducts.stream()
                .filter(p -> LocalDate.now().isBefore(p.extractDate()))
                .findAny();
        if(product.isPresent()) {
            return Optional.of(createEventFromProduct(product.get()));
        } else {
            return fonoProducts.stream().findAny().map(this::createEventFromProduct);
        }
    }

    private Event createEventFromProduct(FonoProduct fonoProduct) {
        return new ReleaseEvent(fonoProduct);
    }
}
