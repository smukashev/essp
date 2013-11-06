package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.protocol.ProtocolSingleton;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService {
    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IBaseEntitySearcher searcher;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    protected ProtocolSingleton statusSingleton;

    public EntityServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void save(BaseEntity baseEntity) {
        long t1 = System.currentTimeMillis();
        BaseEntity entity = (BaseEntity)baseEntityDao.process(baseEntity);
        long t2 = System.currentTimeMillis() - t1;

        Date contractDate = (Date)entity.getEl("primary_contract.date");
        String contractNo = (String)entity.getEl("primary_contract.no");

        System.out.println("[core][save]                :           " + t2);
        System.out.println(contractNo + " - " + contractDate);

        statusSingleton.addContractStatus(entity.getBatchId(), new ContractStatusJModel(
                entity.getBatchIndex() - 1,
                "SAVED", "" + entity.getId(), new Date(),
                contractNo,
                contractDate));
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
        ArrayList<Long> result = searcher.findAll(baseEntity);

        return (BaseEntity)baseEntityDao.load(result.get(0));
    }

    @Override
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {


        Long id = metaClassDao.save(baseEntityLoad.getMeta());
        baseEntityDao.saveOrUpdate(baseEntityLoad);
    }

    @Override
    public BaseEntity load(long id) {
        System.out.println("Load with id: " + id);
        return (BaseEntity)baseEntityDao.load(id);
    }
}
