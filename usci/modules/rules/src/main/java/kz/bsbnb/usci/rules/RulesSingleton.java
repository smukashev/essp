package kz.bsbnb.usci.rules;

import org.drools.runtime.StatelessKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class RulesSingleton
{
    @Autowired
    private StatelessKnowledgeSession ksession;


}
