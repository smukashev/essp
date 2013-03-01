package kz.bsbnb.usci.batch.writer.impl;

import kz.bsbnb.usci.batch.writer.AbstractWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Component
public class SimpleEventEntityWriter<T> implements AbstractWriter<T>
{
    @Override
    public void write(List items) throws Exception
    {
        System.out.println(" -------------- ");
        System.out.println(items);
    }
}
