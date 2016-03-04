package com.bsbnb.usci.portlets.protocol.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;
import kz.bsbnb.usci.eav.util.Errors;

import java.text.SimpleDateFormat;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class XmlProtocolExporter extends ProtocolExporter {

    public XmlProtocolExporter() {
        super();
    }

    @Override
    protected byte[] export() throws ExportException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("\n");
        sb.append("<protocol>");
        sb.append("\n");
        List<ProtocolDisplayBean> protocols = getProtocols();
        for (ProtocolDisplayBean protocol : protocols) {
            sb.append("<protocolRecord>").append("\n");
            sb.append("\t").append("<type>").append(protocol.getType().getCode()).append("</type>").append("\n");
            sb.append("\t").append("<level>").append(protocol.getMessageTypeCode()).append("</level>").append("\n");
            sb.append("\t").append("<no>").append(protocol.getDescription() == null ? "" : protocol.getDescription()).append("</no>").append("\n");
            String primaryContractDateString = protocol.getPrimaryContractDate()==null ? "" : sdf.format(protocol.getPrimaryContractDate());
            sb.append("\t").append("<contractdate>").append(primaryContractDateString).append("</contractdate>").append("\n");
            /*sb.append("\t").append("<messagecode>").append(protocol.getMessageCode()).append("</messagecode>").append("\n");*/
            sb.append("\t").append("<messagecode>").append(protocol.getMessageTypeString()).append("</messagecode>").append("\n");
            sb.append("\t").append("<message>").append(protocol.getMessage() == null ? "" : protocol.getMessage()).append("</message>").append("\n");
            sb.append("\t").append("<note>").append(protocol.getNote() == null ? "" : protocol.getNote()).append("</note>").append("\n");
            sb.append("</protocolRecord>").append("\n");
        }
        sb.append("</protocol>");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PrintStream ps = new PrintStream(baos, true, "UTF-8");
            ps.print(sb.toString());
            ps.close();
            return baos.toByteArray();
        } catch (UnsupportedEncodingException uee) {
            throw new ExportException(Errors.getMessage(Errors.E248, uee));
        }
    }

    @Override
    protected String getFileName() {
        String prefix = getFilenamePrefix();
        return (prefix==null ? "protocol" : prefix)+".xml";
    }

    @Override
    protected String getContentType() {
        return "application/xml";
    }

}
