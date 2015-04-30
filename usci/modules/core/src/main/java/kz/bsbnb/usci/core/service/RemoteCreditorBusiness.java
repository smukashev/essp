package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

public interface RemoteCreditorBusiness {
    List<Creditor> findMainOfficeCreditors();

    boolean creditorApproved(Creditor cred);

    int contractCount(Creditor cred);
}
