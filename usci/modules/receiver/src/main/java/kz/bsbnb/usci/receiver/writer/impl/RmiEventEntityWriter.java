package kz.bsbnb.usci.receiver.writer.impl;

import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
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

    @Autowired
    protected SQLQueriesStats sqlStats;

    @PostConstruct
    public void init() {
        logger.info("Writer init");
        rulesSingleton.reloadCache();
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public void write(List items) throws Exception {
        logger.info("Writer write: " + items.size());
        //System.out.println("Writer write: " + items.size());

        Iterator<Object> iter = items.iterator();

        ArrayList<BaseEntity> entitiesToSave = new ArrayList<BaseEntity>(items.size());

        while(iter.hasNext()) {
            BaseEntity entity = (BaseEntity)iter.next();
            //System.out.println(entity.toString());

            Date contractDate = (Date)entity.getEl("primary_contract.date");
            String contractNo = (String)entity.getEl("primary_contract.no");

            if (statusSingleton.isEntityCompleted(entity.getBatchId(), entity.getBatchIndex() - 1)) {
                //System.out.println("Contract no " + contractNo + " with date " + contractDate + " skipped because it " +
                        //"has status \"" + EntityStatuses.COMPLETED + "\"");
                continue;
            }

            //TODO: uncomment!!!
            statusSingleton.addContractStatus(entity.getBatchId(), new ContractStatusJModel(
                    entity.getBatchIndex() - 1,
                    EntityStatuses.CHECK_IN_PARSER, null, new Date(),
                    contractNo,
                    contractDate));
            try {
                long t1 = System.currentTimeMillis();
                rulesSingleton.runRules(entity, entity.getMeta().getClassName() + "_parser", entity.getReportDate());
                sqlStats.put(entity.getMeta().getClassName() + "_parser", System.currentTimeMillis() - t1);
            } catch(Exception e) {
                logger.error("Can't run rules: " + e.getMessage());
            }

            if (entity.getValidationErrors().size() > 0) {
                for (String errorMsg : entity.getValidationErrors()) {
                    System.out.println(errorMsg);
                    //TODO: check for error with Index
                    statusSingleton.addContractStatus(entity.getBatchId(), new ContractStatusJModel(
                            entity.getBatchIndex() - 1,
                            EntityStatuses.ERROR, errorMsg, new Date(),
                            contractNo,
                            contractDate));
                }
            } else {
                statusSingleton.addContractStatus(entity.getBatchId(), new ContractStatusJModel(
                    entity.getBatchIndex() - 1,
                    EntityStatuses.WAITING, null, new Date(),
                    contractNo,
                    contractDate));

                entitiesToSave.add(entity);
            }
        }

        entityService.process(entitiesToSave);
    }
}
