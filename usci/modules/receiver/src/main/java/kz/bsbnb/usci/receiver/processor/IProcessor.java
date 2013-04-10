package kz.bsbnb.usci.receiver.processor;

import org.springframework.batch.item.ItemProcessor;

/**
 * @author k.tulbassiyev
 */
public interface IProcessor<T, O> extends ItemProcessor<T, O> {

}
