package kz.bsbnb.usci.eav.tool.generator.xml.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.tool.generator.xml.AbstractXmlGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author k.tulbassiyev
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

            if(metaType.isComplex() && metaType.isArray())
                doComplexArray(entity, metaType, document, element, name);
            else if(metaType.isComplex() && !metaType.isArray())
                doComplexValue(entity, metaType, document, element, name);
            else if(!metaType.isComplex() && metaType.isArray())
                doSimpleArray(entity, metaType, document, element, name);
            else if(!metaType.isComplex() && !metaType.isArray())
                doSimpleValue(entity, metaType, document, element, name);
        }

        parentElement.appendChild(element);
    }

    public void doComplexArray(BaseEntity entity, IMetaType metaType, Document document,
                                Element parentElement, String name)
    {
        Element arrayContainer = document.createElement(name);

        for (IBaseValue batchValue : (((BaseSet)entity.getBaseValue(name).getValue()).get()))
        {
            BaseEntity memberEntity = (BaseEntity) batchValue.getValue();

            processBaseEntity(document, memberEntity, "item", false, arrayContainer);
        }

        parentElement.appendChild(arrayContainer);
    }

    public void doComplexValue(BaseEntity entity, IMetaType metaType, Document document,
                               Element parentElement, String name)
    {
        BaseEntity memberEntity = (BaseEntity) entity.getBaseValue(name).getValue();

        processBaseEntity(document, memberEntity, name, false, parentElement);
    }

    public void doSimpleArray(BaseEntity entity, IMetaType metaType, Document document,
                                 Element parentElement, String name)
    {
        MetaSet metaSet = (MetaSet) metaType;

        Element arrayContainer = document.createElement(name);

        for (IBaseValue batchValue : (((BaseSet)entity.getBaseValue(name).getValue()).get()))
        {
            Element childElement = document.createElement("item");

            Object value = batchValue.getValue();

            childElement.appendChild(document.createTextNode(
                    metaSet.getTypeCode() == DataTypes.DATE ?
                            new SimpleDateFormat("yyyy-MM-dd").format(value)
                            : value.toString()));

            arrayContainer.appendChild(childElement);
        }

        parentElement.appendChild(arrayContainer);
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
