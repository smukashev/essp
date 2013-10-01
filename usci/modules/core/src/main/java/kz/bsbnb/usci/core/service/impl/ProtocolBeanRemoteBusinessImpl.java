package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness
{
    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId)
    {
        return null;
    }
}
