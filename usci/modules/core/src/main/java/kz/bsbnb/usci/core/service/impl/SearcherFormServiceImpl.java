package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ISearcherFormService;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IUserDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Bauyrzhan.Makhambeto on 19/02/2015.
 */
@Service
public class SearcherFormServiceImpl implements ISearcherFormService {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    private IUserDao userDao;

    @Override
    public List<Pair> getMetaClasses(long userId) {

        List<String> allowedMetas = userDao.getAllowedClasses(userId);
        List<Pair> ret = new ArrayList<Pair>();
        List<MetaClassName> metas = metaClassRepository.getMetaClassesNames();

        Set<String> allowedMetasSet = new HashSet<String>();

        if(allowedMetas != null)
            allowedMetasSet = new HashSet<String>(allowedMetas);

        for(MetaClassName metaClass : metas) {
            if(allowedMetas == null || allowedMetasSet.contains(metaClass.getClassName()))
                ret.add(new Pair(metaClass.getId(), metaClass.getClassName()));
        }

        return ret;
    }

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

        List<RefListItem> list = baseEntityProcessorDao.getRefsByMetaclassRaw(metaClass.getId());
        List<Long> allowedRefs = userDao.getAllowedRefs(userId, metaClass.getClassName());
        Set<Long> refSet = new HashSet<Long>(allowedRefs);
        String option;
        for(RefListItem item : list) {
            if(!refSet.contains(item.getId()))
                continue;
            option = "<option value='%d'>" + item.getTitle() + "</option>";
            option = String.format(option, item.getId());
            ret += option;
        }

        return ret + "</select></div>";
    }

    public String getDom(long userId, IMetaClass metaClass) {
        inputId.set(1L);
        return getDom(userId, metaClass, "null");
    }

    public String getDom(long userId, IMetaClass metaClass,  String attribute) {
        long id = nextId();

        String ret =
                "<div class='node'><div class='leaf'> %s : " +
                        "<input type=\"text\" id='inp-%d-%s-%s' class='inp-%d' readonly /> " +
                        "<a href='#' onclick='find(this);'>find</a>" +
                        "<div class='loading'>loading</div>" +
                        "<div class='not-filled' id = 'err-%d'>not.filled</div></div><div class='node'>";

        ret = String.format(ret, metaClass.getClassName(), id, metaClass.getClassName(), attribute, id, id);

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
                    String divSimple;
                    if( ((MetaValue)metaType).getTypeCode().equals(DataTypes.DATE))
                        divSimple = "<div class='leaf'> %s: <input type = 'date' id='inp-%d-%s-%s' />" +
                                "<div class='not-filled' id='err-%d'>not.filled</div></div>";
                    else
                        divSimple = "<div class='leaf'> %s: <input type = 'text' id='inp-%d-%s-%s' />" +
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
