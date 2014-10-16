package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dakkuliyev
 */
public class BaseEntityMergeManager implements IBaseEntityMergeManager{

    private Action action;
    private Map<MergeManagerKey, IBaseEntityMergeManager> childMap = null;

    @Override
    public Map<MergeManagerKey, IBaseEntityMergeManager> getChildMap() {
        return childMap;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public boolean containsKey(MergeManagerKey attr){
        return this.childMap.containsKey(attr);
    }

    @Override
    public Action getAction() {
        return this.action;
    }

    @Override
    public IBaseEntityMergeManager getChildManager(MergeManagerKey key) {
        return childMap.get(key);
    }

    @Override
    public void setChildManager(MergeManagerKey key, IBaseEntityMergeManager member) {
        if(childMap == null)
        {
            childMap = new HashMap<MergeManagerKey, IBaseEntityMergeManager>();
        }
        childMap.put(key, member);
    }

}
