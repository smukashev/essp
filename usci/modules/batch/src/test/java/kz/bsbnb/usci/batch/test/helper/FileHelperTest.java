package kz.bsbnb.usci.batch.test.helper;

import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.fail;

/**
 * @author abukabayev
 */
public class FileHelperTest {

    FileHelper fileHelper = new FileHelper();



    @Test
    public void testFile() throws Exception {

//         File ff = new File("test2.txt");
//         byte[] bytes = new byte[new Random().nextInt(100)];
//         new Random().nextBytes(bytes);
//
//
//         FileOutputStream fos = new FileOutputStream(ff);
//         fos.write(bytes);
//         fos.close();
//
//
//         Assert.assertEquals(bytes,fileHelper.getFileBytes(ff));
    }

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
