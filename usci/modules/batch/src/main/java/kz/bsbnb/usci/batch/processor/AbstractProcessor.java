package kz.bsbnb.usci.batch.processor;

import org.springframework.batch.item.ItemProcessor;

/**
 * @author k.tulbassiyev
 */
public interface AbstractProcessor<T, O> extends ItemProcessor<T, O> {

}
