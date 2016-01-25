package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IBatch;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * @author abukabayev
 */

@Component
public class Batch extends Persistable implements IBatch
{
    private Date repDate;
    private String name;
    private String description;


    public Batch(){

    }

    public Batch(String name,Date repDate){
        this.name = name;
        this.repDate = repDate;
    }

    public Batch(Date repoDate){
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
