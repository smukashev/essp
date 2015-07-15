package kz.bsbnb.usci.receiver.writer.impl;

import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
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
import java.util.*;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class RmiEventEntityWriter<T> implements IWriter<T> {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    @Qualifier(value="remoteRuleService")
    private RmiProxyFactoryBean rmiProxyRuleService;

    private IEntityService entityService;
    private Logger logger = Logger.getLogger(RmiEventEntityWriter.class);
    private IRuleService ruleService;

    @Autowired
    protected StatusSingleton statusSingleton;

    @Autowired
    protected SQLQueriesStats sqlStats;

    @Autowired
    protected Global global;

    private Set<String> metaRules = new HashSet<String>();

    @PostConstruct
    public void init() {
        logger.info("Writer init");
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
        ruleService = (IRuleService) rmiProxyRuleService.getObject();

        metaRules.add("credit");
        metaRules.add("ref_creditor");
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

            //TODO: UNCOMMENT
            /*if (statusSingleton.isEntityCompleted(entity.getBatchId(), entity.getBatchIndex() - 1)) {
                //System.out.println("Contract no " + contractNo + " with date " + contractDate + " skipped because it " +
                        //"has status \"" + EntityStatuses.COMPLETED + "\"");
                continue;
            }*/

            EntityStatusJModel entityStatusJModel = new EntityStatusJModel(
                    entity.getBatchIndex() - 1,
                    EntityStatuses.CHECK_IN_PARSER, null, new Date());

            StatusProperties.fillSpecificProperties(entityStatusJModel, entity);

            statusSingleton.addContractStatus(entity.getBatchId(), entityStatusJModel);

            List<String> errors = new LinkedList<String>(entity.getValidationErrors());
            String ruleRuntimeException = null;

            if(global.isRulesEnabled() && entity.getMeta() != null &&
                    metaRules.contains(entity.getMeta().getClassName())) {
                try {
                    long t1 = System.currentTimeMillis();

                    errors = ruleService.runRules(entity, entity.getMeta().getClassName() + "_parser",
                            entity.getReportDate());

                    sqlStats.put(entity.getMeta().getClassName() + "_parser", System.currentTimeMillis() - t1);
                } catch (Exception e) {
                    logger.error("Can't run rules: " + e.getMessage());
                    ruleRuntimeException = e.getMessage();
                }
            }

            if(ruleRuntimeException != null) {
                ruleRuntimeException = "Ошибка при запуске правил: " + ruleRuntimeException;
                entityStatusJModel = new EntityStatusJModel(
                        entity.getBatchIndex() - 1,
                        EntityStatuses.ERROR, ruleRuntimeException, new Date());

                StatusProperties.fillSpecificProperties(entityStatusJModel, entity);

                statusSingleton.addContractStatus(entity.getBatchId(), entityStatusJModel);
            } else if (errors != null && errors.size() > 0) {
                for (String errorMsg : errors) {
                    System.out.println(errorMsg);
                    //TODO: check for error with Index
                    entityStatusJModel = new EntityStatusJModel(
                            entity.getBatchIndex() - 1,
                            EntityStatuses.ERROR, errorMsg, new Date());

                    StatusProperties.fillSpecificProperties(entityStatusJModel, entity);

                    statusSingleton.addContractStatus(entity.getBatchId(), entityStatusJModel);
                }
            } else {
                entityStatusJModel = new EntityStatusJModel(
                        entity.getBatchIndex() - 1,
                        EntityStatuses.WAITING, null, new Date());

                StatusProperties.fillSpecificProperties(entityStatusJModel, entity);

                statusSingleton.addContractStatus(entity.getBatchId(), entityStatusJModel);

                entitiesToSave.add(entity);
            }
        }

        entityService.process(entitiesToSave);
    }
}
