package kz.bsbnb.usci.eav.showcase;


import kz.bsbnb.ddlutils.model.Index;
import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.ArrayList;
import java.util.List;

public class ShowCase extends Persistable {
    private String name;
    private String tableName;
    private MetaClass meta;
    private String downPath = "";
    private boolean isFinal = false;

    private ArrayList<ShowCaseField> fields = new ArrayList<>();
    private ArrayList<ShowCaseField> customFields = new ArrayList<>();
    private ArrayList<ShowCaseField> keyFields = new ArrayList<>();
    private ArrayList<Index> indexes = new ArrayList<>();

    public ShowCase() {
        super();
    }

    public MetaClass getMeta() {
        return meta;
    }

    public void setMeta(MetaClass meta) {
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDownPath() {
        return downPath;
    }

    public void setDownPath(String downPath) {
        this.downPath = downPath;
    }

    public void addField(ShowCaseField field) {
        fields.add(field);
    }

    public void addCustomField(ShowCaseField field) {
        customFields.add(field);
    }

    public void addKeyField(ShowCaseField field) {
        keyFields.add(field);
    }

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public ArrayList<Index> getIndexes() {
        return indexes;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public MetaClass getActualMeta() {
        if (downPath == null || downPath.equals(""))
            return meta;
        if (meta.getElAttribute(downPath).getMetaType().isSet())
            return (MetaClass) ((MetaSet) meta.getEl(downPath)).getMemberType();
        return (MetaClass) meta.getEl(downPath);
    }

    public void addField(String attributePath, String columnName) {
        if (meta == null)
            throw new IllegalArgumentException(String.valueOf(Errors.E52));

        IMetaAttribute attr = getActualMeta().getElAttribute(attributePath);

        if (attr == null)
            throw new IllegalArgumentException(Errors.E51+"|"+getName() + "|" + attributePath);

        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setMetaId(this.getActualMeta().getId());
        showCaseField.setAttributeId(attr.getId());
        showCaseField.setAttributePath(attributePath);
        showCaseField.setColumnName(columnName);

        addField(showCaseField);
    }

    public void addCustomField(String attributePath, String columnName, MetaClass customMeta) {
        if (customMeta == null)
            throw new IllegalArgumentException(String.valueOf(Errors.E50));

        IMetaAttribute attr = null;
        if (!attributePath.equals("root"))
            attr = customMeta.getElAttribute(attributePath);

        if (attr == null && !attributePath.equals("root"))
            throw new IllegalArgumentException(Errors.E51 + "|" + getName() + "|" + attributePath);

        ShowCaseField showCaseField = new ShowCaseField();

        if (attr != null)
            showCaseField.setAttributeId(attr.getId());
        else
            showCaseField.setAttributeId(0L);

        showCaseField.setMetaId(customMeta.getId());
        showCaseField.setAttributePath(attributePath);
        showCaseField.setColumnName(columnName);
        showCaseField.setType(ShowCaseField.ShowCaseFieldTypes.CUSTOM);

        customFields.add(showCaseField);
    }

    public void addKeyField(String columnName) {
        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setAttributeId(0L);
        showCaseField.setMetaId(0L);
        showCaseField.setAttributePath(columnName);
        showCaseField.setColumnName(columnName);
        showCaseField.setType(ShowCaseField.ShowCaseFieldTypes.KEY);

        addKeyField(showCaseField);
    }

    public List<ShowCaseField> getKeyFieldsList() {
        return keyFields;
    }

    public ArrayList<ShowCaseField> getCustomFieldsList() {
        return customFields;
    }

    public List<ShowCaseField> getFieldsList() {
        return fields;
    }

    @Override
    public String toString() {
        return "ShowCase{" +
                "name='" + name + '\'' +
                ", tableName='" + tableName + '\'' +
                ", downPath='" + downPath + '\'' +
                ", isFinal=" + isFinal +
                ", customMeta=" + (meta != null ? meta.getClassName() : null) +
                '}';
    }
}
