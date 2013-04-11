package kz.bsbnb.usci.receiver.test.helper;

import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.fail;

/**
 * @author abukabayev
 */
public class FileHelperTest {
    FileHelper fileHelper = new FileHelper();

    @Test
    public void testBytesNull() throws Exception {
        boolean pass;
        pass = false;
        File ff = new File("test");
        try{
            fileHelper.getFileBytes(ff);
        } catch (NullPointerException e){
            pass=true;
        }
        if (!pass){
            fail("Empty file");
        }
    }
}
