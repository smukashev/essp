package kz.bsbnb.usci.batch.writer;

import kz.bsbnb.usci.eav.model.BaseEntity;
import org.springframework.batch.item.ItemWriter;

/**
 * @author k.tulbassiyev
 */
public interface AbstractWriter<T> extends ItemWriter<T>
{
}
