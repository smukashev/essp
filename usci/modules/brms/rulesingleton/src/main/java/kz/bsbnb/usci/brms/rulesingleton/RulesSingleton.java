package kz.bsbnb.usci.brms.rulesingleton;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import org.drools.KnowledgeBase;
import org.drools.StatelessSession;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.spi.Activation;
import org.drools.runtime.rule.AgendaFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Scope(value = "singleton")
public class RulesSingleton
{
    @Autowired
    private KnowledgeBase kbase;

    @Autowired(required = false)
    private IBatchService remoteBatchService;
    @Autowired(required = false)
    private IRuleService remoteRuleService;
    @Autowired(required = false)
    private IBatchVersionService remoteBatchVersionService;

    public StatelessKnowledgeSession getSession()
    {
        return kbase.newStatelessKnowledgeSession();
    }

    public KnowledgeBase getKbase()
    {
        return kbase;
    }

    public void setKbase(KnowledgeBase kbase)
    {
        this.kbase = kbase;
    }

    public void setRules(String rules)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(rules.getBytes())),
                ResourceType.DRL);

        if ( kbuilder.hasErrors() ) {
            throw new IllegalArgumentException( kbuilder.getErrors().toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
    }

    private class PackageAgendaFilter implements AgendaFilter
    {
        private String pkgName = "";

        public PackageAgendaFilter(String pkgName){
            this.pkgName = pkgName.trim();
        }

        @Override
        public boolean accept(org.drools.runtime.rule.Activation activation)
        {
            return pkgName.equals(activation.getRule().getPackageName());
        }
    }

    public void runRules(BaseEntity entity, String pkgName)
    {
        StatelessKnowledgeSession ksession = getSession();

        @SuppressWarnings("rawtypes")
        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(entity));
        commands.add(new FireAllRulesCommand(new PackageAgendaFilter(pkgName)));
        ksession.execute(CommandFactory.newBatchExecution(commands));
    }

    public void runRules(BaseEntity entity, String pkgName, Date repDate)
    {
        StatelessKnowledgeSession ksession = getSession();

        List<Batch> b = remoteBatchService.getAllBatches();

        Batch batch = null;

        for (Batch bb : b) {
            if (bb.getName().equals(pkgName)) {
                batch = bb;
                break;
            }
        }

        if (batch == null) {
            throw new IllegalArgumentException("No such package " + pkgName);
        }

        BatchVersion version = remoteBatchVersionService.load(batch, repDate);
        List<Rule> rules = remoteRuleService.load(version);

        String packages = "";

        packages += "package " + pkgName + "_" + version.getId() + "\n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";

        for (Rule r : rules)
        {
            packages += r.getRule();
        }

        System.out.println("###");
        System.out.println(packages);

        setRules(packages);

        @SuppressWarnings("rawtypes")
        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(entity));
        commands.add(new FireAllRulesCommand(new PackageAgendaFilter(pkgName + "_" + version.getId())));
        ksession.execute(CommandFactory.newBatchExecution(commands));
    }

    public void runRules(BaseEntity entity)
    {
        StatelessKnowledgeSession ksession = getSession();

        ksession.execute(entity);
    }
}
