package com.bsbnb.usci.portlets.protocol.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TxtProtocolNumbersExporter extends ProtocolExporter {

    public TxtProtocolNumbersExporter() {
        super();
    }

    @Override
    protected byte[] export() throws ExportException {
        List<ProtocolDisplayBean> protocols = getProtocols();
        Set<String> uniqueNumbers = new HashSet<String>();
        for (ProtocolDisplayBean protocol : protocols) {
            if (protocol.isError()&&protocol.getDescription()!=null&&protocol.getDescription().length()>0) {
                uniqueNumbers.add(protocol.getDescription());
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PrintStream ps = new PrintStream(baos, true, "UTF-8");
            for (String number : uniqueNumbers) {
                ps.println(number);
            }
            ps.close();
            return baos.toByteArray();
        } catch (UnsupportedEncodingException uee) {
            throw new ExportException("Couldn't find encoding", uee);
        }
    }

    @Override
    protected String getFileName() {
        String prefix = getFilenamePrefix();
        return (prefix==null ? "protocol" : prefix)+".txt";
    }

    @Override
    protected String getContentType() {
        return "text/plain";
    }
}
