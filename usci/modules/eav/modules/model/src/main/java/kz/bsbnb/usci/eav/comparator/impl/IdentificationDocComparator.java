package kz.bsbnb.usci.eav.comparator.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.Comparator;

public class IdentificationDocComparator implements Comparator<IBaseValue> {
    @Override
    public int compare(IBaseValue val1, IBaseValue val2) {
        BaseEntity doc1 = (BaseEntity) val1.getValue();
        BaseEntity doc2 = (BaseEntity) val2.getValue();

        if (doc1 == null)
            throw new IllegalStateException(Errors.compose(Errors.E3,val1));

        if (doc2 == null)
            throw new IllegalStateException(Errors.compose(Errors.E3,val2));

        BaseEntity docType1 = (BaseEntity) doc1.getEl("doc_type");
        BaseEntity docType2 = (BaseEntity) doc2.getEl("doc_type");

        if (docType1 == null)
            throw new IllegalStateException(Errors.compose(Errors.E4,doc1));

        if (docType2 == null)
            throw new IllegalStateException(Errors.compose(Errors.E4,doc2));

        if (docType1.getBaseValue("weight") == null || docType1.getBaseValue("weight").getValue() == null)
            throw new IllegalStateException(Errors.compose(Errors.E5,doc1));

        if (docType2.getBaseValue("weight") == null || docType2.getBaseValue("weight").getValue() == null)
            throw new IllegalStateException(Errors.compose(Errors.E5,doc2));

        Integer weight1 = Integer.parseInt(docType1.getBaseValue("weight").getValue().toString());
        Integer weight2 = Integer.parseInt(docType2.getBaseValue("weight").getValue().toString());

        return weight1 < weight2 ? -1 : weight1 > weight2 ? 1 : 0;
    }
}
