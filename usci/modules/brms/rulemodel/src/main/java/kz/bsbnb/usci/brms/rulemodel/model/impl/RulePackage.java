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
    private Date repDate;
    private String name;
    private String description;


    public RulePackage(){

    }

    public RulePackage(String name, Date repDate){
        this.name = name;
        this.repDate = repDate;
    }

    public RulePackage(Date repoDate){
        this.repDate = repoDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepDate(Date repoDate) {
        this.repDate = repoDate;
    }

    public Date getRepDate() {
        return repDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
