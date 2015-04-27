package kz.bsbnb.usci.core.service;

import java.util.Date;
import java.util.List;
import java.util.Queue;

public interface CoreShowcaseService {
    void start(String metaName, Long id, Date reportDate);

    void pause(Long id);

    void resume(Long id);

    void stop(Long id);

    List<Long> listLoading();

    void startLoadHistory(boolean populate, Queue<Long> creditorIds);

    void stopHistory();
}
