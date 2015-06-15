package kz.bsbnb.usci.brms.rulesvr.model.impl;

import kz.bsbnb.usci.brms.rulesvr.persistable.impl.Persistable;
import kz.bsbnb.usci.brms.rulesvr.model.IRule;

/**
 * @author abukabayev
 */
public class Rule extends Persistable implements IRule {
    private String rule;
    private String title;
    private boolean isActive;

    public Rule(){

    }

    public Rule(String rule){
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
