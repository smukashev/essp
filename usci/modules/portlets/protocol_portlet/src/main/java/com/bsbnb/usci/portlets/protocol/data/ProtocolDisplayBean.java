package com.bsbnb.usci.portlets.protocol.data;

import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import com.bsbnb.usci.portlets.protocol.ProtocolPortletResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Aidar.Myrzahanov
 */
public class ProtocolDisplayBean {
    private Protocol protocol;
    private boolean isText = false;
    private String text;
    private Embedded statusIcon;
    private Link link;
    public final Logger logger = Logger.getLogger(ProtocolDisplayBean.class);
    private final HashMap<String, String> messageTypeString = new HashMap<String, String>();



    public ProtocolDisplayBean(Protocol protocol) {
        this.protocol = protocol;
    }

    public ProtocolDisplayBean(String text) {
        this.isText = true;
        this.text = text;
    }

    public static Resource getIconByMessageTypeName(String name) {
        if ("ERROR".equals(name)) {
            return ProtocolPortletResource.CRITICAL_ERROR_ICON;
        } else {
            return ProtocolPortletResource.OK_ICON;
        }
    }

    /**
     * ERROR or INFO
     */
    public String getMessageType() {
        String typeName = getMessageTypeCode();

        if ("ERROR".equals(typeName)) {
            return "ERROR";
        } else {
            return "INFO";
        }
    }

    public String getMessage() {
        Message message = protocol.getMessage();

        if (message == null)
            return "";

        if (PortletEnvironmentFacade.get().isLanguageKazakh())
            return message.getNameKz();

        return message.getNameRu();
    }

    public Embedded getStatusIcon() {
        if (statusIcon == null) {
            Resource resource = getIconByMessageTypeName(getMessageTypeCode());
            statusIcon = new Embedded("", resource);
            statusIcon.setDescription(getMessageTypeName());
        }

        return statusIcon;
    }

    public void setStatusIcon(Embedded statusIcon) {
        this.statusIcon = statusIcon;
    }

    public Shared getType() {
        return protocol.getProtocolType();
    }

    public String getDescription() {
        return protocol.getTypeDescription()==null ? protocol.getTypeDescription() : protocol.getTypeDescription().split(" \\| ")[0];
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

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getNote() {
        return protocol.getNote();
    }

    public Date getPrimaryContractDate() {
        Date primaryContractDate=null;
        try{
            primaryContractDate = protocol.getPrimaryContractDate()!=null?protocol.getPrimaryContractDate(): protocol.getTypeDescription()!=null?new SimpleDateFormat("dd.MM.yyyy").parse(protocol.getTypeDescription().split(" \\| ")[1]):protocol.getPrimaryContractDate();

        }
        catch(ParseException e){
            logger.error(e.getMessage(),e);
        }
        return primaryContractDate;
    }

    public boolean isError() {
        return "ERROR".equals(getMessageTypeCode());
    }

    public String getMessageTypeCode() {
        return protocol.getMessageType().getCode();
    }

    public String getMessageTypeString() {
        switch(getMessageTypeCode()) {
            case "CHECK_IN_PARSER":
                return "Проверка";
            case "WAITING":
                return "В очереди";
            case "PROCESSING":
                return "Обработка";
            case "COMPLETED":
                return "Завершен";
            default:
                return "Неизвестно";
        }
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

    public void setText(String text) {
        this.text = text;
    }

    public String getMessageTypeName() {
        Shared messageType = protocol.getMessageType();
        if (messageType == null) {
            return "";
        }
        return PortletEnvironmentFacade.get().isLanguageKazakh() ? messageType.getNameKz() : messageType.getNameRu();
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void setIsText(boolean isText) {
        this.isText = isText;
    }
}
