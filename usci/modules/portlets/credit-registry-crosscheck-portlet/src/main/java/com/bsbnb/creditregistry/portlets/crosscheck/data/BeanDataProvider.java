package com.bsbnb.creditregistry.portlets.crosscheck.data;

import com.bsbnb.creditregistry.portlets.crosscheck.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.crosscheck.api.CrossCheckBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.api.CrossCheckMessageBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.api.PortalUserBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.api.ReportBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.impl.CrossCheckBean;
import com.bsbnb.creditregistry.portlets.crosscheck.impl.CrossCheckMessageBean;
import com.bsbnb.creditregistry.portlets.crosscheck.impl.PortalUserBean;
import com.bsbnb.creditregistry.portlets.crosscheck.impl.ReportBean;
import com.bsbnb.creditregistry.portlets.crosscheck.model.Creditor;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheck;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheckMessage;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static com.bsbnb.creditregistry.portlets.crosscheck.CrossCheckApplication.log;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private CrossCheckBeanRemoteBusiness crossCheckBusiness;
    private CrossCheckMessageBeanRemoteBusiness messageBusiness;
    private ReportBeanRemoteBusiness reportBusiness;
    private PortletEnvironmentFacade facade;

    InitialContext contextInit = null;

    private InitialContext getInitialContext() {
        /*
         Properties props = new Properties();
         props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
         props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
         props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
         props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
         props.setProperty("org.omg.CORBA.ORBInitialPort", "3800");
         InitialContext initialContext = null;
         try {
         initialContext = new InitialContext(props);

         } catch (NamingException ne) {
         log.log(Level.WARNING, "", ne);
         }
         return initialContext;
         */

        //Тестовый контекст
        if (contextInit != null)
            return contextInit;

        try {
            contextInit = new InitialContext();
            contextInit.bind(PortalUserBeanRemoteBusiness.class.getName(), new PortalUserBean());
            contextInit.bind(CrossCheckBeanRemoteBusiness.class.getName(), new CrossCheckBean());
            contextInit.bind(CrossCheckMessageBeanRemoteBusiness.class.getName(), new CrossCheckMessageBean());
            contextInit.bind(ReportBeanRemoteBusiness.class.getName(), new ReportBean());
            return contextInit;
        } catch (NamingException ne) {
            try {
                contextInit = new InitialContext();
                contextInit.rebind(PortalUserBeanRemoteBusiness.class.getName(), new PortalUserBean());
                contextInit.rebind(CrossCheckBeanRemoteBusiness.class.getName(), new CrossCheckBean());
                contextInit.rebind(CrossCheckMessageBeanRemoteBusiness.class.getName(), new CrossCheckMessageBean());
                contextInit.rebind(ReportBeanRemoteBusiness.class.getName(), new ReportBean());
                return contextInit;
            } catch (NamingException e) {
                log.log(Level.SEVERE, "", e);
            }
        }
        return null;
    }

    public static String namingExceptionString(NamingException ne) {
        StringBuilder sb = new StringBuilder("Naming exception: \n");
        sb.append("Message: ").append(ne.getMessage()).append("\n");
        sb.append("Explanation: ").append(ne.getExplanation()).append("\n");
        sb.append("Remaining name: ").append(ne.getRemainingName()).append("\n");
        sb.append("Resolved name: ").append(ne.getResolvedName()).append("\n");
        sb.append("Resolved object: ").append(ne.getResolvedObj()).append("\n");
        sb.append("Cause: ").append(ne.getCause() == null ? "" : ne.getCause().getMessage()).append("\n");
        return sb.toString();
    }

    public BeanDataProvider(PortletEnvironmentFacade facade) throws DataException {
        this.facade = facade;
        InitialContext context = getInitialContext();
        if (context != null) {
            StringBuilder exceptionString = new StringBuilder();
            try {
                portalUserBusiness = (PortalUserBeanRemoteBusiness) context.lookup(PortalUserBeanRemoteBusiness.class.getName());
            } catch (NamingException ne) {
                exceptionString.append(namingExceptionString(ne));
            }
            try {
                crossCheckBusiness = (CrossCheckBeanRemoteBusiness) context.lookup(CrossCheckBeanRemoteBusiness.class.getName());
            } catch (NamingException ne) {
                exceptionString.append(namingExceptionString(ne));
            }
            try {
                messageBusiness = (CrossCheckMessageBeanRemoteBusiness) context.lookup(CrossCheckMessageBeanRemoteBusiness.class.getName());
            } catch (NamingException ne) {
                exceptionString.append(namingExceptionString(ne));
            }

            try {
                reportBusiness = (ReportBeanRemoteBusiness) context.lookup(ReportBeanRemoteBusiness.class.getName());
            } catch (NamingException ne) {
                exceptionString.append(namingExceptionString(ne));
            }
            if (exceptionString.length() > 0) {
                throw new DataException(exceptionString.toString());
            }
        } else {
            throw new DataException("Context is null");
        }
    }

    @Override
    public List<Creditor> getCreditorsList() {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(facade.getUserID());
    }

    @Override
    public List<CrossCheck> getCrossChecks(Creditor[] creditors, Date date) {
        List<BigInteger> ids = new ArrayList<BigInteger>();
        for (Creditor creditor : creditors) {
            ids.add(creditor.getId());
        }
        return crossCheckBusiness.loadCrossCheck(ids, date);
    }

    public List<CrossCheckMessageDisplayWrapper> getMessages(CrossCheck crossCheck) {
        List<CrossCheckMessageDisplayWrapper> result = new ArrayList<CrossCheckMessageDisplayWrapper>();
        for (CrossCheckMessage message : messageBusiness.getMessagesByCrossCheck(crossCheck)) {
            result.add(new CrossCheckMessageDisplayWrapper(message));
        }
        return result;
    }

    public Date getCreditorsReportDate(Creditor creditor) {
        return reportBusiness.getReportDate(creditor.getId());
    }
}
