package kz.bsbnb.usci.receiver.helper.impl;

import kz.bsbnb.usci.receiver.helper.IHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author k.tulbassiyev
 */
@Component
public class FileHelper implements IHelper {
    public byte[] getFileBytes(File file) {
        FileInputStream fii;
        byte bytes[];

        try {
            fii = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            fii = null;
        }

        try {
            bytes = new byte[fii.available()];
            fii.read(bytes);
        } catch (IOException e) {
            bytes = null;
        }

        return bytes;
    }
}
