package kz.bsbnb.usci.eav.persistance.logic;

import kz.bsbnb.usci.brms.rulemodel.service.IRuleService;

/**
 * Created by bauka on 1/23/16.
 */
public interface IRuleServicePool {
    public IRuleService getRuleService();
}
