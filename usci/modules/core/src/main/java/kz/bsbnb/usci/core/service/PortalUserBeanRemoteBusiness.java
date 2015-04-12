package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;

import java.util.List;

public interface PortalUserBeanRemoteBusiness {
    boolean hasPortalUserCreditor(long userId, long creditorId);
    void setPortalUserCreditors(long userId, long creditorId);
    void unsetPortalUserCreditors(long userId, long creditorId);
    List<Creditor> getPortalUserCreditorList(long userId);
    void synchronize(List<PortalUser> users);
    List<Creditor> getMainCreditorsInAlphabeticalOrder(long userId);
}
