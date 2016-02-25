package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.ArrayList;
import java.util.List;

public class ChildShowCase extends Persistable {
    private ShowCase showCase;

    private String name;

    private MetaClass meta;

    private String childDownPath = "";

    private List<ChildShowCaseField> fields = new ArrayList<>();

    private List<ChildShowCaseField> keyFields = new ArrayList<>();

    public ShowCase getShowCase() {
        return showCase;
    }

    public void setShowCase(ShowCase showCase) {
        this.showCase = showCase;
    }

    public String getChildDownPath() {
        return childDownPath;
    }

    public void setChildDownPath(String childDownPath) {
        this.childDownPath = childDownPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetaClass getMeta() {
        return meta;
    }

    public void setMeta(MetaClass meta) {
        this.meta = meta;
    }

    public List<ChildShowCaseField> getFields() {
        return fields;
    }

    public List<ChildShowCaseField> getKeyFields() {
        return keyFields;
    }

    public void addField(ChildShowCaseField field) {
        fields.add(field);
    }

    public void addKeyField(ChildShowCaseField field) {
        keyFields.add(field);
    }

    public void addField(String name) {
        if (meta == null)
            throw new IllegalStateException("MetaClass is not available!");

        IMetaAttribute metaAttribute = meta.getElAttribute(name);

        if (metaAttribute == null)
            throw new IllegalStateException("Attribute " + name + " is not available in " + meta.getClassName());

        ChildShowCaseField field = new ChildShowCaseField();
        field.setAttributeId(metaAttribute.getId());
        field.setAttributePath(name);
        field.setColumnName(metaAttribute.getName());
        field.setType(ChildShowCaseField.ChildShowCaseFieldTypes.DEFAULT);

        addField(field);
    }

    public void addKeyField(String name) {
        ChildShowCaseField field = new ChildShowCaseField();
        field.setAttributeId(0L);
        field.setAttributePath(name);
        field.setColumnName(name);
        field.setType(ChildShowCaseField.ChildShowCaseFieldTypes.KEY);

        addKeyField(field);
    }

    @Override
    public String toString() {
        return "ChildShowCase{" +
                "showCase=" + showCase +
                ", name='" + name + '\'' +
                ", meta=" + meta +
                ", childDownPath='" + childDownPath + '\'' +
                ", fields=" + fields +
                ", keyFields=" + keyFields +
                '}';
    }
}
