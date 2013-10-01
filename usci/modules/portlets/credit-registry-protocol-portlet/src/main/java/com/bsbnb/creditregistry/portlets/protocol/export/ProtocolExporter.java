/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.creditregistry.portlets.protocol.export;

import com.bsbnb.creditregistry.portlets.protocol.data.ProtocolDisplayBean;
import com.bsbnb.util.translit.Transliterator;
import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class ProtocolExporter {

    private List<ProtocolDisplayBean> protocols;
    private Application application;
    private String filenamePrefix;
    
    public ProtocolExporter() {
        
    }
    
    public ProtocolExporter(List<ProtocolDisplayBean> protocols, Application application) {
        this.protocols = Collections.unmodifiableList(protocols);
        this.application = application;
    }

    public StreamResource getResource() throws ExportException{
        final byte[] bytes = export();
        StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {

            public InputStream getStream() {
                return new ByteArrayInputStream(bytes);
            }
        };
        StreamResource resource = new StreamResource(streamSource, getFileName(), application) {

            @Override
            public DownloadStream getStream() {
                DownloadStream downloadStream = super.getStream();
                downloadStream.setParameter("Content-Disposition", "attachment;filename=" + getFilename());
                downloadStream.setContentType(getContentType());
                downloadStream.setCacheTime(0);
                return downloadStream;
            }
        };
        return resource;
    }

    protected abstract byte[] export() throws ExportException;
    protected abstract String getFileName();
    protected abstract String getContentType();

    /**
     * @return the protocols
     */
    protected List<ProtocolDisplayBean> getProtocols() {
        return protocols;
    }

    /**
     * @param protocols the protocols to set
     */
    public void setProtocols(List<ProtocolDisplayBean> protocols) {
        this.protocols = protocols;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(Application application) {
        this.application = application;
    }
    
    protected String getFilenamePrefix() {
        return filenamePrefix;
    }
    
    public void setFilenamePrefix(String filenamePrefix) {
        this.filenamePrefix = Transliterator.transliterate(filenamePrefix);
    }
}
