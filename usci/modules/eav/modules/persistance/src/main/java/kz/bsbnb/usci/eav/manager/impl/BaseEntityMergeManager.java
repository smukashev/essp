package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseEntityMergeManager implements IBaseEntityMergeManager {

    private Action action;
    private Map<MergeManagerKey, List<IBaseEntityMergeManager>> childMap = null;

    @Override
    public Map<MergeManagerKey, List<IBaseEntityMergeManager>> getChildMap() {
        return childMap;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public boolean containsKey(MergeManagerKey attr) {
        return this.childMap.containsKey(attr);
    }

    @Override
    public Action getAction() {
        return this.action;
    }

    @Override
    public List<IBaseEntityMergeManager> getChildManager(MergeManagerKey key) {
        return childMap.get(key);
    }

    @Override
    public void setChildManager(MergeManagerKey key, IBaseEntityMergeManager member) {
        if (childMap == null)
            childMap = new HashMap<>();

        if (!childMap.containsKey(key)) {
            List<IBaseEntityMergeManager> value = new ArrayList<IBaseEntityMergeManager>();
            value.add(member);
            childMap.put(key, value);
        } else {
            childMap.get(key).add(member);
        }
    }
}
