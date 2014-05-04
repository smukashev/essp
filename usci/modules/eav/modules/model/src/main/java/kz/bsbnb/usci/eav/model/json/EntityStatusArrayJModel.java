package kz.bsbnb.usci.eav.model.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class EntityStatusArrayJModel
{
    private String type = "entity_status";

    private Long batchId;
    private Long index;

    private List<EntityStatusJModel> entityStatuses = new ArrayList<EntityStatusJModel>();

    public EntityStatusArrayJModel(Long batchId, Long index)
    {
        this.batchId = batchId;
        this.index = index;
    }

    public List<EntityStatusJModel> getEntityStatuses() {
        return entityStatuses;
    }

    public void setEntityStatuses(List<EntityStatusJModel> entityStatuses) {
        this.entityStatuses = entityStatuses;
    }

    @Override
    public String toString() {
        return "EntityStatusArrayJModel{" +
                "entityStatuses=" + entityStatuses +
                '}';
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getIndex()
    {
        return index;
    }

    public void setIndex(Long index)
    {
        this.index = index;
    }
}
