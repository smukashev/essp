package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by almaz on 6/16/14.
 */
@Scope("singleton")
@Component
public class ShowCaseSingleton {

    private Queue<QueueEntry> queue = new LinkedList<QueueEntry>();
    private ArrayList<ShowCaseHolder> holders = new ArrayList<ShowCaseHolder>();

    @Autowired
    ShowCaseHolder scHolder;

    @Autowired
    protected IMetaClassRepository metaClassRepository;

    @Autowired
    SQLQueriesStats stats;


    synchronized public void enqueue(QueueEntry queueEntry){

        queue.add(queueEntry);
    }

    @PostConstruct
    private void runThread(){
        new Thread(new ShowCaseSender()).start();
    }

    private class ShowCaseSender implements Runnable{

        @Override
        public void run() {
            ShowCase showCase = new ShowCase();
            MetaClass metaClass = metaClassRepository.getMetaClass("credit");
            showCase.setName("CREDITS_TEST");
            showCase.setTableName("CREDITS_TEST");
            showCase.setMeta(metaClass);
            showCase.addField(metaClass, "subjects.person.docs", "no", "person_doc_no");
            showCase.addField(metaClass, "primary_contract", "no", "contract_no");
            showCase.addField(metaClass, "primary_contract", "date");
            showCase.addField(metaClass, "subjects.person.names", "firstname");
            showCase.addField(metaClass, "subjects.person.names", "lastname");
            scHolder.setShowCaseMeta(showCase);
            scHolder.createTables();
            addShowCaseHolder(scHolder);
            //debug
            //System.out.println("Thread run..");

            while(true){
                if(queue.size() > 0){
                    for(ShowCaseHolder holder : holders) {
                        QueueEntry queueEntry = queue.poll();
                        long t1 = System.currentTimeMillis();
                        holder.dbCarteageGenerate(queueEntry.getBaseEntityApplied());
                        long t2 = System.currentTimeMillis() - t1;
                        stats.put("showcaseGenerate(per credit)", t2);//sqlstat rmi://127.0.0.1:1099/entityService
                    }
                } else{
                    try {
                        //debug
                        //System.out.println("Sleeping...");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void addShowCaseHolder(ShowCaseHolder holder) {
            holders.add(holder);
        }
    }
}
