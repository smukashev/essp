package kz.bsbnb.usci.bconv.xsd;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextTest.xml"})
@ActiveProfiles({"postgres"})
public class Xsd2MetaClassTest
{
    @Test
    public void testConvertXSD() throws Exception
    {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("credit-registry.xsd");

        MetaClass meta = Xsd2MetaClass.convertXSD(in, "ct_package");

        System.out.println("---------------------");
        System.out.println(meta.toString());
    }
}
