package de.ghci.dialog;

import com.google.inject.AbstractModule;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.process.EventProcess;
import de.ghci.dialog.process.QuestionProcess;
import de.ghci.dialog.process.opinion.*;
import de.ghci.dialog.process.soccer.*;

/**
 * @author Dominik
 */
public class SoccerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DomainOpinionGenerator.class).to(SoccerStatementGenerator.class);
        bind(EntityClassifier.class).to(SoccerEntityClassifier.class);
        bind(DomainTextProvider.class).to(SoccerTextProvider.class);
        bind(AspectStateClassifier.class).to(SoccerAspectStateClassifier.class);
        bind(QuestionProcess.class).to(SoccerQuestionProcess.class);
        bind(DomainStatementParser.class).to(SoccerStatementParser.class);
        bind(EventProcess.class).to(SoccerEventProcess.class);
        bind(Information.class).to(SoccerInformation.class);
    }
}