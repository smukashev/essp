package kz.bsbnb.usci.portlet.report.data;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
        import com.liferay.portal.model.User;
        import kz.bsbnb.usci.cr.model.Creditor;
        import kz.bsbnb.usci.cr.model.InputFile;
        import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
        import java.math.BigInteger;
        import java.util.Date;
        import java.util.List;
        import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
    public List<Creditor> getCreditorsList(User user);
    public List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate);
    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo);
    //public Map<String,List<ProtocolDisplayBean>> getProtocolsByInputInfoGroupedByContractNo(InputInfoDisplayBean inputInfo);
    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo);
    public InputFile getFileByInputInfo(InputInfoDisplayBean inputInfo);
    public BatchFullJModel getBatchFullModel(BigInteger batchId);
}
