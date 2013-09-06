package kz.bsbnb.usci.receiver.helper.impl;

import kz.bsbnb.usci.receiver.helper.IHelper;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author k.tulbassiyev
 */
@Component
public class FileHelper implements IHelper {
    public byte[] getFileBytes(File file) {
        FileInputStream fii = null;
        byte bytes[] = null;

        try {
            fii = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            bytes = new byte[fii.available()];
            fii.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }
}