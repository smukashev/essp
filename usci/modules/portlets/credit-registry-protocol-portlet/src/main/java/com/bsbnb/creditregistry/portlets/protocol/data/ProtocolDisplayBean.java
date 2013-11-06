package com.bsbnb.creditregistry.portlets.protocol.data;

import java.util.Date;

//import com.bsbnb.creditregistry.dm.maintenance.Message;
//import com.bsbnb.creditregistry.dm.maintenance.Protocol;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.dm.ref.shared.MessageType;
import com.bsbnb.creditregistry.portlets.protocol.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.protocol.ProtocolPortletResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.MessageType;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ProtocolDisplayBean {

    private Protocol protocol;
    private boolean isText = false;
    private String text;

    public ProtocolDisplayBean(Protocol protocol) {
        this.protocol = protocol;
    }

    public ProtocolDisplayBean(String text) {
        this.isText = true;
        this.text = text;
    }

    public static Resource getIconByMessageTypeCode(String code) {
        if(code==null) {
            code = "";
        }
        ProtocolPortletResource resource = null;
        if (code.equals(MessageType.CRITICAL_ERROR.getCode())) {
            resource = ProtocolPortletResource.CRITICAL_ERROR_ICON;
        } else if (code.equals(MessageType.NON_CRITICAL_ERROR.getCode())) {
            resource = ProtocolPortletResource.WARNING_ICON;
        } else if (code.equals(MessageType.INFO.getCode())) {
            resource = ProtocolPortletResource.INFO_ICON;
        } else {
            resource = ProtocolPortletResource.OK_ICON;
        }
        return resource;

    }

    public String getMessage() {
        Message message = protocol.getMessage();
        if (message == null) {
            return "";
        }
        if (PortletEnvironmentFacade.get().isLanguageKazakh()) {
            return message.getNameKz();
        }
        return message.getNameRu();
    }
    
    private Embedded statusIcon;

    public Embedded getStatusIcon() {
        if (statusIcon == null) {
            Resource resource = getIconByMessageTypeCode(getMessageTypeCode());
            statusIcon = new Embedded("", resource);
            statusIcon.setDescription(getMessageTypeName());;
        }
        return statusIcon;
    }

    public Shared getType() {
        return protocol.getProtocolType();
    }

    public String getDescription() {
        return protocol.getTypeDescription();
    }

    public String getTypeName() {
        Shared type = protocol.getProtocolType();
        if (type == null) {
            return "";
        }
        return PortletEnvironmentFacade.get().isLanguageKazakh() ? type.getNameKz() : type.getNameRu();
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getNote() {
        return protocol.getNote();
    }

    public Date getPrimaryContractDate() {
        return protocol.getPrimaryContractDate();
    }

    public boolean isError() {
        return MessageType.CRITICAL_ERROR.getCode().equals(protocol.getMessageType().getCode());
    }

    public String getMessageTypeCode() {
        return protocol.getMessageType().getCode();
    }

    public String getMessageCode() {
        return protocol.getMessage().getCode();
    }

    /**
     * @return the isText
     */
    public boolean isText() {
        return isText;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    public String getMessageTypeName() {
        Shared messageType = protocol.getMessageType();
        if (messageType == null) {
            return "";
        }
        return PortletEnvironmentFacade.get().isLanguageKazakh() ? messageType.getNameKz() : messageType.getNameRu();
    }

    private Link link;

    public Link getLink()
    {
        return link;
    }

    public void setLink(Link link)
    {
        this.link = link;
    }
}
