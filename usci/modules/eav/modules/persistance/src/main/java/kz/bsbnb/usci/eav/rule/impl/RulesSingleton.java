package kz.bsbnb.usci.eav.rule.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.rule.*;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class RulesSingleton {
    private final Logger logger = LoggerFactory.getLogger(RulesSingleton.class);

    private KnowledgeBase knowledgeBase;

    public static final DateFormat ruleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

    @Autowired
    private IPackageDao packageDao;

    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    private IMetaClassRepository metaClassRepository;

    private class RuleCacheEntry implements Comparable {
        private Date repDate;
        private String rules;

        private RuleCacheEntry(Date repDate, String rules) {
            this.repDate = repDate;
            this.rules = rules;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj == null)
                return 0;
            if (!(getClass() == obj.getClass()))
                return 0;

            return (repDate.compareTo(((RuleCacheEntry) obj).getRepDate()));
        }

        private Date getRepDate() {
            return repDate;
        }

        private void setRepDate(Date repDate) {
            this.repDate = repDate;
        }

        private String getRules() {
            return rules;
        }

        private void setRules(String rules) {
            this.rules = rules;
        }
    }

    private HashMap<String, ArrayList<RuleCacheEntry>> ruleCache = new HashMap<>();

    private ArrayList<RulePackageError> rulePackageErrors = new ArrayList<>();

    @Autowired
    private IRuleDao ruleDao;

    public StatelessKnowledgeSession getSession() {
        return knowledgeBase.newStatelessKnowledgeSession();
    }

    public RulesSingleton() {
        knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
    }

    @PostConstruct
    public void init() {
        reloadCache();
    }

    public void reloadCache() {
        fillPackagesCache();
    }

    public void setRules(String rules) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(rules.getBytes())),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
            throw new IllegalArgumentException(kbuilder.getErrors().toString());
        }

        knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    public String getRuleErrors(String rule) {
        String packages = "";
        packages += "package test \n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.eav.rule.impl.BRMSHelper;\n";

        rule = packages + rule;

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(rule.getBytes())),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            return kbuilder.getErrors().toString();
        }

        return null;
    }

    public String getPackageErrorsOnRuleUpdate(Rule rule, PackageVersion packageVersion) {
        List<Rule> rules = ruleDao.load(packageVersion);

        String packages = "";

        packages += "package test\n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.eav.rule.impl.BRMSHelper;\n";

        for (Rule r : rules) {
            if (r.getId() != rule.getId())
                packages += r.getRule() + "\n";
            else {
                packages += rule.getRule() + "\n";
            }
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(packages.getBytes())),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            return kbuilder.getErrors().toString();
        }

        return null;
    }

    private class PackageAgendaFilter implements AgendaFilter {
        private String pkgName = "";

        public PackageAgendaFilter(String pkgName) {
            this.pkgName = pkgName.trim();
        }

        @Override
        public boolean accept(org.drools.runtime.rule.Activation activation) {
            return pkgName.equals(activation.getRule().getPackageName());
        }
    }

    public void runRules(BaseEntity entity, String pkgName) {
        runRules(entity, pkgName, new Date());
    }

    synchronized public void fillPackagesCache() {
        knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        List<RulePackage> packages = packageDao.getAllPackages();

        rulePackageErrors.clear();
        ruleCache.clear();

        for (RulePackage curPackage : packages) {
            List<PackageVersion> versions = ruleDao.getPackageVersions(curPackage);

            ArrayList<RuleCacheEntry> ruleCacheEntries = new ArrayList<>();

            for (PackageVersion version : versions) {
                List<Rule> rules = ruleDao.load(version);

                StringBuilder droolPackage = new StringBuilder();

                droolPackage.append("package ").append(curPackage.getName()).append("_")
                        .append(ruleDateFormat.format(version.getReportDate())).append("\n");

                droolPackage.append("dialect \"mvel\"\n");
                droolPackage.append("import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n");
                droolPackage.append("import kz.bsbnb.usci.eav.rule.impl.BRMSHelper;\n");

                for (Rule r : rules) {
                    if (r.isActive() || true)
                        droolPackage.append(r.getRule()).append("\n");
                }

                logger.debug(droolPackage.toString());
                //System.out.println("%%%%%%%%%%%%%%%%% packages:" + packages);
                try {
                    setRules(droolPackage.toString());
                } catch (Exception e) {
                    rulePackageErrors.add(new RulePackageError(curPackage.getName() + "_" + version,
                            e.getMessage()));
                }

                ruleCacheEntries.add(new RuleCacheEntry(version.getReportDate(),
                        curPackage.getName() + "_" + version));
            }

            Collections.sort(ruleCacheEntries);
            ruleCache.put(curPackage.getName(), ruleCacheEntries);
        }
    }

    public void runRules(IBaseEntity entity, String pkgName, Date repDate) {
        StatelessKnowledgeSession kSession = getSession();
        // todo: rule
        kSession.setGlobal("baseEntityProcessorDao", baseEntityProcessorDao);
        kSession.setGlobal("metaClassRepository", metaClassRepository);

        ArrayList<RuleCacheEntry> versions = ruleCache.get(pkgName);
        String packageName = null;

        for (RuleCacheEntry version : versions) {
            if (version.getRepDate().compareTo(repDate) <= 0) {
                packageName = pkgName + "_" + ruleDateFormat.format(version.getRepDate());
            } else {
                break;
            }
        }

        if (packageName == null)
            return;

        @SuppressWarnings("rawtypes")
        List<Command> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsert(entity));
        commands.add(new FireAllRulesCommand(new PackageAgendaFilter(packageName)));
        kSession.execute(CommandFactory.newBatchExecution(commands));
    }

    public void runRules(BaseEntity entity) {
        StatelessKnowledgeSession kSession = getSession();

        kSession.execute(entity);
    }

    public String getPackageErrorsOnRuleInsert(PackageVersion packageVersion, String title, String ruleBody) {
        List<Rule> rules = ruleDao.load(packageVersion);

        String packages = "";

        packages += "package test\n";
        packages += "dialect \"mvel\"\n";
        packages += "import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n";
        packages += "import kz.bsbnb.usci.eav.rule.impl.BRMSHelper;\n";

        for (Rule r : rules)
            packages += r.getRule() + "\n";

        packages += ruleBody + "\n";

        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kBuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(packages.getBytes())), ResourceType.DRL);

        if (kBuilder.hasErrors()) {
            return kBuilder.getErrors().toString();
        }

        return null;
    }

    public String getPackageErrors(List<Rule> rules) {
        StringBuilder packages = new StringBuilder();

        packages.append("package test\n");
        packages.append("dialect \"mvel\"\n");
        packages.append("import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;\n");
        packages.append("import kz.bsbnb.usci.eav.rule.impl.BRMSHelper;\n");

        for (Rule r : rules)
            packages.append(r.getRule()).append("\n");

        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kBuilder.add(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(packages.toString().getBytes())),
                ResourceType.DRL);

        if (kBuilder.hasErrors()) {
            return kBuilder.getErrors().toString();
        }

        return null;
    }


    public String getPackageErrorsOnRuleDelete(Rule rule) {
        List<RulePackage> rulePackages = ruleDao.getRulePackages(rule);

        for (RulePackage rulePackage : rulePackages) {
            List<PackageVersion> packageVersions = ruleDao.getPackageVersions(rulePackage);

            for (PackageVersion packageVersion : packageVersions) {
                if (packageVersion.getReportDate().compareTo(rule.getOpenDate()) > 0)
                    continue;

                List<Rule> rules = ruleDao.load(packageVersion);

                for (Rule ruleInPackage : rules) {
                    if (ruleInPackage.getId() == rule.getId()) {
                        rules.iterator().remove();
                        break;
                    }
                }

                String curResult = getPackageErrors(rules);

                if (curResult != null)
                    return curResult;
            }
        }

        return null;
    }
}
