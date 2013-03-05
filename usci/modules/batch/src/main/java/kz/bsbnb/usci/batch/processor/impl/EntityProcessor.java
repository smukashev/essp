package kz.bsbnb.usci.batch.processor.impl;

import kz.bsbnb.usci.batch.processor.AbstractProcessor;
import org.springframework.stereotype.Component;

/**
 * @author k.tulbassiyev
 */
@Component
public class EntityProcessor<T, O> implements AbstractProcessor<T, O>
{
    @Override
    public O process(T item) throws Exception
    {
        return (O) item;
    }
}
