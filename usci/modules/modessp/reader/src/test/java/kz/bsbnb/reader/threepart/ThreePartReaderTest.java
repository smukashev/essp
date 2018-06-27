package kz.bsbnb.reader.threepart;

import kz.bsbnb.reader.test.ThreePartReader;
import kz.bsbnb.testing.FunctionalTest;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;


public class ThreePartReaderTest extends FunctionalTest{
    ThreePartReader reader = new ThreePartReader();

    @Test
    public void testThreePart() throws Exception {
        InputStream inputStream =  Thread.currentThread().
                getContextClassLoader().getResourceAsStream("kz/bsbnb/reader/threepart/infoattop.xml");

        reader.withSource(inputStream)
            .withMeta(metaCredit)
            .read();

    }
}