package kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.xml.impl;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.xml.AbstractXmlGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author abukabayev
 */
public class BaseEntityXmlGenerator extends AbstractXmlGenerator
{
    public Document getGeneratedDocument(List<BaseEntity> baseEntities)
    {
        Document document = getDocument();

        Element batchElement = document.createElement("batch");

        document.appendChild(batchElement);

        Element entitiesElement = document.createElement("entities");

        for (BaseEntity baseEntity : baseEntities)
            processBaseEntity(document, baseEntity, "entity", true, entitiesElement);

        batchElement.appendChild(entitiesElement);

        return document;
    }

    private void processBaseEntity(Document document, BaseEntity entity, String nameInParent,
                                   boolean firstTime, Element parentElement)
    {
        MetaClass meta = entity.getMeta();

        Element element = document.createElement(nameInParent);

        if(firstTime)
            element.setAttribute("class", entity.getMeta().getClassName());

        for (String name : meta.getMemberNames())
        {

            IMetaType metaType = meta.getMemberType(name);

            if(metaType.isComplex() && !metaType.isSet())
                doComplexValue(entity, metaType, document, element, name);
            if(!metaType.isComplex() && !metaType.isSet())
                doSimpleValue(entity, metaType, document, element, name);
        }

        parentElement.appendChild(element);
    }

    public void doComplexValue(BaseEntity entity, IMetaType metaType, Document document,
                               Element parentElement, String name)
    {
        BaseEntity memberEntity = (BaseEntity) entity.getBaseValue(name).getValue();

        processBaseEntity(document, memberEntity, name, false, parentElement);
    }

    public void doSimpleValue(BaseEntity entity, IMetaType metaType, Document document,
                              Element parentElement, String name)
    {
        MetaValue metaValue = (MetaValue) metaType;

        Element childElement = document.createElement(name);

        Object value = entity.getBaseValue(name).getValue();

        childElement.appendChild(document.createTextNode(
                metaValue.getTypeCode() == DataTypes.DATE ?
                        new SimpleDateFormat("yyyy-MM-dd").format(value)
                        : value.toString()));

        parentElement.appendChild(childElement);
    }
}