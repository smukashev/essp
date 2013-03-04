package kz.bsbnb.usci.batch.writer.impl;

import kz.bsbnb.usci.batch.writer.AbstractWriter;
import kz.bsbnb.usci.sync.service.IDataService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class RmiEventEntityWriter<T> implements AbstractWriter<T>
{
    IDataService dataService;

    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    private Logger logger = Logger.getLogger(RmiEventEntityWriter.class);

    @PostConstruct
    public void init()
    {
        dataService = (IDataService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public void write(List items) throws Exception
    {
        long t1 = System.currentTimeMillis();
        dataService.process(items);
        long t2 = System.currentTimeMillis() - t1;

        logger.info("[batch][service][data]               :               " + t2);
    }
}
