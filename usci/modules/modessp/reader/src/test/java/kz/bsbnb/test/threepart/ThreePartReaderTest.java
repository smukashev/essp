package kz.bsbnb.test.threepart;

import kz.bsbnb.reader.test.ThreePartReader;
import kz.bsbnb.testing.FunctionalTest;
import org.junit.Test;

import java.io.InputStream;


public class ThreePartReaderTest extends FunctionalTest{
    ThreePartReader reader = new ThreePartReader();

    @Test
    public void testThreePart() throws Exception {
        InputStream inputStream = getInputStream("threepart/infoattop.xml");

        reader.withSource(inputStream)
            .withMeta(metaCredit)
            .read();

    }
}