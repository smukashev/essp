package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;
import java.util.Map;

/**
 * @author dakkuliyev
 */
public interface IBaseEntityMergeManager{

    enum Action
    {
        KEEP_RIGHT,
        KEEP_LEFT,
        TO_MERGE,
        KEEP_BOTH
    }

    public Action getAction();
    public void setAction(Action action);

    public Map<MergeManagerKey, IBaseEntityMergeManager> getChildMap();

    public IBaseEntityMergeManager getChildManager(MergeManagerKey key);

    public void setChildManager(MergeManagerKey key, IBaseEntityMergeManager member);

    public boolean containsKey(MergeManagerKey key);
}
