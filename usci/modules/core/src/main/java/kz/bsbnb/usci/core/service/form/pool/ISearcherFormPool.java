package kz.bsbnb.usci.core.service.form.pool;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;

import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 01/07/2015.
 */
public interface ISearcherFormPool {
    List<ISearcherForm> getSearcherForms();
    ISearcherForm getSearchForm(String className);
}
