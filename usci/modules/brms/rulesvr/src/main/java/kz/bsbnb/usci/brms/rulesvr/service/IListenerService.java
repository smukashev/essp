package kz.bsbnb.usci.brms.rulesvr.service;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IListenerService {
    public void update(Long versionId,Date date,String packageName);
}
