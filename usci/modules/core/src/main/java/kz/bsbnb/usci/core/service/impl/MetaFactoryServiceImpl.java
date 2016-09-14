package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.bconv.xsd.XSDGenerator;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;
import org.apache.xbean.spring.generator.XsdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService {
    private Logger logger = Logger.getLogger(MetaFactoryServiceImpl.class);
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    IMetaClassDao metaClassDao;
    @Autowired
    XSDGenerator xsdGenerator;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Override
    public List<MetaClass> getMetaClasses() {
        return metaClassRepository.getMetaClasses();
    }

    @Override
    public List<MetaClassName> getMetaClassesNames() {
        return metaClassRepository.getMetaClassesNames();
    }

    @Override
    public List<MetaClassName> getRefNames() {
        return metaClassRepository.getRefNames();
    }

    @Override
    public MetaClass getMetaClass(String name) {
        return metaClassRepository.getMetaClass(name);
    }

    @Override
    public MetaClass getDisabledMetaClass(String name) {
        return metaClassRepository.getDisabledMetaClass(name);
    }

    @Override
    public MetaClass getMetaClass(Long metaId) {
        return metaClassRepository.getMetaClass(metaId);
    }

    @Override
    public boolean saveMetaClass(MetaClass meta)  {
        metaClassRepository.saveMetaClass(meta);
        FileOutputStream fileOutputStream = null;
        try {
            List<MetaClass> metaClasses = metaClassRepository.getMetaClasses();
            fileOutputStream = new FileOutputStream(StaticRouter.getXSDSourceFilePath());
            xsdGenerator.generate(fileOutputStream, metaClasses);
            fileOutputStream = new FileOutputStream(StaticRouter.getXSDTargetFilePath());
            xsdGenerator.generate(fileOutputStream, metaClasses);
        }
        catch (FileNotFoundException e) {
            logger.error(Errors.getError(Errors.E294)+"-"+StaticRouter.getXSDSourceFilePath());
        }finally {
            if(fileOutputStream!=null) {
                try {
                    fileOutputStream.close();
                }
                catch (IOException e) {

                }
            }
        }
        return true;
    }

    @Override
    public boolean delMetaClass(String className) {
        return metaClassRepository.delMetaClass(className);
    }

}
