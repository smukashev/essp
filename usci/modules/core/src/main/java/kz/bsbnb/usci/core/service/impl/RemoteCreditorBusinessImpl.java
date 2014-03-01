package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RemoteCreditorBusinessImpl implements RemoteCreditorBusiness
{
    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Override
    public List<Creditor> findMainOfficeCreditors()
    {
        List<BaseEntity> entities = baseEntityDao.getEntityByMetaclass(
                metaClassRepository.getMetaClass("ref_creditor"));

        ArrayList<Creditor> creditors = new ArrayList<Creditor>();

        for (BaseEntity entity : entities) {
            Creditor creditor = new Creditor();

            creditor.setId(entity.getId());
            BaseValue value = (BaseValue)entity.getBaseValue("name");
            if (value != null)
                creditor.setName((String)value.getValue());
            else
                creditor.setName("none");

            value = (BaseValue)entity.getBaseValue("short_name");
            if (value != null)
                creditor.setShortName((String)value.getValue());
            else
                creditor.setShortName("none");

            value = (BaseValue)entity.getBaseValue("code");
            if (value != null)
                creditor.setCode((String)value.getValue());
            else
                creditor.setCode("none");

            creditor.setBIN("");
            value = (BaseValue)entity.getBaseValue("docs");
            if (value != null && value.getValue() != null) {
                BaseSet docs = (BaseSet)value.getValue();

                for (IBaseValue doc : docs.get()) {
                    BaseEntity docEntity = (BaseEntity)doc.getValue();
                    if(docEntity != null) {
                        String doc_type = (String)docEntity.getEl("doc_type.code");
                        if (doc_type.equals("15")) {
                            creditor.setBIN((String)docEntity.getEl("no"));
                        }
                        if (doc_type.equals("11")) {
                            creditor.setRNN((String)docEntity.getEl("no"));
                        }
                    }
                }
            }

            creditors.add(creditor);
        }

        return creditors;
    }

    @Override
    public boolean creditorApproved(Creditor cred) {
        return baseEntityDao.isApproved(cred.getId());
    }

    @Override
    public int contractCount(Creditor cred) {
        int c_count = baseEntityDao.batchCount(cred.getId(), "ct_package");

        System.out.println("### " + cred.getId() + " - " + c_count);

        return c_count;
    }
}
