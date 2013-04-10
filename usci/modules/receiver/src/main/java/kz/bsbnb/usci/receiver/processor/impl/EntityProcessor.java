package kz.bsbnb.usci.receiver.processor.impl;

import kz.bsbnb.usci.receiver.processor.IProcessor;
import org.springframework.stereotype.Component;

/**
 * @author k.tulbassiyev
 */
@Component
public class EntityProcessor<T, O> implements IProcessor<T, O> {
    @Override
    public O process(T item) throws Exception {
        // todo: implement
        return (O) item;
    }
}
