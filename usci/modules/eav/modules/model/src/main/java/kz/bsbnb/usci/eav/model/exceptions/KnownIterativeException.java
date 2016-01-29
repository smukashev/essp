package kz.bsbnb.usci.eav.model.exceptions;

import java.util.List;

/**
 * Created by bauka on 1/24/16.
 */
public class KnownIterativeException extends KnownException {

    private List<String> messages;

    public KnownIterativeException(String message) {
        super(message);
    }

    public KnownIterativeException(List<String> messages) {
        super("");
        this.messages = messages;
    }

    public List<String> getMessages(){
        return messages;
    }

}
