package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;

import java.util.List;

public interface IUserDao {
    boolean hasPortalUserCreditor(long userId, long creditorId);

    void setPortalUserCreditors(long userId, long creditorId);

    void unsetPortalUserCreditors(long userId, long creditorId);

    List<Creditor> getPortalUserCreditorList(long userId);

    void synchronize(List<PortalUser> users);

    List<String> getAllowedClasses(long portalUserId);

    List<Long> getAllowedRefs(long portalUserId, String meta);

    PortalUser getUser(long userId);

    List<PortalUser> getPortalUsersHavingAccessToCreditor(Creditor creditor);
}
