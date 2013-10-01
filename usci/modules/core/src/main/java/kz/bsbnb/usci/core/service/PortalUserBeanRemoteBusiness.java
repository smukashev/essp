package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;

import java.util.List;

public interface PortalUserBeanRemoteBusiness
{
    public boolean hasPortalUserCreditor(long userId, long creditorId);
    public void setPortalUserCreditors(long userId, long creditorId);
    public void unsetPortalUserCreditors(long userId, long creditorId);
    public List<Creditor> getPortalUserCreditorList(long userId);
    public void synchronize(List<PortalUser> users);
    public List<Creditor> getMainCreditorsInAlphabeticalOrder(long userId);
}
