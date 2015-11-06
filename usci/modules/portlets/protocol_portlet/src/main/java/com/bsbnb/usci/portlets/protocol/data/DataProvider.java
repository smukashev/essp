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

    List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate);

    List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo);

    Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>>
        getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo);

    BatchFullJModel getBatchFullModel(BigInteger batchId);
}
