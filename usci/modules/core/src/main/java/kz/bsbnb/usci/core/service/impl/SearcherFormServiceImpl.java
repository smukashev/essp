package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ISearcherFormService;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.search_form.IRoleManager;
import kz.bsbnb.usci.eav.search_form.SearchField;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bauyrzhan.Makhambeto on 19/02/2015.
 */
@Service
public class SearcherFormServiceImpl implements ISearcherFormService {

    @Autowired
    IRoleManager roleManager;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Override
    public List<Pair> getMetaClasses(long userId) {

        List<Long> allowedMetas = roleManager.getAllowedMetas(userId);
        List<Pair> ret = new ArrayList<Pair>();
        List<MetaClassName> metas = metaClassRepository.getMetaClassesNames();

        Set<Long> allowedMetasSet = new HashSet<Long>();

        if(allowedMetas != null)
            allowedMetasSet = new HashSet<Long>(allowedMetas);

        for(MetaClassName metaClass : metas) {
            if(allowedMetas == null || allowedMetasSet.contains(metaClass.getId()))
                ret.add(new Pair(metaClass.getId(), metaClass.getClassName()));
        }

        return ret;
    }

    @Override
    public List<SearchField> getFields(long userId, IMetaClass metaClass) {
        return null;
    }

    /*public String getDom(long userId, IMetaClass metaClass){
        return getDom(userId, metaClass, "credit");
    }*/


    private static final ThreadLocal<Long> inputId = new ThreadLocal<Long>() {
        @Override protected Long initialValue(){
            return 1L;
        }
    };

    private long nextId() {
        long id = inputId.get();
        inputId.set(id + 1);
        return id;
    }

    public String getDomRef(long userId, IMetaClass metaClass, String attr) {

        String ret = "<div class='leaf'> %s: <select id='ref-%d-%s-%s'>";
        ret = String.format(ret, metaClass.getClassName(), nextId(), metaClass.getClassName(), attr);

        List<RefListItem> list = baseEntityProcessorDao.getRefsByMetaclass(metaClass.getId());
        String option;
        for(RefListItem item : list) {
            option = "<option value='%d'>" + item.getTitle() + "</option>";
            option = String.format(option, item.getId());
            ret += option;
        }

        return ret + "</select></div>";
    }

    public String getDom(long userId, IMetaClass metaClass,  String attribute) {
        long id = nextId();

        String ret =
                "<div class='node'><div class='leaf'> %s : " +
                        "<input type=\"text\" id='inp-%d-%s-%s' readonly /> " +
                        "<a href='#' onclick='find(this);'>find</a>" +
                        "<div class='loading'>loading</div>" +
                        "<div class='not-filled' id = 'err-%d'>not.filled</div></div><div class='node'>";

        ret = String.format(ret, metaClass.getClassName(), id, metaClass.getClassName(), attribute, id);

        for(String attr : metaClass.getAttributeNames()) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attr);
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaAttribute.isKey()) {
                if(metaType.isReference()) {
                    ret+=getDomRef( userId, (MetaClass) metaType, attr);
                } else if (metaType.isComplex()) {
                    if(metaType.isSet() || metaType.isSetOfSets())
                        throw new NotImplementedException();
                    ret += getDom(userId, (MetaClass) metaType, attr);
                } else {
                    String divSimple = "<div class='leaf'> %s: <input type = 'text' id='inp-%d-%s-%s' />" +
                            "<div class='not-filled' id='err-%d'>not.filled</div></div>";
                    long nextId = nextId();
                    divSimple = String.format(divSimple, metaAttribute.getName(), nextId, "simple", attr , nextId);
                    ret += divSimple;
                }
            }
        }

        return ret + "</div></div>";
    }
}
