package kz.bsbnb.usci.brms.rulesvr.model.impl;

import kz.bsbnb.usci.brms.rulesvr.model.IBatch;
import org.springframework.stereotype.Component;
import kz.bsbnb.usci.brms.rulesvr.persistable.impl.Persistable;
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

    public void setRepoDate(Date repoDate) {
        this.repDate = repoDate;
    }

    public Date getRepoDate() {
        return repDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
