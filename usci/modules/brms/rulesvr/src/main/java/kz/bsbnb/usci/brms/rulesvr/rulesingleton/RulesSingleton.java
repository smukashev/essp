package kz.bsbnb.usci.brms.rulesvr.rulesingleton;

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

            return -(repDate.compareTo(((RuleCasheEntry)obj).getRepDate()));
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

    private ArrayList<RulePackageError> rulePackageErrors = new ArrayList<RulePackageError>();

    @Autowired(required = false)
    private IBatchService remoteRuleBatchService;
    @Autowired(required = false)
    private IRuleService remoteRuleService;
    @Autowired(required = false)
    private IBatchVersionService remoteRuleBatchVersionService;

    public StatelessKnowledgeSession getSession()
    {
        return kbase.newStatelessKnowledgeSession();
    }

    public RulesSingleton() {
        reloadCache();
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
    }

    public void reloadCache() {
        if (remoteRuleBatchService == null ||
                remoteRuleService == null ||
                remoteRuleBatchVersionService == null) {
            logger.warn("RuleServer services are null, using local cache only");
            //System.out.println("%%%%%%%%%%%%%%%%% no services wiered");
        } else {
            //System.out.println("%%%%%%%%%%%%%%%%% filling cache");
            fillPackagesCache();
        }
    }

    public void setRules(String rules)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(rules.getBytes())),
                ResourceType.DRL);

        if ( kbuilder.hasErrors() ) {
            System.out.println(kbuilder.getErrors().toString());
            throw new IllegalArgumentException( kbuilder.getErrors().toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
    }

    public String getRuleErrors(String rule)
    {
        String packages = "";
        packages += "package test \n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.brms.rulesvr.rulesingleton.BRMSHelper;\n";

        rule = packages + rule;

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(rule.getBytes())),
                ResourceType.DRL);

        if ( kbuilder.hasErrors() ) {
            return kbuilder.getErrors().toString();
        }

        return null;
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

    synchronized public void fillPackagesCache() {
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        List<Batch> allBatches = remoteRuleBatchService.getAllBatches();

        rulePackageErrors.clear();
        ruleCache.clear();

        for (Batch curBatch : allBatches) {
            if (curBatch == null) {
                throw new IllegalArgumentException("Null package recieved from service " + curBatch);
            }

            List<BatchVersion> versions = remoteRuleBatchVersionService.getBatchVersions(curBatch);

            ArrayList<RuleCasheEntry> ruleCasheEntries = new ArrayList<RuleCasheEntry>();

            for (BatchVersion curVersion : versions) {
                List<Rule> rules = remoteRuleService.load(curVersion);

                String packages = "";

                packages += "package " + curBatch.getName() + "_" + curVersion.getId() + "\n";
                packages += "dialect \"mvel\"\n";
                packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
                packages += "import kz.bsbnb.usci.brms.rulesvr.rulesingleton.BRMSHelper;\n";

                for (Rule r : rules)
                {
                    packages += r.getRule() + "\n";
                }

                logger.debug(packages);
                //System.out.println("%%%%%%%%%%%%%%%%% packages:" + packages);
                try {
                    setRules(packages);
                } catch (Exception e) {
                    rulePackageErrors.add(new RulePackageError(curBatch.getName() + "_" + curVersion.getId(),
                            e.getMessage()));
                }

                ruleCasheEntries.add(new RuleCasheEntry(curVersion.getReport_date(),
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
            if (entry.getRepDate().compareTo(repDate) <= 0)
                return entry.getRules();
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

    synchronized public void update(Long versionId, Date date, String packageName)
    {
        //System.out.println("%%%%%%%%%%%%%%%%% Update called!!!");
        rulePackageErrors.clear();

        BatchVersion curVersion = new BatchVersion();
        curVersion.setId(versionId);
        curVersion.setReport_date(date);

        List<Rule> rules = remoteRuleService.load(curVersion);

        String packages = "";

        packages += "package " + packageName + "_" + curVersion.getId() + "\n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.brms.rulesingleton.BRMSHelper;\n";

        for (Rule r : rules)
        {
            packages += r.getRule() + "\n";
        }

        logger.debug(packages);
        //System.out.println("%%%%%%%%%%%%%%%%% packages:" + packages);
        try {
            setRules(packages);
        } catch (Exception e) {
            e.printStackTrace();
            rulePackageErrors.add(new RulePackageError(packageName + "_" + curVersion.getId(),
                    e.getMessage()));
        }

        ArrayList<RuleCasheEntry> ruleCasheEntries = ruleCache.get(packageName);

        if (ruleCasheEntries != null)
        {
            boolean found = false;
            for (RuleCasheEntry entry : ruleCasheEntries)
            {
                if (entry.getRules().equals(packageName + "_" + curVersion.getId())) {
                    found = true;
                    entry.setRepDate(curVersion.getReport_date());
                    break;
                }
            }

            if(!found) {
                ruleCasheEntries.add(new RuleCasheEntry(curVersion.getReport_date(),
                        packageName + "_" + curVersion.getId()));
            }

            Collections.sort(ruleCasheEntries);
        }
    }

    public ArrayList<RulePackageError> getRulePackageErrors()
    {
        return rulePackageErrors;
    }
}