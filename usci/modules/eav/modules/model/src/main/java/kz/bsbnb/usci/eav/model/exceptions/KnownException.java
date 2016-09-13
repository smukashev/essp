package kz.bsbnb.usci.eav.model.exceptions;

public class KnownException extends RuntimeException {

    public KnownException(){
        super();
    }

    public KnownException(String message) {
        super(message);
    }
}
