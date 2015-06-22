/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.usci.portlets.protocol.data;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
//import com.bsbnb.creditregistry.dm.maintenance.Message;
//import com.bsbnb.creditregistry.dm.maintenance.Protocol;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.dm.ref.shared.MessageType;
import com.bsbnb.usci.portlets.protocol.data.DataProvider;
import com.bsbnb.usci.portlets.protocol.data.InputInfoDisplayBean;
import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;
import com.bsbnb.usci.portlets.protocol.data.SharedDisplayBean;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestDataProvider implements DataProvider {

    public List<Creditor> getCreditorsList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo) {
        List<ProtocolDisplayBean> result = new ArrayList<ProtocolDisplayBean>();
        Protocol protocol = new Protocol();
        protocol.setId(1);
        protocol.setInputInfo(null);
        Message message = new Message();
        message.setCode("CDG");
        message.setId(1);
        message.setNameKz("Name kz");
        message.setNameRu("Name ru");
        protocol.setMessage(message);
        Shared shared = new Shared();
        shared.setCode(MessageType.CRITICAL_ERROR.getCode());
        protocol.setMessageType(shared);
        protocol.setNote("Note");
        protocol.setProtocolType(shared);
        protocol.setTypeDescription("Credit");
        result.add(new ProtocolDisplayBean(protocol));
        return result;
    }

    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InputFile getFileByInputInfo(InputInfoDisplayBean inputInfo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
