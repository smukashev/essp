package com.bsbnb.creditregistry.portlets.protocol.data;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
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
}
