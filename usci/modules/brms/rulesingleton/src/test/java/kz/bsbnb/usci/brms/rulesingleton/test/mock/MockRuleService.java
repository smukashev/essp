package kz.bsbnb.usci.brms.rulesingleton.test.mock;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockRuleService implements IRuleService
{
    @Override
    public long save(Rule rule, BatchVersion batchVersion)
    {
        //not used in tests
        return 0;
    }

    @Override
    public List<Rule> load(BatchVersion batchVersion)
    {
        List<Rule> rules = new ArrayList<Rule>();

        Rule rule = new Rule("rule \"test1\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ()\n" +
                "    then\n" +
                "        System.out.println(\"Test rule, always executes.\");\n" +
                "end");
        rule.setId(1);
        rule.setTitle("Rule 1");
        rules.add(rule);

        rule = new Rule("rule \"test2\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ( getEl(\"subject.name.lastname\") == \"TULBASSIYEV\" )\n" +
                "    then\n" +
                "        $entity.addValidationError(\"Test rule, that is true for TULBASSIYEV\");\n" +
                "end");
        rule.setId(2);
        rule.setTitle("Rule 2");
        rules.add(rule);

        rule = new Rule("rule \"test3\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ( getEl(\"subject.name.lastname\") != \"TULBASSIYEV\" )\n" +
                "    then\n" +
                "        $entity.addValidationError(\"Test rule, that is false for TULBASSIYEV\");\n" +
                "end");
        rule.setId(3);
        rule.setTitle("Rule 3");
        rules.add(rule);

        return rules;
    }

    @Override
    public void update(Rule rule)
    {
        //not used in tests
    }

    @Override
    public List<Rule> getAllRules()
    {
        //not used in tests
        return null;
    }
}
