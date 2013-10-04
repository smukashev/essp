package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.List;

public interface IUserDao
{
    public boolean hasPortalUserCreditor(long userId, long creditorId);
    public void setPortalUserCreditors(long userId, long creditorId);
    public void unsetPortalUserCreditors(long userId, long creditorId);
    public List<Creditor> getPortalUserCreditorList(long userId);
    public void synchronize(List<PortalUser> users);
}
