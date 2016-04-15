package kz.bsbnb.usci.core.service;
import java.util.Date;

public interface IListenerService {
    void update(Long versionId, Date date, String packageName);
}
