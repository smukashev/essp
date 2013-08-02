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

        rule = new Rule("rule \"RNN_test1\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ( getEl(\"subject.documents.document[type=RNN].no\") == null )\n" +
                "    then\n" +
                "        $entity.addValidationError(\"Отсутствует РНН\");\n" +
                "end");
        rule.setId(2);
        rule.setTitle("RNN_test1");
        rules.add(rule);

        rule = new Rule("rule \"RNN_test2\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ( " +
                "           getEl(\"subject.documents.document[type=RNN].no\") != null && " +
                "           ((String)getEl(\"subject.documents.document[type=RNN].no\")).length() != 12 )\n" +
                "    then\n" +
                "        $entity.addValidationError(\"В РНН не 12 символов\");\n" +
                "end");
        rule.setId(2);
        rule.setTitle("RNN_test2");
        rules.add(rule);

        rule = new Rule("rule \"RNN_test3\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ( " +
                "           getEl(\"subject.documents.document[type=RNN].no\") != null && " +
                "           !BRMSHelper.isValidRNN((String)getEl(\"subject.documents.document[type=RNN].no\")) )\n" +
                "    then\n" +
                "        $entity.addValidationError(\"РНН не проходит проверку контрольной суммы\");\n" +
                "end");
        rule.setId(2);
        rule.setTitle("RNN_test3");
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
