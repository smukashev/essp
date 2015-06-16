package kz.bsbnb.usci.brms.rulesvr.rulesingleton;

import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
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

import javax.annotation.PostConstruct;
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

    @Autowired
    private IBatchService ruleBatchService;
    @Autowired
    private IRuleDao ruleDao;
    @Autowired
    private IBatchVersionService ruleBatchVersionService;

    public StatelessKnowledgeSession getSession()
    {
        return kbase.newStatelessKnowledgeSession();
    }

    public RulesSingleton() {
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
    }

    @PostConstruct
    public void init(){
        reloadCache();
    }

    public void reloadCache() {
            fillPackagesCache();
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

    public String getPackageErrorsOnRuleUpdate(String ruleBody, long ruleId,
                                               String pkgName, Date repDate,
                                               boolean makeActive,
                                               boolean makeInActive,
                                               boolean ruleEdited)
    {

        if(makeActive && makeInActive)
            throw new IllegalArgumentException("non proper method call");

        if(!makeActive && ruleEdited)
            throw new IllegalArgumentException("non proper method call");

        BatchVersion batchVersion = ruleBatchVersionService.getBatchVersion(pkgName, repDate);
        if(batchVersion == null)
            return "Версия пакета правил остутвует на текущую дату";

        List<Rule> rules = ruleDao.load(batchVersion);

        String packages = "";

        packages += "package test\n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.brms.rulesvr.rulesingleton.BRMSHelper;\n";

        for (Rule r : rules)
        {
            if(!r.isActive() && !(r.getId() == ruleId && makeActive))
                continue;

            if(r.getId() == ruleId && makeInActive)
                continue;

            if(r.getId() != ruleId)
                packages += r.getRule() + "\n";
            else {
                packages += ruleEdited ? ruleBody : r.getRule()+ "\n";
            }
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(packages.getBytes())),
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
        List<Batch> allBatches = ruleBatchService.getAllBatches();

        rulePackageErrors.clear();
        ruleCache.clear();

        for (Batch curBatch : allBatches) {
            if (curBatch == null) {
                throw new IllegalArgumentException("Null package recieved from service " + curBatch);
            }

            List<BatchVersion> versions = ruleBatchVersionService.getBatchVersions(curBatch);

            ArrayList<RuleCasheEntry> ruleCasheEntries = new ArrayList<RuleCasheEntry>();

            for (BatchVersion curVersion : versions) {
                List<Rule> rules = ruleDao.load(curVersion);

                String packages = "";

                packages += "package " + curBatch.getName() + "_" + curVersion.getId() + "\n";
                packages += "dialect \"mvel\"\n";
                packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
                packages += "import kz.bsbnb.usci.brms.rulesvr.rulesingleton.BRMSHelper;\n";

                for (Rule r : rules)
                {
                    if(r.isActive())
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
}
