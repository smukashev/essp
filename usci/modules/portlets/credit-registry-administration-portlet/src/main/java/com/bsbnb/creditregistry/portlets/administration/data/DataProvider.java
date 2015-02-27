package com.bsbnb.creditregistry.portlets.administration.data;

import com.liferay.portal.model.User;
import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

//import com.bsbnb.creditregistry.dm.ref.Creditor;

/**
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
    public List<Creditor> getAllCreditors();

    public void addUserCreditor(User user, Creditor creditor);

    public void removeUserCreditor(User user, Creditor creditor);

    public List<Creditor> getUsersCreditors(User user);
}
