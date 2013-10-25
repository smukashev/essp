package com.bsbnb.creditregistry.portlets.crosscheck.data;

import com.bsbnb.creditregistry.portlets.crosscheck.CrossCheckPortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheckMessage;
import com.bsbnb.creditregistry.portlets.crosscheck.model.Message;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckMessageDisplayWrapper {

    private CrossCheckMessage crossCheckMessage;
    private String description;
    private String help;

    public CrossCheckMessageDisplayWrapper(CrossCheckMessage message) {
        this.crossCheckMessage = message;
        if (message.getDescription() != null) {
            String text = message.getDescription();
            int pos = text.indexOf("|");
            if (pos < 0) {
                description = text;
            } else {
                description = text.substring(0, pos);
                help = "<h2>" + text.substring(pos + 1) + "</h2>";
            }
        }
    }

    public String getDescription() {
        if (crossCheckMessage.getMessage() != null) {
            Message message = crossCheckMessage.getMessage();
            return "KZ".equals(CrossCheckPortletEnvironmentFacade.get().getCurrentLanguage()) ? message.getNameKz() : message.getNameRu();
        }
        return description;
    }

    public String getHelp() {
        if (crossCheckMessage.getMessage() != null) {
            return crossCheckMessage.getMessage().getNote();
        }
        return help;
    }

    public String getInnerValue() {
        return crossCheckMessage.getInnerValue();
    }

    public String getOuterValue() {
        return crossCheckMessage.getOuterValue();
    }

    public String getDifference() {
        return crossCheckMessage.getDifference();
    }

    public int getIsError() {
        return crossCheckMessage.getIsError();
    }
}
