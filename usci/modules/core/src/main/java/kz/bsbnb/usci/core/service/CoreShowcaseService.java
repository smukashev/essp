package kz.bsbnb.usci.core.service;

import java.util.Date;
import java.util.List;

/**
 * Created by almaz on 6/27/14.
 */
public interface CoreShowcaseService {
    public void start(String metaName, Long id, Date reportDate);

    void pause(Long id);

    void resume(Long id);

    void stop(Long id);

    List<Long> listLoading();

    void startLoadHistory(boolean populate);

    void stopHistory();
}
