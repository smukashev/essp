package kz.bsbnb.usci.portlet.report.export;

import com.mockrunner.mock.jdbc.MockResultSet;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.portlet.report.data.BeanDataProvider;
import kz.bsbnb.usci.portlet.report.data.DataProvider;
import kz.bsbnb.usci.portlet.report.data.InputInfoDisplayBean;
import kz.bsbnb.usci.portlet.report.data.ProtocolDisplayBean;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static kz.bsbnb.usci.portlet.report.ReportApplication.log;

/**
 * Created by Bauyrzhan.Ibraimov on 18.06.2015.
 */
public class ProtocolsForRepDateTableReportExporter {
    Date repDate;
    Long creditorId;
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public  ProtocolsForRepDateTableReportExporter(Long creditorId, Date repDate)
    {
        this.repDate=repDate;
        this.creditorId=creditorId;
    }

    public ResultSet getData()  {

        DataProvider provider = new BeanDataProvider();
        ResultSet rs=null;
        List<String> headers = getHeaders();
        List<List<Object>> TableData = new ArrayList<List<Object>>();
        HashMap<Long, Creditor> inputCreditors = new HashMap<Long, Creditor>();

        List<Creditor> creditors = provider.getCreditorsList();
        for(Creditor cred : creditors) {
            inputCreditors.put(cred.getId(), cred);
        }
        Creditor currentCreditor = inputCreditors.get(creditorId);
        creditors.clear();
        creditors.add(currentCreditor);

        List<InputInfoDisplayBean> list  =provider.getInputInfosByCreditors(creditors, repDate);
        List<ProtocolDisplayBean> ProtocolList;
        for(InputInfoDisplayBean info : list)
        {
            ProtocolList = provider.getProtocolsByInputInfo(info);

            for(ProtocolDisplayBean protocolBean: ProtocolList)
            {

                Protocol protocol = protocolBean.getProtocol();
                if (!protocol.getMessageType().getNameRu().equals("COMPLETED") && !protocol.getMessageType().getNameRu().equals("ERROR"))
                {
                    continue;
                }
                else {
                    List<Object> Fields = new ArrayList<Object>();
                    Fields.add(protocol.getInputInfo().getFileName().substring(protocol.getInputInfo().getFileName().lastIndexOf("/")+1));
                    Fields.add(dateFormat.format(protocol.getInputInfo().getReceiverDate()));
                    Fields.add(protocol.getTypeDescription());
                    Fields.add(protocol.getPrimaryContractDate());
                    Fields.add(protocol.getProtocolType().getType());
                    Fields.add(protocol.getMessageType().getNameRu());
                    Fields.add(protocol.getMessage().getNameKz());
                    Fields.add(protocol.getNote());
                    TableData.add(Fields);
                }
            }

        }
        try {
            rs = getResultSet(headers, TableData);
        }
        catch (Exception ex)
        {
            log.log(Level.WARNING, "MockResultSet error: {0}",ex.getStackTrace());
        }
        return  rs;
    }

    public List<String> getHeaders()
    {
        List<String> headers = new ArrayList<String>();
        headers.add("File_name");
        headers.add("receiver_date");
        headers.add("protocol_type_description");
        headers.add("primary_contract_date");
        headers.add("protocol_type");
        headers.add("message_type");
        headers.add("message");
        headers.add("note");

        return  headers;
    }

    public ResultSet getResultSet(List<String> headers, List<List<Object>> data) throws Exception {

        // validation
        if (headers == null || data == null) {
            throw new Exception("null parameters");
        }

        //  if (headers.size() != data.size()) {
        //     throw new Exception("parameters size are not equals");
        // }

        // create a mock result set
        MockResultSet mockResultSet = new MockResultSet("CouchResultSet");

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
