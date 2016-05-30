package kz.bsbnb.usci.core.service.form.pool.impl;

import kz.bsbnb.usci.core.service.form.pool.ISearcherFormPool;
import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 01/07/2015.
 */
@Component
public class SearcherFormPool implements ISearcherFormPool {

    @Autowired
    List<ISearcherForm> searcherForms;

    @Override
    public List<ISearcherForm> getSearcherForms() {
        Iterator<ISearcherForm> iterator = searcherForms.listIterator();

        while(iterator.hasNext()) {
            ISearcherForm sf = iterator.next();
            if(sf.getClass().getSimpleName().startsWith("Dev"))
                iterator.remove();
        }

        return searcherForms;
    }

    @Override
    public ISearcherForm getSearchForm(String className) {
        for(ISearcherForm sf : searcherForms)
            if(sf.getClass().getName().equals(className))
                return sf;

        throw new RuntimeException(Errors.compose(Errors.E230));
    }
}
