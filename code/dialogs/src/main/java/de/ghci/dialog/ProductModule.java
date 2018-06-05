package de.ghci.dialog;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ghci.dialog.gui.MainFrame;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.products.ProductsInformation;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.process.DialogProcess;
import de.ghci.dialog.process.EventProcess;
import de.ghci.dialog.process.QuestionProcess;
import de.ghci.dialog.process.SmallTalkProcess;
import de.ghci.dialog.process.opinion.*;
import de.ghci.dialog.process.products.*;
import de.ghci.dialog.process.soccer.*;
import org.lambda3.graphene.core.Graphene;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Dominik
 */
public class ProductModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DomainOpinionGenerator.class).to(ProductsStatementGenerator.class);
        bind(EntityClassifier.class).to(ProductsEntityClassifier.class);
        bind(DomainTextProvider.class).to(ProductsTextProvider.class);
        bind(AspectStateClassifier.class).to(ProductsAspectStateClassifier.class);
        bind(QuestionProcess.class).to(ProductsQuestionProcess.class);
        bind(DomainStatementParser.class).to(ProductsStatementParser.class);
        bind(EventProcess.class).to(ProductsEventProcess.class);
        bind(Information.class).to(ProductsInformation.class);
    }
}