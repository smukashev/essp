package kz.bsbnb.usci.portlets.signing;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SignatureValidationException extends Exception {

    private final Localization code;
    private final Object[] arguments;

    public SignatureValidationException(Localization code, Object... arguments) {
        this.code = code;
        this.arguments = arguments;
    }

    /**
     * @return the code
     */
    public Localization getCode() {
        return code;
    }

    /**
     * @return the args
     */
    public Object[] getArguments() {
        return arguments;
    }

}
