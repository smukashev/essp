package kz.bsbnb.usci.brms.rulesvr.model.impl;

import kz.bsbnb.usci.brms.rulesvr.model.ISimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.persistable.impl.Persistable;

/**
 * Simple class to rerpesent [<b>id</b>, <b>name</b>] combination <br/>
 * User: Bauyrzhan.Makhambeto
 * Date: 21.01.14
 * Time: 12:42
 */
public class SimpleTrack extends Persistable implements ISimpleTrack {

    private static final long serialVersionUID = -2781296616323525651L;

     public String name;
    private boolean isActive;

    public SimpleTrack() {
    }

    public SimpleTrack(long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
