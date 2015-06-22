package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;

/**
 * Created by Bauyrzhan.Makhambeto on 20/06/2015.
 */
public enum MailMessageStatus implements Serializable{
    BATCH_STOPPED(131),
    PROCESSING(132),
    SENT(133),
    REJECTED_BY_USER_SETTINGS(134);

    private int value;

    MailMessageStatus(int value) {
        this.value = value;
    }

    public static MailMessageStatus getFromInt(Integer status){
        switch (status){
            case 131:
                return MailMessageStatus.BATCH_STOPPED;
            case 132:
                return MailMessageStatus.PROCESSING;
            case 133:
                return MailMessageStatus.SENT;
            case 134:
                return MailMessageStatus.REJECTED_BY_USER_SETTINGS;
        }

        throw new RuntimeException("status not found");
    }

}
