package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RemoteCreditorBusinessImpl implements RemoteCreditorBusiness
{
    @Override
    public List<Creditor> findMainOfficeCreditors()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
