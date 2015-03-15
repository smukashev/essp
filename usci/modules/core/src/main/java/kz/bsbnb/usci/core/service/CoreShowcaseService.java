package kz.bsbnb.usci.core.service;

import java.util.Date;
import java.util.List;

public interface CoreShowcaseService {
    public void start(String metaName, Long id, Date reportDate);

    void pause(Long id);

    void resume(Long id);

    void stop(Long id);

    List<Long> listLoading();
}
