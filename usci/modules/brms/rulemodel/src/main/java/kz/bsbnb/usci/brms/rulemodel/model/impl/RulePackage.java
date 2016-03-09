package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * @author abukabayev
 */

@Component
public class RulePackage extends Persistable
{
    private String name;
    private String description;


    public RulePackage(String name){
        this.name = name;
    }

    public RulePackage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
