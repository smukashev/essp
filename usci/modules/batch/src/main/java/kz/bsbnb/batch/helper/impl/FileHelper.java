package kz.bsbnb.batch.helper.impl;

import kz.bsbnb.batch.helper.AbstractHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author k.tulbassiyev
 */
public class FileHelper extends AbstractHelper
{
    public byte[] getFileBytes(File file) {
        FileInputStream fii = null;
        byte bytes[] = null;

        try
        {
            fii = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        try
        {
            bytes = new byte[fii.available()];
            fii.read(bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bytes;
    }
}
