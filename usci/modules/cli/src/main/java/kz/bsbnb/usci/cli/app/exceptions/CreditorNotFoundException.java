package kz.bsbnb.usci.cli.app.exceptions;

/**
 * Created by Bauyrzhan.Makhambeto on 12/03/2015.
 */
public class CreditorNotFoundException extends Exception {
    @Override
    public String getMessage() {
        return "creditor not found";
    }
}
