package com.bsbnb.usci.portlets.protocol.data;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
    public List<Creditor> getCreditorsList();
    public List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate);
    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo);
    //public Map<String,List<ProtocolDisplayBean>> getProtocolsByInputInfoGroupedByContractNo(InputInfoDisplayBean inputInfo);
    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo);
    public InputFile getFileByInputInfo(InputInfoDisplayBean inputInfo);
    public BatchFullJModel getBatchFullModel(BigInteger batchId);
}
