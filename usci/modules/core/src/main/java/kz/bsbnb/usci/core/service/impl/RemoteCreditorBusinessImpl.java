package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RemoteCreditorBusinessImpl implements RemoteCreditorBusiness
{
    @Override
    public List<Creditor> findMainOfficeCreditors()
    {
        Creditor creditor = new Creditor();

        creditor.setId(1);
        creditor.setName("Creditor1");
        creditor.setShortName("C1");
        creditor.setCode("CODE1");

        ArrayList<Creditor> list = new ArrayList<Creditor>();

        list.add(creditor);

        return list;
    }
}
