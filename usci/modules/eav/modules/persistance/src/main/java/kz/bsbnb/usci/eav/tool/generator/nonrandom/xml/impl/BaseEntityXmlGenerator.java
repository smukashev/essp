package kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.impl;


import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.xml.AbstractXmlGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author abukabayev
 */
public class BaseEntityXmlGenerator extends AbstractXmlGenerator
{
    public Document getGeneratedDocument(List<BaseEntity> baseEntities) {
        Document document = getDocument();
        Element batchElement = document.createElement("batch");
        document.appendChild(batchElement);
        Element entitiesElement = document.createElement("entities");

        for (BaseEntity baseEntity : baseEntities)
            processBaseEntity(document, baseEntity, "entity", true, entitiesElement, null);

        batchElement.appendChild(entitiesElement);

        return document;
    }

    public Document getGeneratedDeleteDocument(List<BaseEntity> baseEntities) {
        Document document = getDocument();
        Element batchElement = document.createElement("batch");
        document.appendChild(batchElement);
        Element entitiesElement = document.createElement("entities");

        for (BaseEntity baseEntity : baseEntities)
            processBaseEntity(document, baseEntity, "entity", true, entitiesElement, "delete");

        batchElement.appendChild(entitiesElement);

        return document;
    }

    private void processBaseEntity(Document document, BaseEntity entity, String nameInParent,
                                   boolean firstTime, Element parentElement, String operation) {
        MetaClass meta = entity.getMeta();
        Element element = document.createElement(nameInParent);

        if(firstTime)
            element.setAttribute("class", entity.getMeta().getClassName());

        if(operation != null && operation.length() > 0)
            element.setAttribute("operation", "delete");

        for (String name : meta.getMemberNames()) {
            IMetaType metaType = meta.getMemberType(name);

            if(metaType.isComplex()) {
                if (metaType.isSet()) {
                    BaseValue baseValue = (BaseValue)entity.safeGetValue(name);
                    doComplexSet(baseValue, document, element, name);
                } else {
                    BaseValue baseValue = (BaseValue)entity.safeGetValue(name);
                    doComplexValue(baseValue, document, element, name);
                }
            }

            if(!metaType.isComplex()) {
                if (metaType.isSet()) {
                    BaseValue baseValue = (BaseValue)entity.safeGetValue(name);
                    doSimpleSet(baseValue, metaType, document, element, name);
                } else {
                    BaseValue baseValue = (BaseValue)entity.safeGetValue(name);
                    doSimpleValue(baseValue, metaType, document, element, name);
                }
            }
        }

        parentElement.appendChild(element);
    }

    public void doComplexValue(BaseValue baseValue, Document document,
                               Element parentElement, String name) {
        if (baseValue != null) {
            BaseEntity memberEntity = (BaseEntity)baseValue.getValue();
            processBaseEntity(document, memberEntity, name, false, parentElement, null);
        }
    }

    public void doSimpleSet(BaseValue baseValue, IMetaType metaType, Document document,
                              Element parentElement, String name) {
        MetaSet metaSet = (MetaSet) metaType;
        Element childElement = document.createElement(name);

        if (baseValue != null) {
            BaseSet baseSet = (BaseSet)baseValue.getValue();

            for (IBaseValue value : baseSet.get()) {
                doSimpleValue((BaseValue)value, metaSet.getMemberType(), document, childElement, "item");
            }

            parentElement.appendChild(childElement);
        }
    }

    public void doComplexSet(BaseValue baseValue, Document document,
                            Element parentElement, String name) {
        Element childElement = document.createElement(name);

        if (baseValue != null) {
            BaseSet baseSet = (BaseSet)baseValue.getValue();

            for (IBaseValue value : baseSet.get()) {
                doComplexValue((BaseValue)value, document, childElement, "item");
            }

            parentElement.appendChild(childElement);
        }
    }

    public void doSimpleValue(BaseValue baseValue, IMetaType metaType, Document document,
                              Element parentElement, String name) {
        MetaValue metaValue = (MetaValue) metaType;
        Element childElement = document.createElement(name);

        if (baseValue != null) {
            Object value = baseValue.getValue();
            childElement.appendChild(document.createTextNode(
                    metaValue.getTypeCode() == DataTypes.DATE ?
                            new SimpleDateFormat("dd.MM.yyyy").format(value)
                            : value.toString()));

            if(baseValue.getNewBaseValue() != null){
                Object newValue = baseValue.getNewBaseValue().getValue();
                childElement.setAttribute("operation","new");
                childElement.setAttribute("data",metaValue.getTypeCode() == DataTypes.DATE ?
                        new SimpleDateFormat("dd.MM.yyyy").format(newValue) : newValue.toString());
            }

            parentElement.appendChild(childElement);
        }
    }
}