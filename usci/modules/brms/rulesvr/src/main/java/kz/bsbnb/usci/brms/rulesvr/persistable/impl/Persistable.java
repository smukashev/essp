package kz.bsbnb.usci.brms.rulesvr.persistable.impl;

import kz.bsbnb.usci.brms.rulesvr.persistable.IPersistable;

/**
 * @author abukabayev
 */
public class Persistable implements IPersistable {

    protected long id = 0;

    protected Persistable(){
        super();
    }

    protected Persistable(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
}
