package kz.bsbnb.usci.core.service.form.searcher.impl;

import kz.bsbnb.usci.core.service.form.listener.IRefLoadedListener;
import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IRefProcessorDao;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KeySearcherForm implements ISearcherForm {

    private static final ThreadLocal<Long> inputId = new ThreadLocal<Long>() {
        @Override protected Long initialValue(){
            return 1L;
        }
    };

    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    private IRefProcessorDao refProcessorDao;

    @Autowired(required = false)
    private IRefLoadedListener refLoadedListener;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    ImprovedBaseEntitySearcher searcher;

    private long nextId() {
        long id = inputId.get();
        inputId.set(id + 1);
        return id;
    }

    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        inputId.set(1L);
        return getDom(userId, metaClass, "null", prefix);
    }

    public String getDom(long userId, IMetaClass metaClass,  String attribute, String prefix) {
        long id = nextId();

        String ret =
                "<div class='node'><div class='leaf'> %s : " +
                        "<input type=\"text\" id='%sinp-%d-%s-%s' class='inp-%d' readonly /> " +
                        "<a href='#' onclick='find(this);'>найти</a>" +
                        "<div class='loading'>загрузка</div>" +
                        "<div class='not-filled' id = '%serr-%d'>не заполнено</div></div><div class='node'>";

        ret = String.format(ret, ((MetaClass)metaClass).getClassTitle(), prefix, id, metaClass.getClassName(), attribute, id, prefix, id);

        for(String attr : metaClass.getAttributeNames()) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attr);
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaAttribute.isKey()) {
                if(metaType.isReference()) {
                    ret+=getDomRef( userId, (MetaClass) metaType, attr);
                } else if (metaType.isComplex()) {
                    if(metaType.isSetOfSets())
                        throw new NotImplementedException();
                    if(metaType.isSet()) {
                        IMetaType childMeta = ((MetaSet) metaType).getMemberType();
                        ret += getDom(userId, (MetaClass) childMeta, attr, prefix);
                    } else {
                        ret += getDom(userId, (MetaClass) metaType, attr, prefix);
                    }
                } else {
                    String divSimple;
                    long nextId = nextId();

                    if( ((MetaValue)metaType).getTypeCode().equals(DataTypes.DATE)) {
                        divSimple = "<div class='leaf'> <div id='%sinp-%d-%s-%s' class='usci-date' ></div>" +
                                "<div class='not-filled' id='err-%d'>not.filled</div></div>";
                        divSimple = String.format(divSimple, prefix, nextId, "simple", attr, nextId);
                    }
                    else {
                        divSimple = "<div class='leaf'> %s: <input type = 'text' id='inp-%d-%s-%s' />" +
                                "<div class='not-filled' id='%serr-%d'>не заполнено</div></div>";
                        divSimple = String.format(divSimple, metaAttribute.getTitle(), nextId, "simple", attr, prefix, nextId);
                    }
                    ret += divSimple;
                }
            }
        }

        return ret + "</div></div>";
    }

    public String getDomRef(long userId, IMetaClass metaClass, String attr) {
        String ret = "<div class='leaf'> %s: <select id='ref-%d-%s-%s'>";
        ret = String.format(ret, ((MetaClass)metaClass).getClassTitle(), nextId(), metaClass.getClassName(), attr);

        List<RefListItem> list = refProcessorDao.getRefsByMetaclassRaw(metaClass.getId());
        if(refLoadedListener !=null)
            refLoadedListener.process(userId, metaClass, attr, list);

        String option;

        for(RefListItem item : list) {
            option = "<option value='%d'>" + item.getTitle() + "</option>";
            option = String.format(option, item.getId());
            ret += option;
        }

        return ret + "</select></div>";
    }

    @Override
    public List<Pair> getMetaClasses(long userId) {

        List<Pair> ret = new ArrayList<>();
        List<MetaClassName> metas = metaClassRepository.getMetaClassesNames();

        for(MetaClassName metaClass : metas) {
                ret.add(new Pair(metaClass.getId(), metaClass.getClassName(), metaClass.getClassTitle()));
        }
        return ret;
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix) {
        BaseEntity baseEntity = new BaseEntity(metaClass, new Date());
        ISearchResult ret = new NonPaginableSearchResult();
        Iterator<String> it = parameters.keySet().iterator();
        while(it.hasNext()) {
            String attribute = it.next();

            Object value;
            String parameterValue = parameters.get(attribute);

            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
            if(metaAttribute == null)
                continue;
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaType.isSetOfSets())
                throw new UnsupportedOperationException("Not yet implemented");

            Batch b = new Batch(new Date(), 100500L);
            b.setId(100500L);

            if(metaType.isSet()) {
                BaseSet childBaseSet = new BaseSet(metaType);
                IMetaType itemMeta = ((MetaSet) metaType).getMemberType();
                BaseEntity childBaseEntity = new BaseEntity((MetaClass) itemMeta, new Date());
                childBaseEntity.setId(Long.valueOf(parameterValue));

                childBaseSet.put(BaseValueFactory.create(
                        BaseContainerType.BASE_SET,
                        itemMeta,
                        0,
                        /* TODO set creditorId */ 0,
                        new Date(),
                        childBaseEntity,
                        false,
                        true
                ));
                value = childBaseSet;
            } else if(metaType.isComplex()) {
                BaseEntity childBaseEntity = new BaseEntity((MetaClass) metaType, new Date());
                childBaseEntity.setId(Long.valueOf(parameterValue));
                value = childBaseEntity;

            } else {
                MetaValue metaValue = (MetaValue) metaType;
                value = DataTypes.fromString(metaValue.getTypeCode(), parameterValue);
            }

            baseEntity.put(attribute, BaseValueFactory.create(
                    BaseContainerType.BASE_ENTITY,
                    metaAttribute.getMetaType(),
                    0,
                    /* TODO set creditorId */ 0,
                    new Date(),
                    value,
                    false,
                    true
                ));
        }

        Long id = searcher.findSingle(baseEntity, /* TODO set creditorId */ 0);
        baseEntity.setId(id);
        ret.setData(Arrays.asList(new BaseEntity[]{baseEntity}));
        return ret;
    }
}
