package kz.bsbnb.usci.brms.rulesingleton.test.errormock;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MockRuleServiceWithError implements IRuleService
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
                "        System.out.println(\"Test rule #1\");\n" +
                "end");
        rule.setId(1);
        rule.setTitle("Rule 1");
        rules.add(rule);

        rule = new Rule("rule \"test2\"\n" +
                "    when\n" +
                "        $entity : BaseEntity (getEl(\"subject.zzz\").toString() == \"\")\n" +
                "    then\n" +
                "        System.out.println(\"Test rule #2\");\n" +
                "end");
        rule.setId(1);
        rule.setTitle("Rule 2");
        rules.add(rule);

        rule = new Rule("rule \"test3\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ()\n" +
                "    then\n" +
                "        System.out.println(\"Test rule #3\");\n" +
                "end");
        rule.setId(1);
        rule.setTitle("Rule 3");
        rules.add(rule);

        rule = new Rule("rule \"test4\"\n" +
                "    when\n" +
                "        $entity : BaseEntity ()\n" +
                "    then\n" +
                "        System.out.println(\"Test rule #4\");\n" +
                "end");
        rule.setId(1);
        rule.setTitle("Rule 4");
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

    @Override
    public Map getRuleTitles(Long packageId, Date repDate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteRule(long ruleId, long batchVersionId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long saveEmptyRule(String title, long batchVersionId)
    {
        return 0;
    }

    @Override
    public void updateBody(Long ruleId, String body)
    {

    }

    @Override
    public void copyExistingRule(long ruleId, long batchVersionId)
    {

    }

    @Override
    public long createCopy(long ruleId, String title, long batchVersionId)
    {
        return 0;
    }

    @Override
    public long createNewRuleInBatch(Rule rule, BatchVersion batchVersion)
    {
        return 0;
    }

    @Override
    public Rule getRule(Long ruleId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
//    public long saveRule(long ruleId, long batchVersionId) {
//        return 0;  //To change body of implemented methods use File | Settings | File Templates.
//    }

}
