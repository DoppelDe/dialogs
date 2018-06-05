package de.ghci.dialog.process.products;

import com.google.common.base.Strings;
import de.ghci.dialog.model.products.CellPhoneAspect;
import de.ghci.dialog.model.products.PowerBankAspect;
import de.ghci.dialog.model.products.ProductAspect;
import de.ghci.dialog.model.products.ProductContext;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.opinion.DomainTextProvider;
import org.nd4j.linalg.io.Assert;

/**
 * @author Dominik
 */
public class ProductsTextProvider implements DomainTextProvider {

    @Override
    public String getAspectText(OpinionAspect aspect, OpinionContext context, AspectState state) {
        Assert.isTrue(aspect instanceof ProductAspect || aspect instanceof CellPhoneAspect || aspect instanceof PowerBankAspect);
        if(aspect instanceof ProductAspect) {
            return getProductAspectText((ProductAspect) aspect);
        } else if (aspect instanceof CellPhoneAspect){
            return getCellPhoneAspectText((CellPhoneAspect) aspect);
        } else if (aspect instanceof PowerBankAspect){
            return getPowerBankAspectText((PowerBankAspect) aspect);
        }
        throw new IllegalArgumentException();
    }

    private String getCellPhoneAspectText(CellPhoneAspect aspect) {
        AspectState state = aspect.getState();
        if (state == AspectState.GOOD) {
            switch (aspect) {
                case DISPLAY:
                    return "has a really good display";
                case BATTERY:
                    return "has a persevering battery";
                case PERFORMANCE:
                    return "is a really good cell phone";
                case PRICE:
                    return "is quite low-priced";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else if (state == AspectState.BAD) {
            switch (aspect) {
                case DISPLAY:
                    return "has a really bad display";
                case BATTERY:
                    return "has a bad battery life";
                case PERFORMANCE:
                    return "is a really bad cell phone";
                case PRICE:
                    return "is pretty expensive";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else {
            switch (aspect) {
                case DISPLAY:
                    return "has a decent display";
                case BATTERY:
                    return "has an average battery";
                case PERFORMANCE:
                    return "is an acceptable cell phone";
                case PRICE:
                    return "has a reasonable price";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        }
    }

    private String getPowerBankAspectText(PowerBankAspect aspect) {
        AspectState state = aspect.getState();
        if (state == AspectState.GOOD) {
            switch (aspect) {
                case POWER:
                    return "provides power for a long time";
                case PERFORMANCE:
                    return "is a good power bank overall";
                case PRICE:
                    return "is quite low-priced";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else if (state == AspectState.BAD) {
            switch (aspect) {
                case POWER:
                    return "is not very durable";
                case PERFORMANCE:
                    return "is a really bad power bank overall";
                case PRICE:
                    return "is pretty expensive";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else {
            switch (aspect) {
                case POWER:
                    return "provides an average amount of power";
                case PERFORMANCE:
                    return "is an acceptable cell phone";
                case PRICE:
                    return "has a reasonable price";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        }
    }

    private String getProductAspectText(ProductAspect aspect) {
        AspectState state = aspect.getState();
        if (state == AspectState.GOOD) {
            switch (aspect) {
                case PERFORMANCE:
                    return "is a really good product";
                case PRICE:
                    return "is quite low-priced";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else if (state == AspectState.BAD) {
            switch (aspect) {
                case PERFORMANCE:
                    return "is a really bad product";
                case PRICE:
                    return "is pretty expensive";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else {
            switch (aspect) {
                case PERFORMANCE:
                    return "is ok";
                case PRICE:
                    return "has a reasonable price";
                default:
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        }
    }

    @Override
    public String getContextText(OpinionContext context) {
        Assert.isTrue(context instanceof ProductContext);
        ProductContext productContext = (ProductContext) context;
        switch (productContext) {
            case SPECIFIC:
                return getInformationOr(productContext, "in this case");
            case GENERAL:
                return getInformationOr(productContext, "normally");
            case UNKNOWN:
                return getInformationOr(productContext, "");
            default:
                throw new IllegalStateException("unreachable case");
        }
    }

    private String getInformationOr(ProductContext context, String alternative) {
        if (!Strings.isNullOrEmpty(context.getInformation())) {
            return context.getInformation();
        } else {
            return alternative;
        }
    }
}
