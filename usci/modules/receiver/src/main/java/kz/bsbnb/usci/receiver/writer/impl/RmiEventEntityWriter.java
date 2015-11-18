package kz.bsbnb.usci.receiver.writer.impl;

import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.StatusProperties;
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

    @Autowired
    private IServiceRepository serviceFactory;

    private IEntityService entityService;
    private Logger logger = Logger.getLogger(RmiEventEntityWriter.class);
    private IRuleService ruleService;

    private IBatchService batchService;

    @Autowired
    protected SQLQueriesStats sqlStats;

    @Autowired
    protected Global global;

    private Set<String> metaRules = new HashSet<>();

    @PostConstruct
    public void init() {
        logger.info("Writer init");

        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
        ruleService = (IRuleService) rmiProxyRuleService.getObject();
        batchService = serviceFactory.getBatchService();

        metaRules.add("credit");
        metaRules.add("ref_creditor");
        metaRules.add("ref_creditor_branch");
        metaRules.add("ref_exclusive_doc");
    }

    @Override
    public void write(List items) throws Exception {
        logger.info("Writer write: " + items.size());

        Iterator<BaseEntity> iterator = items.iterator();

        ArrayList<BaseEntity> entitiesToSave = new ArrayList<>(items.size());

        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            List<String> errors = new LinkedList<>(entity.getValidationErrors());
            String ruleRuntimeException = null;

            if(global.isRulesEnabled() && entity.getMeta() != null &&
                    metaRules.contains(entity.getMeta().getClassName())) {
                try {
                    long t1 = System.currentTimeMillis();

                    errors = ruleService.runRules(entity, entity.getMeta().getClassName() + "_parser",
                            entity.getReportDate());

                    sqlStats.put(entity.getMeta().getClassName() + "_parser", System.currentTimeMillis() - t1);
                } catch (Exception e) {
                    logger.error("Не могу применить бизнес правила: " + e.getMessage());
                    ruleRuntimeException = e.getMessage();
                }
            }

            if(ruleRuntimeException != null) {
                ruleRuntimeException = "Ошибка при запуске правил: " + ruleRuntimeException;

                EntityStatus entityStatus = new EntityStatus()
                        .setBatchId(entity.getBatchId())
                        .setEntityId(entity.getId())
                        .setStatus(EntityStatuses.ERROR)
                        .setDescription(StatusProperties.getSpecificParams(entity) + " (" +
                                ruleRuntimeException + ")")
                        .setReceiptDate(new Date())
                        .setIndex(entity.getBatchIndex() - 1);


                batchService.addEntityStatus(entityStatus);
            } else if (errors != null && errors.size() > 0) {
                for (String errorMsg : errors) {
                    EntityStatus entityStatus = new EntityStatus()
                            .setBatchId(entity.getBatchId())
                            .setEntityId(entity.getId())
                            .setStatus(EntityStatuses.ERROR)
                            .setDescription(StatusProperties.getSpecificParams(entity) + " (" +
                                    errorMsg + ")")
                            .setReceiptDate(new Date())
                            .setIndex(entity.getBatchIndex() - 1);

                    batchService.addEntityStatus(entityStatus);
                }
            } else {
                entitiesToSave.add(entity);
            }
        }

        entityService.process(entitiesToSave);
    }
}
