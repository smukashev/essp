package kz.bsbnb.usci.bconv.cr.parser.model;

import java.io.CharArrayWriter;

/**
 * @author k.tulbassiyev
 */
public class CustomCharArrayWriter extends CharArrayWriter {
    @Override
    public String toString() {
        return super.toString().trim();
    }
}
