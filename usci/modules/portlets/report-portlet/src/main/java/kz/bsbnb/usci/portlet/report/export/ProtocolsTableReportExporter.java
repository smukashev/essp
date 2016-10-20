package kz.bsbnb.usci.portlet.report.export;

import com.liferay.portal.model.User;
import com.mockrunner.mock.jdbc.MockResultSet;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlet.report.data.BeanDataProvider;
import kz.bsbnb.usci.portlet.report.data.DataProvider;
import kz.bsbnb.usci.portlet.report.data.InputInfoDisplayBean;
import kz.bsbnb.usci.portlet.report.data.ProtocolDisplayBean;
import org.apache.log4j.Logger;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Bauyrzhan.Ibraimov on 18.06.2015.
 */
public class ProtocolsTableReportExporter {
    Date repDate;
    Date beginDate;
    Date endDate;
    Long creditorId;
    String RepName;

    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final Logger logger = Logger.getLogger(ProtocolsTableReportExporter.class);

    public ProtocolsTableReportExporter(List<Object> parameterList, String RepName)
    {
        this.RepName=RepName;
        Date[] dates = new Date[2];
        int i=0;
        for(int parameterIndex=0; parameterIndex < parameterList.size(); parameterIndex++)
        {

            Object parameter = parameterList.get(parameterIndex);
            if (parameter instanceof Date) {
                dates[i] = (Date) parameter;
                i++;
            }
            else
            {
                this.creditorId = Long.parseLong(parameter.toString());
            }

        }
        i=0;
        if(RepName.equals("ProtocolsByRepDate"))
        {
            this.repDate=dates[0];
        }
        else
        {
            if(dates[0].compareTo(dates[1])<0)
            {
                this.beginDate=dates[0];
                this.endDate=dates[1];
            }
            else
            {
                this.beginDate=dates[1];
                this.endDate=dates[0];
            }
        }

    }

    public ResultSet getData(User user)  {

        DataProvider provider = new BeanDataProvider();
        ResultSet rs=null;
        List<String> headers = getHeaders();
        List<List<Object>> TableData = new ArrayList<List<Object>>();
        HashMap<Long, Creditor> inputCreditors = new HashMap<Long, Creditor>();

        List<Creditor> creditors = provider.getCreditorsList(user);
        for(Creditor cred : creditors) {
            inputCreditors.put(cred.getId(), cred);
        }
        Creditor currentCreditor = inputCreditors.get(creditorId);
        creditors.clear();
        creditors.add(currentCreditor);

        List<InputInfoDisplayBean>   list  = provider.getInputInfosByCreditors(creditors, repDate);

        List<ProtocolDisplayBean> ProtocolList=null;
        for(InputInfoDisplayBean info : list)
        {
            if(RepName.equals("ProtocolsByRepDate")) {
                ProtocolList = provider.getProtocolsByInputInfo(info);
            }
            else
            {
                if(info.getReceiverDate().compareTo(beginDate)>=0 && info.getReceiverDate().compareTo(endDate)<=0)
                {
                    ProtocolList = provider.getProtocolsByInputInfo(info);
                }
            }
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
            logger.warn("MockResultSet error",ex);
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
            logger.error(Errors.getError(Errors.E255));
            throw new Exception(Errors.compose(Errors.E255));
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
