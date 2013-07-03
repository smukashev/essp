package kz.bsbnb.usci.rules;

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
import rules.model.impl.Batch;
import rules.model.impl.BatchVersion;
import rules.model.impl.Rule;
import rules.service.IBatchService;
import rules.service.IBatchVersionService;
import rules.service.IRuleService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

@Component
@Scope(value = "singleton")
public class RulesSingleton
{
    @Autowired
    private KnowledgeBase kbase;

    @Autowired
    private IBatchService remoteBatchService;
    @Autowired
    private IRuleService remoteRuleService;
    @Autowired
    private IBatchVersionService remoteBatchVersionService;


    private HashMap<String, List<BatchVersion>> packages = new HashMap<String, List<BatchVersion>>();

    public RulesSingleton()
    {

    }

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
        runRules(entity, pkgName, new Date());
    }

    public void runRules(BaseEntity entity, String pkgName, Date repDate)
    {
        StatelessKnowledgeSession ksession = getSession();

        //long versionId = getVersionId(pkgName, repDate);

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
        packages += "dialect  \"mvel\"\n";
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

    private long getVersionId(String packageName, Date packageDate)
    {
        List<BatchVersion> versions = packages.get(packageName);

        if (versions == null) {
            //TODO: Implement loading attempt if cache failed
            throw new IllegalArgumentException("Can't find package: " + packageName);
        }

        BatchVersion foundVersion = versions.get(0);

        //Assume that versions list is sorted by repDate
        for (BatchVersion version : versions)
        {
            if (version.getRepDate().compareTo(foundVersion.getRepDate()) > 0) {
                break;
            } else {
                foundVersion = version;
            }
        }

        return foundVersion.getId();
    }

    public void runRules(BaseEntity entity)
    {
        StatelessKnowledgeSession ksession = getSession();

        ksession.execute(entity);
    }

    public IRuleService getRemoteRuleService() {
        return remoteRuleService;
    }

    public void setRemoteRuleService(IRuleService remoteRuleService) {
        this.remoteRuleService = remoteRuleService;
    }

    public IBatchService getRemoteBatchService() {
        return remoteBatchService;
    }

    public void setRemoteBatchService(IBatchService remoteBatchService) {
        this.remoteBatchService = remoteBatchService;
    }

    public IBatchVersionService getRemoteBatchVersionService() {
        return remoteBatchVersionService;
    }

    public void setRemoteBatchVersionService(IBatchVersionService remoteBatchVersionService) {
        this.remoteBatchVersionService = remoteBatchVersionService;
    }
}
