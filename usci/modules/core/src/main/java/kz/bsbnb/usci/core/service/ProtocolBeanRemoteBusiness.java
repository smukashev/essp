package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;

import java.util.List;

public interface ProtocolBeanRemoteBusiness
{
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId);
}
