package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.creditregistry.dm.maintenance.InputInfo;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.creditregistry.dm.ref.shared.InputInfoStatus;
import com.bsbnb.creditregistry.dm.ref.shared.SharedType;
import com.bsbnb.creditregistry.ejb.api.maintenance.InputInfoBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.SysconfigBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteCreditorBusiness;
import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private SysconfigBeanRemoteBusiness sysconfigBeanRemoteBusiness;
    private IRemoteCreditorBusiness creditorBusiness;
    private IRemoteSharedBusiness sharedBusiness;

    /*
     * Constructor to be called by background thread
     */
    public BeanDataProvider() {
        Properties props = new Properties();
        props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
        props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3800");
        try {
            InitialContext context = new InitialContext(props);
            portalUserBusiness = (PortalUserBeanRemoteBusiness) context.lookup(PortalUserBeanRemoteBusiness.class.getName());
            inputInfoBusiness = (InputInfoBeanRemoteBusiness) context.lookup(InputInfoBeanRemoteBusiness.class.getName());
            sysconfigBeanRemoteBusiness = (SysconfigBeanRemoteBusiness) context.lookup(SysconfigBeanRemoteBusiness.class.getName());
            creditorBusiness = (IRemoteCreditorBusiness) context.lookup(IRemoteCreditorBusiness.class.getName());
            sharedBusiness = (IRemoteSharedBusiness) context.lookup(IRemoteSharedBusiness.class.getName());
        } catch (NamingException ne) {
            log.log(Level.SEVERE, "Exception occured while initializing beans", ne);
        }
    }

    @Override
    public List<Creditor> getCreditors(long userId, boolean isUserAdmin) {
        if (!isUserAdmin) {
            return portalUserBusiness.getPortalUserCreditorList(userId);
        } else {
            return creditorBusiness.findMainOfficeCreditors();
        }
    }

    @Override
    public List<QueueFileInfo> getQueue(List<Creditor> creditors) {
        Set<String> creditorNames = new HashSet<String>();
        if (creditors != null) {
            for (Creditor creditor : creditors) {
                creditorNames.add(creditor.getName());
            }
        }
        List queueList = inputInfoBusiness.getQueueWithProtocolCount();
        List<QueueFileInfo> queue = new ArrayList<QueueFileInfo>();
        for (Object queueObject : queueList) {
            Object[] values = (Object[]) queueObject;
            QueueFileInfo file = new QueueFileInfo();
            int counter = 0;
            file.setRownum(((BigDecimal) values[counter++]).intValue());
            file.setInputInfoId(((BigDecimal) values[counter++]).intValue());
            file.setUserId(((BigDecimal) values[counter++]).intValue());
            file.setProtocolCount(((BigDecimal) values[counter++]).intValue());
            file.setCreditorId(((BigDecimal) values[counter++]).intValue());
            file.setCreditorName((String) values[counter++]);
            file.setStatusCode((String) values[counter++]);
            file.setStatus((String) values[counter++]);
            file.setFilePath((String) values[counter++]);
            file.setReceiverDate((Date) values[counter++]);
            file.setFilename((String) values[counter++]);
            queue.add(file);
        }
        if (creditors == null) {
            return queue;
        } else {
            List<QueueFileInfo> filteredQueue = new ArrayList<QueueFileInfo>();
            for (QueueFileInfo info : queue) {
                if (creditorNames.contains(info.getCreditorName())) {
                    filteredQueue.add(info);
                }
            }
            return filteredQueue;
        }
    }

    @Override
    public Sysconfig getConfig(String key) throws ConfigurationException {
        try {
            return sysconfigBeanRemoteBusiness.getSysconfigByKey(key);
        } catch (ResultInconsistentException rie) {
            throw new ConfigurationException("Result inconsistent", rie);
        } catch (ResultNotFoundException rnfe) {
            throw new ConfigurationException("Key not found", rnfe);
        }
    }

    @Override
    public void saveConfig(Sysconfig config) throws ConfigurationException {
        sysconfigBeanRemoteBusiness.update(config);
    }

    @Override
    public void rejectInputInfo(int inputInfoId) throws InputInfoNotInQueueException {
        InputInfo inputInfo = inputInfoBusiness.getBy_Id(BigInteger.valueOf(inputInfoId));
        if (!InputInfoStatus.IN_QUEUE.getCode().equals(inputInfo.getStatus().getCode())) {
            throw new InputInfoNotInQueueException();
        }
        try {
            inputInfo.setStatus(sharedBusiness.findByC_T(InputInfoStatus.REJECTED.getCode(), SharedType.INPUT_INFO_STATUS.getType()));
            inputInfoBusiness.update(inputInfo);
        } catch (ResultInconsistentException rie) {
            log.log(Level.SEVERE, null, rie);
        } catch (ResultNotFoundException rnfe) {
            log.log(Level.SEVERE, null, rnfe);
        }
    }
}
