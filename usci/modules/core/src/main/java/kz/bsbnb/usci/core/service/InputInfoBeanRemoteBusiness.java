package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;

import java.util.Date;
import java.util.List;

public interface InputInfoBeanRemoteBusiness {
    List<InputInfo> getAllInputInfos(List<Creditor> creditorsList, Date reportDate);
    List<InputInfo> getPendingBatches(List<Creditor> creditorsList);
}
