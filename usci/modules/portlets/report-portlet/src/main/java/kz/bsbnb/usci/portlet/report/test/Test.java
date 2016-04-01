package kz.bsbnb.usci.portlet.report.test;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.data.*;
import com.mockrunner.mock.jdbc.MockResultSet;

import java.util.List;
import  java.util.*;
import java.sql.ResultSet;
import java.util.List;
/**
 * Created by Bauyrzhan.Ibraimov on 15.06.2015.
 */
public class Test {
    Date repdate;
    Long creditorId;
    public  Test(Long creditorId, Date repdate)
    {
        this.repdate=repdate;
        this.creditorId=creditorId;
    }
    public ResultSet getData()  {
        List<String> headers = new ArrayList<String>();
        headers.add("File_name");
        headers.add("receiver_date");
        headers.add("protocol_type_description");
        headers.add("primary_contract_date");
        headers.add("protocol_type");
        headers.add("message_type");
        headers.add("message");
        headers.add("note");
        List<List<Object>> data = new ArrayList<List<Object>>();
        ResultSet rs=null;
        DataProvider provider = new BeanDataProvider();
        HashMap<Long, Creditor> inputCreditors = new HashMap<Long, Creditor>();
        List<Creditor> creditors = provider.getCreditorsList();
        for(Creditor cred : creditors) {
            inputCreditors.put(cred.getId(), cred);
        }
        Creditor currentCreditor = inputCreditors.get(creditorId);
        creditors.clear();
        creditors.add(currentCreditor);

        List<InputInfoDisplayBean> list  =provider.getInputInfosByCreditors(creditors, repdate);

        List<ProtocolDisplayBean> protlist;
        for(InputInfoDisplayBean info : list)
        {
            protlist = provider.getProtocolsByInputInfo(info);

            for(ProtocolDisplayBean protbean: protlist)
            {
                Protocol protocol = protbean.getProtocol();
                List<Object> Fields = new ArrayList<Object>();
                Fields.add(protocol.getInputInfo().getFileName());
                Fields.add(protocol.getInputInfo().getReceiverDate());
                Fields.add(protocol.getTypeDescription());
                Fields.add(protocol.getPrimaryContractDate());
                Fields.add(protocol.getProtocolType().getType());
                Fields.add(protocol.getMessageType().getNameRu());
                Fields.add(protocol.getMessage().getNameKz());
                Fields.add(protocol.getNote());
                data.add(Fields);
            }

        }
       try {
            rs = getResultSet(headers, data);
       }
       catch (Exception ex)
       {}
        return  rs;
    }

    public ResultSet getResultSet(List<String> headers, List<List<Object>> data) throws Exception {

        // validation
        if (headers == null || data == null) {
            throw new Exception(Errors.compose(Errors.E255));
        }

      //  if (headers.size() != data.size()) {
       //     throw new Exception("parameters size are not equals");
       // }

        // create a mock result set
        MockResultSet mockResultSet = new MockResultSet("myResultSet");

        // add header
        for (String string : headers) {
            mockResultSet.addColumn(string);
        }

        // add data
        for (List<Object> list : data) {
            mockResultSet.addRow(list);
        }

        return mockResultSet;
    }
}

