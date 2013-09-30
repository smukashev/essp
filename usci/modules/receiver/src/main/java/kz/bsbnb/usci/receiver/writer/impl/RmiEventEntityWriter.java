package kz.bsbnb.usci.receiver.writer.impl;

import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.singleton.StatusSingleton;
import kz.bsbnb.usci.receiver.writer.IWriter;
import kz.bsbnb.usci.sync.service.IEntityService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class RmiEventEntityWriter<T> implements IWriter<T> {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    private IEntityService entityService;
    private Logger logger = Logger.getLogger(RmiEventEntityWriter.class);

    @Autowired
    private RulesSingleton rulesSingleton;

    @Autowired
    protected StatusSingleton statusSingleton;

    @PostConstruct
    public void init() {
        logger.info("Writer init");
        rulesSingleton.reloadCache();
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public void write(List items) throws Exception {
        logger.info("Writer write: " + items.size());

        Iterator<Object> iter = items.iterator();

        ArrayList<BaseEntity> entitiesToSave = new ArrayList<BaseEntity>(items.size());

        while(iter.hasNext()) {
            BaseEntity entity = (BaseEntity)iter.next();
            rulesSingleton.runRules(entity, entity.getMeta().getClassName() + "_parser", entity.getReportDate());

            if (entity.getValidationErrors().size() > 0) {
                for (String errorMsg : entity.getValidationErrors()) {
                    //TODO: check for error with Index
                    statusSingleton.addContractStatus(entity.getBatchId(), new ContractStatusJModel(entity.getBatchIndex() - 1,
                            Global.CONTRACT_STATUS_ERROR, errorMsg, new Date()));

                    //System.out.println("Error: " + errorMsg);
                }
                if (entity.getValidationErrors().size() == 0) {
                    entitiesToSave.add(entity);
                }

                //TODO: fix
                //iter.remove();
            }
        }

        entityService.process(entitiesToSave);
    }
}
