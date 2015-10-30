package kz.bsbnb.usci.eav.comparator.impl;

import kz.bsbnb.usci.eav.comparator.IBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

@Component
public class BasicBaseEntityComparator implements IBaseEntityComparator {
    Logger logger = Logger.getLogger(BasicBaseEntityComparator.class);

    private boolean compareValue(IMetaType type, IBaseValue value1, IBaseValue value2) {
        boolean res;

        if (!type.isComplex()) {
            res = (value1.getValue() == null ?
                    value2.getValue() == null :
                    value1.getValue().equals(value2.getValue()));
        } else {
            res = compare((BaseEntity) value1.getValue(),
                    (BaseEntity) value2.getValue());
        }

        return res;
    }

    private boolean compareSet(IMetaType type, IBaseValue value1, IBaseValue value2) {
        BaseSet set1 = (BaseSet) value1.getValue();
        BaseSet set2 = (BaseSet) value2.getValue();

        if (set1 == null && set2 == null)
            return true;

        if (set1 == null)
            return false;

        if (set2 == null)
            return false;

        Collection<IBaseValue> ar1 = set1.get();
        Collection<IBaseValue> ar2 = set2.get();

        if (ar1.size() != ar2.size())
            return false;

        boolean res = (((MetaSet) type).getArrayKeyType() == ComplexKeyTypes.ALL);

        for (IBaseValue v1 : ar1) {
            if (!type.isComplex()) {
                if (((MetaSet) type).getArrayKeyType() == ComplexKeyTypes.ALL)
                    res = res && ar2.contains(v1);
                else
                    res = res || ar2.contains(v1);
            } else {
                boolean found = false;

                if (v1.getValue() != null) {
                    for (IBaseValue v2 : ar2) {
                        if (compare((BaseEntity) v1.getValue(), (BaseEntity) v2.getValue())) {
                            found = true;
                            break;
                        }
                    }


                    if (((MetaSet) type).getArrayKeyType() == ComplexKeyTypes.ALL)
                        res = res && found;
                    else
                        res = res || found;
                }
            }
        }

        return res;
    }

    @Override
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException {
        if (!c1.getMeta().equals(c2.getMeta()))
            return false;

        if (!c1.getMeta().isSearchable() || !c2.getMeta().isSearchable())
            return false;

        if (c1.getId() > 0 && c2.getId() > 0 && c1.getId() == c2.getId())
            return true;

        MetaClass meta = c1.getMeta();

        boolean result = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);

        Set<String> names = meta.getMemberNames();

        for (String name : names) {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            if (!attribute.isKey())
                continue;

            IBaseValue value1 = c1.safeGetValue(name);
            IBaseValue value2 = c2.safeGetValue(name);

            if (value1 == null || value2 == null)
                throw new IllegalArgumentException("Ключевой атрибут (" + name + ") не может быть пустым;");

            if (meta.getComplexKeyType() == ComplexKeyTypes.ALL) {
                if (!type.isSet())
                    result = result && compareValue(type, value1, value2);
                else
                    result = result && compareSet(type, value1, value2);
            } else {
                if (!type.isSet())
                    result = result || compareValue(type, value1, value2);
                else
                    result = result || compareSet(type, value1, value2);
            }
        }

