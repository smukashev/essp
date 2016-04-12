package kz.bsbnb.usci.receiver.writer.impl;

import kz.bsbnb.usci.brms.rulemodel.service.IRuleService;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
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
    public static final String LOGIC_RULE_SETTING = "LOGIC_RULE_SETTING";
    public static final String LOGIC_RULE_META = "LOGIC_RULE_META";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    @SuppressWarnings("SpringJavaAutowiringInspection")
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

        String[] metas = serviceFactory.getGlobalService().getValue(LOGIC_RULE_SETTING, LOGIC_RULE_META).split(",");
        Collections.addAll(metaRules, metas);
    }

    @Override
    public void write(List items) throws Exception {
        entityService.process(items);
    }
}
