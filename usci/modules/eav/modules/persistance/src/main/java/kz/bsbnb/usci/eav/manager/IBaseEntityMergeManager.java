package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;

import java.util.Map;

public interface IBaseEntityMergeManager {
    enum Action {
        KEEP_RIGHT,
        KEEP_LEFT,
        TO_MERGE,
        KEEP_BOTH
    }

    Action getAction();

    void setAction(Action action);

    Map<MergeManagerKey, IBaseEntityMergeManager> getChildMap();

    IBaseEntityMergeManager getChildManager(MergeManagerKey key);

    void setChildManager(MergeManagerKey key, IBaseEntityMergeManager member);

    boolean containsKey(MergeManagerKey key);
}
