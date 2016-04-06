package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IRule;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.io.Serializable;
import java.util.Date;

/**
 * @author abukabayev
 */
public class Rule extends Persistable implements IRule, Serializable {
    private static final long serialVersionUID = 1L;
    private String rule;
    private String title;
    private boolean isActive;
    protected Date openDate;

    public Rule(){

    }

    public Rule(String title, String rule){
        this.title = title;
        this.rule = rule;
    }

    public Rule(String title, String rule, Date openDate) {
        this.title = title;
        this.rule = rule;
        this.openDate = openDate;
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

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }
}