        return result;
    }

    public List<String> findBaseEntity(BaseEntity entity1, BaseEntity c2, MetaClass type) {
        ArrayList<String> paths = new ArrayList<>();

        List<String> subClasses = c2.getMeta().getAllPaths(type);

        Iterator<String> i = subClasses.iterator();

        while (i.hasNext()) {
            String path = i.next();

            logger.debug("Path: " + path);

            /*if (c2.getMeta().arrayInPath(path))
                continue;*/

            IMetaType innerMetaType = c2.getMeta().getEl(path);

            List<Object> childValues = c2.getElWithArrays(path);

            for (Object currentChildValue : childValues) {
                if (!innerMetaType.isSet()) {
                    logger.debug("Is not set");
                    BaseEntity entity2 = null;
                    try {
                        entity2 = (BaseEntity) currentChildValue;
                    } catch (ClassCastException e) {
                        System.out.println("============= " + path);
                        e.printStackTrace();
                    }

                    if (entity1 != null && entity2 != null && compare(entity1, entity2)) {
                        paths.add(path);
                        logger.debug("Is equal");
                    }
                    //paths.addAll(intersect(entity1, c2));
                } else {
                    logger.debug("Is set");
                    MetaSet innerSet = (MetaSet) innerMetaType;

                    if (!innerSet.getMemberType().isSet()) {
                        BaseSet set2 = (BaseSet) currentChildValue;

                        if (set2 == null) {
                            continue;
                        }

                        for (String identifier : set2.getAttributes()) {
                            IBaseValue value2 = set2.getBaseValue(identifier);
                            if (value2 != null) {
                                BaseEntity entity2 = (BaseEntity) value2.getValue();

                                if (entity1 != null && entity2 != null && compare(entity1, entity2)) {
                                    paths.add(path + "[" + identifier + "]");
                                }
                            }
                        }

                        //paths.addAll(intersect(entity1, c2));
                    } else {
                        throw new IllegalStateException("Unimplemented");
                    }
                }
            }
        }

        return paths;
    }

    public List<String> intersect(BaseEntity c1, BaseEntity c2) throws IllegalStateException {
        ArrayList<String> paths = new ArrayList<>();

        if (c1 == null)
            return paths;

        MetaClass meta = c1.getMeta();

        Set<String> names = meta.getMemberNames();

        for (String name : names) {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Testing attribute: " + name);

            if (!type.isComplex()) {
                continue;
            }

            if (attribute.isImmutable()) {
                continue;
            }

            if (!type.isSet()) {
                IBaseValue value1 = c1.safeGetValue(name);
                if (value1 != null) {
                    BaseEntity entity1 = (BaseEntity) (value1.getValue());

                    paths.addAll(findBaseEntity(entity1, c2, (MetaClass) type));
                    paths.addAll(intersect(entity1, c2));
                }
            } else {
                MetaSet set = (MetaSet) type;

                if (!set.getMemberType().isSet()) {
                    IBaseValue value1 = c1.safeGetValue(name);
                    if (value1 != null) {
                        BaseSet bSet = (BaseSet) value1.getValue();

                        if (bSet == null) {
                            continue;
                        }

                        for (IBaseValue value11 : bSet.get()) {
                            if (value1 != null) {
                                BaseEntity entity1 = (BaseEntity) (value11.getValue());

                                paths.addAll(findBaseEntity(entity1, c2, (MetaClass) (set.getMemberType())));
                                paths.addAll(intersect(entity1, c2));
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("Unimplemented");
                }
            }
        }

        return paths;
    }

    public boolean hasBaseEntity(BaseEntity entity1, BaseEntity c2, MetaClass type) {
        List<String> subClasses = c2.getMeta().getAllPaths(type);

        Iterator<String> i = subClasses.iterator();

        while (i.hasNext()) {
            String path = i.next();

            logger.debug("Path: " + path);

            /*if (c2.getMeta().arrayInPath(path))
                continue;*/

            IMetaType innerMetaType = c2.getMeta().getEl(path);

            List<Object> childValues = c2.getElWithArrays(path);

            for (Object currentChildValue : childValues) {
                if (!innerMetaType.isSet()) {
                    logger.debug("Is not set");
                    BaseEntity entity2 = null;
                    try {
                        entity2 = (BaseEntity) currentChildValue;
                    } catch (ClassCastException e) {
                        System.out.println("============= " + path);
                        e.printStackTrace();
                    }

                    if (entity1 != null && entity2 != null && compare(entity1, entity2)) {
                        return true;
                    }
                    //paths.addAll(intersect(entity1, c2));
                } else {
                    logger.debug("Is set");
                    MetaSet innerSet = (MetaSet) innerMetaType;

                    if (!innerSet.getMemberType().isSet()) {
                        BaseSet set2 = (BaseSet) currentChildValue;

                        if (set2 == null) {
                            continue;
                        }

                        for (String identifier : set2.getAttributes()) {
                            IBaseValue value2 = set2.getBaseValue(identifier);
                            if (value2 != null) {
                                BaseEntity entity2 = (BaseEntity) value2.getValue();

                                if (entity1 != null && entity2 != null && compare(entity1, entity2)) {
                                    return true;
                                }
                            }
                        }

                        //paths.addAll(intersect(entity1, c2));
                    } else {
                        throw new IllegalStateException("Unimplemented");
                    }
                }
            }
        }

        return false;
    }

    public boolean hasIntersect(BaseEntity c1, BaseEntity c2) throws IllegalStateException {
        if (c1 == null)
            return false;

        MetaClass meta = c1.getMeta();

        Set<String> names = meta.getMemberNames();

        for (String name : names) {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Testing attribute: " + name);

            if (!type.isComplex()) {
                continue;
            }

            if (attribute.isImmutable()) {
                continue;
            }

            if (!type.isSet()) {
                IBaseValue value1 = c1.safeGetValue(name);
                if (value1 != null) {
                    BaseEntity entity1 = (BaseEntity) (value1.getValue());

                    if (hasBaseEntity(entity1, c2, (MetaClass) type)) {
                        return true;
                    }
                    if (hasIntersect(entity1, c2)) {
                        return true;
                    }
                }
            } else {
                MetaSet set = (MetaSet) type;

                if (!set.getMemberType().isSet()) {
                    IBaseValue value1 = c1.safeGetValue(name);
                    if (value1 != null) {
                        BaseSet bSet = (BaseSet) value1.getValue();

                        if (bSet == null) {
                            continue;
                        }

                        for (IBaseValue value11 : bSet.get()) {
                            if (value1 != null) {
                                BaseEntity entity1 = (BaseEntity) (value11.getValue());

                                if (hasBaseEntity(entity1, c2, (MetaClass) (set.getMemberType()))) {
                                    return true;
                                }
                                if (hasIntersect(entity1, c2)) {
                                    return true;
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("Unimplemented");
                }
            }
        }

        return false;
    }
}
