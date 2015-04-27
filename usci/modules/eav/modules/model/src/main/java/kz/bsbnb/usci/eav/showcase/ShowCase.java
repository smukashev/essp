package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.ArrayList;
import java.util.List;

public class ShowCase extends Persistable {
    private String name;
    private String tableName;
    private String title;
    private MetaClass meta;
    private String downPath = "";
    private boolean isFinal = false;

    private ArrayList<ShowCaseField> fields = new ArrayList<ShowCaseField>();
    private ArrayList<ShowCaseField> customFields = new ArrayList<ShowCaseField>();
    private ArrayList<ShowCaseField> filterFields = new ArrayList<ShowCaseField>();

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownPath() {
        return downPath;
    }

    public void setDownPath(String downPath) {
        if (downPath == null)
            this.downPath = "";
        else
            this.downPath = downPath;
    }

    public void addField(ShowCaseField field) {
        fields.add(field);
    }

    public void addCustomField(ShowCaseField field) {
        customFields.add(field);
    }

    public void addFilterField(ShowCaseField field) {
        filterFields.add(field);
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public MetaClass getActualMeta() {
        if (downPath.equals(""))
            return meta;
        if (meta.getElAttribute(downPath).getMetaType().isSet())
            return (MetaClass) ((MetaSet) meta.getEl(downPath)).getMemberType();
        return (MetaClass) meta.getEl(downPath);
    }

    public void addField(String path, String name) {
        if (meta == null)
            throw new IllegalArgumentException("meta not set for showcase");

        addField(path, name, name);
    }

    public void addField(String path, String name, String columnName) {
        if (meta == null)
            throw new IllegalArgumentException("meta not set for showcase");

        IMetaAttribute attr = getActualMeta().getElAttribute(path + "." + name);

        if (attr == null)
            throw new IllegalArgumentException(getName() + ": Can't get attribute: " + path + "." + name);

        IMetaType metaType = attr.getMetaType();

        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setAttributeId(attr.getId());
        showCaseField.setName(columnName);
        showCaseField.setAttributePath(path);
        showCaseField.setAttributeName(name);
        showCaseField.setColumnName(columnName);
        showCaseField.setTitle(attr.getTitle());

        if (metaType.isComplex()) {
            showCaseField.setAttributePath(path.equals("") ? name : path + "." + name);
            showCaseField.setAttributeName("");
        }

        addField(showCaseField);
    }

    public void addCustomField(String path, String name, String columnName, MetaClass meta) {
        if (meta == null)
            throw new IllegalArgumentException("meta can't be null");

        if(!name.equals("root")) {
            IMetaAttribute metaAttribute = meta.getElAttribute(path + "." + name);

            if (metaAttribute == null)
                throw new IllegalArgumentException("Can't get attribute: " + path + "." + name);

            IMetaType metaType = metaAttribute.getMetaType();

            ShowCaseField showCaseField = new ShowCaseField();
            showCaseField.setAttributeId(metaAttribute.getId());
            showCaseField.setName(columnName);
            showCaseField.setAttributePath(path);
            showCaseField.setAttributeName(name);
            showCaseField.setColumnName(columnName);
            showCaseField.setTitle(metaAttribute.getTitle());
            showCaseField.setType(ShowCaseField.ShowCaseFieldTypes.CUSTOM);

            if (metaType.isComplex()) {
                showCaseField.setAttributePath(path.equals("") ? name : path + "." + name);
                showCaseField.setAttributeName("");
            }

            customFields.add(showCaseField);
        } else {
            ShowCaseField showCaseField = new ShowCaseField();
            showCaseField.setAttributeId(meta.getId());
            showCaseField.setName(columnName);
            showCaseField.setAttributePath("ROOT");
            showCaseField.setAttributeName(name);
            showCaseField.setColumnName(columnName);
            showCaseField.setTitle("ROOT_ID");
            showCaseField.setType(ShowCaseField.ShowCaseFieldTypes.CUSTOM);

            customFields.add(showCaseField);
        }
    }

    public void addFilterField(String path, String name) {
        if (meta == null)
            throw new IllegalArgumentException("meta not set for showcase");

        IMetaAttribute attr = getActualMeta().getElAttribute(path + "." + name);

        if (attr == null)
            throw new IllegalArgumentException(getName() + ": Can't get attribute: " + path + "." + name);

        IMetaType metaType = attr.getMetaType();

        ShowCaseField showCaseField = new ShowCaseField();
        showCaseField.setAttributeId(attr.getId());
        showCaseField.setName(name);
        showCaseField.setAttributePath(path);
        showCaseField.setAttributeName(name);
        showCaseField.setColumnName(name);
        showCaseField.setTitle(attr.getTitle());
        showCaseField.setType(ShowCaseField.ShowCaseFieldTypes.FILTER);

        if (metaType.isComplex()) {
            showCaseField.setAttributePath(path.equals("") ? name : path + "." + name);
            showCaseField.setAttributeName("");
        }

        filterFields.add(showCaseField);
    }

    public ArrayList<ShowCaseField> getCustomFieldsList() {
        return customFields;
    }

    public ArrayList<ShowCaseField> getFilterFieldsList() {
        return filterFields;
    }

    public List<ShowCaseField> getFieldsList() {
        return fields;
    }

    @Override
    public String toString() {
        return "ShowCase{" +
                "name='" + name + '\'' +
                ", tableName='" + tableName + '\'' +
                ", title='" + title + '\'' +
                ", downPath='" + downPath + '\'' +
                ", isFinal=" + isFinal +
                ", meta=" + (meta != null ? meta.getClassName() : null) +
                '}';
    }
}
