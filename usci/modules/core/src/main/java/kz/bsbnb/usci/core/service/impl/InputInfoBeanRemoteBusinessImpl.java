package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Shared;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InputInfoBeanRemoteBusinessImpl implements InputInfoBeanRemoteBusiness
{
    @Override
    public List<InputInfo> getAllInputInfosBy_Creditors_By_RepDateSortedBy_Id_Desc(List<Creditor> creditorsList, Date reportDate)
    {
        ArrayList<InputInfo> list = new ArrayList<InputInfo>();

        InputInfo ii = new InputInfo();
        ii.setId(BigInteger.ONE);
        ii.setUserId(1L);
        ii.setCompletionDate(new Date());
        ii.setCreditor(creditorsList.get(0));
        ii.setFileName("asd");
        ii.setReceiverDate(new Date());
        ii.setReportDate(new Date());
        ii.setStartedDate(new Date());
        ii.setTotal(10L);

        Shared s = new Shared();
        s.setCode("S");
        s.setNameRu("Sru");
        s.setNameKz("Skz");

        ii.setReceiverType(s);
        ii.setStatus(s);

        list.add(ii);

        return list;
    }
}
