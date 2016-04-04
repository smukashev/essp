package kz.bsbnb.usci.eav.persistance.logic.impl;

import kz.bsbnb.usci.brms.rulemodel.service.IRuleService;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.persistance.logic.IRuleServicePool;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class RuleServicePool implements IRuleServicePool {

    IRuleService ruleService;

    @Override
    public IRuleService getRuleService() {

        if(ruleService != null)
            return ruleService;

        RmiProxyFactoryBean ruleServiceFactoryBean = new RmiProxyFactoryBean();
        ruleServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1097/ruleService");
        ruleServiceFactoryBean.setServiceInterface(IRuleService.class);
        ruleServiceFactoryBean.setRefreshStubOnConnectFailure(true);

        ruleServiceFactoryBean.afterPropertiesSet();
        ruleService = (IRuleService) ruleServiceFactoryBean.getObject();

        return ruleService;
    }
}
