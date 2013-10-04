package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InputInfoBeanRemoteBusinessImpl implements InputInfoBeanRemoteBusiness
{
    @Override
    public List<InputInfo> getAllInputInfosBy_Creditors_By_RepDateSortedBy_Id_Desc(List<Creditor> creditorsList, Date reportDate)
    {
        return null;
    }
}
