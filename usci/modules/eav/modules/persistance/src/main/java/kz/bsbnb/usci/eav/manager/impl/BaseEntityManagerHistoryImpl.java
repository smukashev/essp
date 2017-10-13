package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManagerHistory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emles on 14.09.17
 */
@Service
public class BaseEntityManagerHistoryImpl implements IBaseEntityManagerHistory {

    protected List<String> history = new ArrayList<>();

    @Override
    public List<String> getHistory() {
        return history;
    }

    @Override
    public void setHistory(List<String> history) {
        this.history = history;
    }

}



