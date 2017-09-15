package kz.bsbnb.usci.eav.manager;

import java.util.List;

/**
 * Created by emles on 14.09.17
 */
public interface IBaseEntityManagerHistory {

    List<String> getHistory();

    void setHistory(List<String> history);

}



