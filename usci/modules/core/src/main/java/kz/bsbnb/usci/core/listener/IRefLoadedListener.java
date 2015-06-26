package kz.bsbnb.usci.core.listener;

import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;

import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 26/06/2015.
 */
public interface IRefLoadedListener {
    void process(long userId, IMetaClass metaClass, String attr, List<RefListItem> list);
}
