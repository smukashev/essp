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
    List<Creditor> getCreditorsList();

    List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo);

    Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>>
        getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo);

    BatchFullJModel getBatchFullModel(BigInteger batchId);

    int countFiles(List<Creditor> selectedCreditors, Date date);

    List<InputInfoDisplayBean> loadFiles(List<Creditor> creditors, Date reportDate, int firstIndex, int count);

    int countProtocols(InputInfoDisplayBean inputInfoDisplayBean);

    List<ProtocolDisplayBean> loadProtocols(InputInfoDisplayBean inputInfo, int firstIndex, int count);
}
