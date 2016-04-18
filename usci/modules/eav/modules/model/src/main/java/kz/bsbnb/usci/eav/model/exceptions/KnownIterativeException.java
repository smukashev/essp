package kz.bsbnb.usci.eav.model.exceptions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class KnownIterativeException extends KnownException {

    private Collection<String> messages;

    public KnownIterativeException(String message) {
        super(message);
    }

    public KnownIterativeException(List<String> messages) {
        super("");
        this.messages = messages;
    }

    public KnownIterativeException(Set<String> messages) {
        super("");
        this.messages = messages;
    }

    public Collection<String> getMessages(){
        return messages;
    }

}
