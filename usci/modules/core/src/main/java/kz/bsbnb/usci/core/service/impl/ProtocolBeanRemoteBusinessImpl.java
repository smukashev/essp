package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness
{
    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId)
    {
        ArrayList<Protocol> list = new ArrayList<Protocol>();

        Protocol prot = new Protocol();
        prot.setId(1L);
        Message m = new Message();
        m.setCode("A");
        m.setNameKz("Akaz");
        m.setNameRu("Aru");
        prot.setMessage(m);
        Shared s = new Shared();
        s.setCode("S");
        s.setNameRu("Sru");
        s.setNameKz("Skz");
        prot.setMessageType(s);
        prot.setNote("note");
        prot.setPackNo(777L);
        prot.setPrimaryContractDate(new Date());
        prot.setProtocolType(s);
        prot.setTypeDescription("type desc");

        list.add(prot);

        System.out.println("Protocols count: " + list.size());

        return list;
    }
}

