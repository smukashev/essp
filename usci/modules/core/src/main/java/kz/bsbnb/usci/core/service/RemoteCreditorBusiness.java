package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

public interface RemoteCreditorBusiness
{
    public List<Creditor> findMainOfficeCreditors();
}
