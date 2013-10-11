package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Shared;

import java.util.Date;
import java.util.List;

public interface InputInfoBeanRemoteBusiness
{
    public List<InputInfo> getAllInputInfosBy_Creditors_By_RepDateSortedBy_Id_Desc(List<Creditor> creditorsList,
                                                                                   Date reportDate);
    public InputInfo insert(long userId, Creditor creditor, String fileName, Date date, Shared webServiceLoadType,
                  Shared inQueueStatus);
}
