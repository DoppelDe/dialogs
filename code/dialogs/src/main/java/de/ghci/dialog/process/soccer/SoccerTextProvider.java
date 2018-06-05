package de.ghci.dialog.process.soccer;

import com.google.common.base.Strings;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.soccer.*;
import de.ghci.dialog.model.statement.AspectState;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.opinion.DomainTextProvider;
import org.nd4j.linalg.io.Assert;

/**
 * @author Dominik
 */
public class SoccerTextProvider implements DomainTextProvider {

    @Override
    public String getAspectText(OpinionAspect aspect, OpinionContext context, AspectState state) {
        Assert.isTrue(aspect instanceof TeamAspect || aspect instanceof PersonAspect);
        TeamAspect teamAspect;
        if(aspect instanceof TeamAspect) {
            teamAspect = (TeamAspect) aspect;
        } else {
            teamAspect = TeamAspect.PERFORMANCE;
        }
        String text = "";
        if (state == AspectState.GOOD) {
            switch (teamAspect) {
                case PERFORMANCE:
                    return "played good";
                case WINS:
                    if (context == SoccerContext.MATCH) {
                        return "won";
                    } else {
                        return "won a lot";
                    }
                case GOALS:
                    return "shot many goals";
                case CHANCES:
                    return "had a lot of chances";
                case FAIRNESS:
                    return "played very fair";
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else if (state == AspectState.BAD) {
            switch (teamAspect) {
                case PERFORMANCE:
                    return "played bad";
                case WINS:
                    if (context == SoccerContext.MATCH) {
                        return "did not win";
                    } else {
                        return "lost a lot";
                    }
                case GOALS:
                    return "didn't shoot many goals";
                case CHANCES:
                    return "had few chances";
                case FAIRNESS:
                    return "played very unfair";
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        } else {
            switch (teamAspect) {
                case PERFORMANCE:
                    return "played quite ok";
                case WINS:
                    return "won some games";
                case GOALS:
                    return "shot a few goals";
                case CHANCES:
                    return "had some chances";
                case FAIRNESS:
                    return "played quite fair";
                case UNKNOWN:
                    throw new IllegalArgumentException("can't create statement with unknown aspect");
            }
        }
        return text;
    }

    @Override
    public String getContextText(OpinionContext context) {
        Assert.isTrue(context instanceof SoccerContext);
        SoccerContext soccerContext = (SoccerContext) context;
        switch (soccerContext) {
            case UNKNOWN:
            case ALL_TIME:
                return getInformationOr(soccerContext, "");
            case SEASONS:
                return getInformationOr(soccerContext, "the last years");
            case SEASON:
                return getInformationOr(soccerContext, "this year");
            case COMPETITION:
                return getInformationOr(soccerContext, "in the Bundesliga this year");
            case MATCHES:
                return getInformationOr(soccerContext, "the last matches");
            case MATCH:
                return getInformationOr(soccerContext, "last game");
            default:
                throw new IllegalStateException("unreachable case");
        }
    }

    private String getInformationOr(SoccerContext context, String alternative) {
        if (!Strings.isNullOrEmpty(context.getInformation())) {
            return context.getInformation();
        } else {
            return alternative;
        }
    }
}
