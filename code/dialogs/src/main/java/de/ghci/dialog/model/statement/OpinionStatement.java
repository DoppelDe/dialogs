package de.ghci.dialog.model.statement;

import de.ghci.dialog.model.soccer.SoccerContext;

/**
 * @author Dominik
 */
public class OpinionStatement {

    private String text;
    private String target;
    private OpinionContext context;
    private OpinionAspect aspect;

    public OpinionStatement(String text, String target, OpinionContext context, OpinionAspect aspect) {
        this.text = text;
        this.target = target;
        this.context = context;
        this.aspect = aspect;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public OpinionContext getContext() {
        return context;
    }

    public void setContext(OpinionContext context) {
        this.context = context;
    }

    public OpinionAspect getAspect() {
        return aspect;
    }

    public void setAspect(OpinionAspect aspect) {
        this.aspect = aspect;
    }

    @Override
    public String toString() {
        return "OpinionStatement{" +
                "text='" + text + '\'' +
                ", target='" + target + '\'' +
                ", context=" + context +
                ", aspect=" + aspect +
                '}';
    }
}
