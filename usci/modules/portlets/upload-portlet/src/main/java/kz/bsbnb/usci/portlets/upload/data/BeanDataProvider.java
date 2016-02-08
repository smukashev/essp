package kz.bsbnb.usci.portlets.upload.data;

import edu.emory.mathcs.backport.java.util.Arrays;
import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

  private static final String UPLOADS_PATH_CONFIG_CODE = "UPLOADS_PATH";
  private static final String DEFAULT_UPLOADS_PATH = "D:\\portal_afn\\uploads\\";
  private static final String DIGITAL_SIGNING_SETTINGS = "DIGITAL_SIGNING_SETTINGS";
  private static final String DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE = "DIGITAL_SIGNING_ORGANIZATIONS_IDS";

  private PortalUserBeanRemoteBusiness portalUserBusiness;
  private RemoteCreditorBusiness creditorBusiness;
  private IGlobalService globalService;

    /*private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private SysconfigBeanRemoteBusiness sysconfigBusiness;
    private IRemoteCreditorBusiness creditorBusiness;*/

  public BeanDataProvider() {
    initializeBeans();
  }

  private void initializeBeans() {

    RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
    portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
        ":1099/portalUserBeanRemoteBusiness");

    portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

    portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
    portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

    RmiProxyFactoryBean remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
    remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
        + ":1099/remoteCreditorBusiness");
    remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

    remoteCreditorBusinessFactoryBean.afterPropertiesSet();
    creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();

    RmiProxyFactoryBean globalServiceFactoryBean = new RmiProxyFactoryBean();
    globalServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/globalService");
    globalServiceFactoryBean.setServiceInterface(IGlobalService.class);
    globalServiceFactoryBean.afterPropertiesSet();
    globalService = (IGlobalService) globalServiceFactoryBean.getObject();
        /*
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
            sysconfigBusiness = (SysconfigBeanRemoteBusiness) context.lookup(SysconfigBeanRemoteBusiness.class.getName());
            creditorBusiness = (IRemoteCreditorBusiness) context.lookup(IRemoteCreditorBusiness.class.getName());
        } catch (NamingException ne) {
            log.log(Level.WARNING, "Naming exception: {0}", ne);
        }*/
  }

  @Override
  public String getUploadsPath() {
        /*
        if (sysconfigBusiness != null) {
            try {
                return sysconfigBusiness.getSysconfigByKey(UPLOADS_PATH_CONFIG_CODE).getValue();
            } catch (ResultInconsistentException rie) {
                log.log(Level.SEVERE, null, rie);
            } catch (ResultNotFoundException rnfe) {
                log.log(Level.SEVERE, null, rnfe);
            }
        }*/
    return DEFAULT_UPLOADS_PATH;
  }

  @Override
  public void saveFile(long userId, boolean isSigning, Creditor creditor, String filename, byte[] content, String path) throws DatabaseException {
    //InputInfoStatus status = isSigning ? InputInfoStatus.WAITING_FOR_SIGNATURE : InputInfoStatus.IN_QUEUE;
    //inputInfoBusiness.insert(userId, creditor, filename, status, path, hash(content));
  }

  private String hash(byte[] content) throws DatabaseException {
        /*
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content);
            return new BigInteger(1, digest).toString();
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.SEVERE, "", ex);
            throw new DatabaseException(ex);
        }*/
    return "123HASH";
  }

  @Override
  public List<Creditor> getUserCreditors(long userId) {
    return portalUserBusiness.getPortalUserCreditorList(userId);
    //return new ArrayList<Creditor>();
  }

  @Override
  public List<Creditor> getOrganizations() {
    return creditorBusiness.findMainOfficeCreditors();
    //return new ArrayList<Creditor>();
  }

  @Override
  public List<Integer> getIdsForOrganizationsUsingDigitalSigning() {
    ArrayList<Integer> result = new ArrayList<Integer>();
    try {
      String idsString = globalService.getValue(DIGITAL_SIGNING_SETTINGS, DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE);
      String[] ids = idsString.split(",");
      for (String id : ids) {
        result.add(Integer.parseInt(id));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public void saveOrganizationsUsingDigitalSigning(List<Creditor> creditors) {
    StringBuilder idsStringBuilder = new StringBuilder("0");
    for (Creditor creditor : creditors) {
      idsStringBuilder.append(",");
      idsStringBuilder.append(creditor.getId());
    }
    try {
      //Sysconfig config = sysconfigBusiness.getSysconfigByKey(DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE);
      globalService.update(DIGITAL_SIGNING_SETTINGS, DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE, idsStringBuilder.toString());
      //config.setValue(idsStringBuilder.toString());
      //sysconfigBusiness.update(config);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
