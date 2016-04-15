package kz.bsbnb.usci.eav.rule;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class SimpleTrack extends Persistable {

    private static final long serialVersionUID = -2781296616323525651L;

    public String name;
    private boolean isActive;

    public SimpleTrack() {
        super();
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
