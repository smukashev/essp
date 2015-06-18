package kz.bsbnb.usci.brms.rulesvr;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RMISecurityManager;

/**
 * @author abukabayev
 */
public class Main {
    public static void main(String args[]){
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");


        System.out.println("RMI server started");

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
            System.out.println("Security manager installed.");
        } else {
            System.out.println("Security manager already exists.");
        }


//        BatchDao batchDao = (BatchDao)context.getBean("BatchDao");
//
//        System.out.println(batchDao.getAllBatches().size());
//        for (Batch s : batchDao.getAllBatches()){
//            System.out.println(s.getName()+" "+s.getId());
//        }
//        BatchVersionDao batchVersionDao = (BatchVersionDao)context.getBean("BatchVersionDao");
//        RuleDao ruleDao = (RuleDao)context.getBean("RuleDao");
//
//
//
//        if (batchDao.testConnection()){
//
//            Batch batch = batchDao.loadBatch(1L);
//            List<BatchVersion> batchVersionList = batchVersionDao.getBatchVersions(batch);
//
//            for (BatchVersion b : batchVersionList)
//            System.out.println(b.getId()+" "+b.getPackageId()+" "+b.getRepDate());
//
//            BatchVersion bb = batchVersionDao.getBatchVersion(batch);
//            System.out.println(bb.getId());
//
////            ruleDao.save(new Rule("second rule"),bb);
//
//            List<Rule> ruleList = ruleDao.load(bb);
//
//            for (Rule r:ruleList)
//                System.out.println(r.getRule());

//        }else{
//            System.out.println("Could not connect to database!");
//        }
    }
}
