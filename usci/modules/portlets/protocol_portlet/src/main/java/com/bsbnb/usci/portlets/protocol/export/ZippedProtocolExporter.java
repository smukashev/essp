/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.usci.portlets.protocol.export;

import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;
import com.vaadin.Application;
import kz.bsbnb.usci.eav.util.Errors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ZippedProtocolExporter extends ProtocolExporter {

    private ProtocolExporter unzippedExporter;

    public ZippedProtocolExporter(ProtocolExporter innerExporter) {
        unzippedExporter = innerExporter;
    }

    @Override
    protected String getFileName() {
        String prefix = getFilenamePrefix();
        return (prefix == null ? "protocol" : prefix+"_protocol") + ".zip";
    }

    @Override
    protected String getContentType() {
        return "application/zip";
    }

    @Override
    protected byte[] export() throws ExportException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            zos.putNextEntry(new ZipEntry(unzippedExporter.getFileName()));
            zos.write(unzippedExporter.export());
            zos.closeEntry();
        } catch (IOException ioe) {
            throw new ExportException(Errors.getMessage(Errors.E252, ioe));
        } finally {
            try {
                zos.close();
            } catch (Exception e) {
            }
        }

        return baos.toByteArray();
    }
    
    @Override
    public void setProtocols(List<ProtocolDisplayBean> protocols) {
        super.setProtocols(protocols);
        unzippedExporter.setProtocols(protocols);
    }
    
    @Override
    public void setApplication(Application application) {
        super.setApplication(application);
        unzippedExporter.setApplication(application);
    }
    
    @Override
    public void setFilenamePrefix(String filenamePrefix) {
        super.setFilenamePrefix(filenamePrefix);
        unzippedExporter.setFilenamePrefix(filenamePrefix);
    }
}
