package kz.bsbnb.usci.brms.rulesingleton;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component
@Scope(value = "singleton")
public class RulesSingleton
{
    Logger logger = LoggerFactory.getLogger(RulesSingleton.class);

    private KnowledgeBase kbase;

    private class RuleCasheEntry implements Comparable {
        private Date repDate;
        private String rules;

        private RuleCasheEntry(Date repDate, String rules)
        {
            this.repDate = repDate;
            this.rules = rules;
        }

        @Override
        public int compareTo(Object obj)
        {
            if (obj == null)
                return 0;
            if (!(getClass() == obj.getClass()))
                return 0;

            return repDate.compareTo(((RuleCasheEntry)obj).getRepDate());
        }

        private Date getRepDate()
        {
            return repDate;
        }

        private void setRepDate(Date repDate)
        {
            this.repDate = repDate;
        }

        private String getRules()
        {
            return rules;
        }

        private void setRules(String rules)
        {
            this.rules = rules;
        }
    }

    private HashMap<String, ArrayList<RuleCasheEntry>> ruleCache = new HashMap<String, ArrayList<RuleCasheEntry>>();

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

    public RulesSingleton() {
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
    }

    public void reloadCache() {
        if (remoteBatchService == null ||
                remoteRuleService == null ||
                remoteBatchVersionService == null) {
            logger.warn("RuleServer services are null, using local cache only");
        } else {
            fillPackagesCache();
        }
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

    public void fillPackagesCache() {
        List<Batch> allBatches = remoteBatchService.getAllBatches();

        for (Batch curBatch : allBatches) {
            if (curBatch == null) {
                throw new IllegalArgumentException("Null package recieved from service " + curBatch);
            }

            List<BatchVersion> versions = remoteBatchVersionService.getBatchVersions(curBatch);

            ArrayList<RuleCasheEntry> ruleCasheEntries = new ArrayList<RuleCasheEntry>();

            for (BatchVersion curVersion : versions) {
                List<Rule> rules = remoteRuleService.load(curVersion);

                String packages = "";

                packages += "package " + curBatch.getName() + "_" + curVersion.getId() + "\n";
                packages += "dialect \"mvel\"\n";
                packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";

                for (Rule r : rules)
                {
                    packages += r.getRule() + "\n";
                }

                logger.debug(packages);

                setRules(packages);

                ruleCasheEntries.add(new RuleCasheEntry(curVersion.getRepDate(),
                        curBatch.getName() + "_" + curVersion.getId()));
            }

            Collections.sort(ruleCasheEntries);
            ruleCache.put(curBatch.getName(), ruleCasheEntries);
        }
    }

    public String getRulePackageName(String pkgName, Date repDate)
    {
        List<RuleCasheEntry> versions = ruleCache.get(pkgName);

        if (versions == null)
            throw new IllegalArgumentException("No such package " + pkgName);
        if (versions.size() < 1)
            throw new IllegalArgumentException("Package " + pkgName + " has no versions information!");

        RuleCasheEntry result = versions.get(0);
        for (RuleCasheEntry entry : versions)
        {
            if (entry.getRepDate().compareTo(result.getRepDate()) > 0)
                return result.getRules();
            result = entry;
        }

        return result.getRules();
    }

    public void runRules(BaseEntity entity, String pkgName, Date repDate)
    {
        StatelessKnowledgeSession ksession = getSession();

        @SuppressWarnings("rawtypes")
        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(entity));
        commands.add(new FireAllRulesCommand(new PackageAgendaFilter(
                getRulePackageName(pkgName, repDate))));
        ksession.execute(CommandFactory.newBatchExecution(commands));
    }

    public void runRules(BaseEntity entity)
    {
        StatelessKnowledgeSession ksession = getSession();

        ksession.execute(entity);
    }
}
