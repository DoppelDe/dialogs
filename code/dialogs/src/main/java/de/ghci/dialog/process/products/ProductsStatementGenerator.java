package de.ghci.dialog.process.products;

import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.products.*;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.opinion.DomainOpinionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Dominik
 */

public class ProductsStatementGenerator implements DomainOpinionGenerator {

    @Override
    public OpinionAspect getRandomAspect(Entity entity) {
        if(entity instanceof CellPhone) {
            return getRandomElement(CellPhoneAspect.values(), 1);
        } else if (entity instanceof PowerBank){
            return getRandomElement(PowerBankAspect.values(), 1);
        } else if (entity instanceof Product){
            return getRandomElement(ProductAspect.values(), 1);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public OpinionContext getRandomContext(Entity entity) {
        return getRandomElement(ProductContext.values(), 1);
    }

    @Override
    public Entity getRandomEntity(Entity entity, Information information) {
        if(entity instanceof CellPhone) {
            return getRandomCellPhone((CellPhone) entity, (ProductsInformation) information);
        } else if(entity instanceof Product) {
            return getRandomProduct((Product) entity, (ProductsInformation) information);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private CellPhone getRandomCellPhone(CellPhone cellPhone, ProductsInformation information) {
        List<CellPhone> cellPhones = information.getCellPhones();
        CellPhone returnPhone = null;
        while(returnPhone == null || returnPhone.equals(cellPhone)) {
            returnPhone = cellPhones.get(new Random().nextInt(cellPhones.size()));
        }
        return returnPhone;
    }

    private Entity getRandomProduct(Product product, ProductsInformation information) {
        List<Entity> products = new ArrayList<>(information.getEntities());
        Entity returnProduct = null;
        while(returnProduct == null || returnProduct.equals(product)) {
            returnProduct = products.get(new Random().nextInt(products.size()));
        }
        return returnProduct;
    }

    private <T> T getRandomElement(T[] array, int ignoreLastElements) {
        return array[new Random().nextInt(array.length - ignoreLastElements)];
    }
}
