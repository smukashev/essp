package kz.bsbnb.usci.portlet.report.dm;

import com.liferay.portal.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import com.liferay.portal.util.PortalUtil;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import org.jooq.tools.Convert;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportController {

    private EntityManagerFactory emf;
    public static final Logger log = Logger.getLogger(ReportApplication.class.getCanonicalName());

    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("report-portlet2PU");
        }
        return emf.createEntityManager();
    }

    public List<Report> loadReports(DatabaseConnect connect) {
            List<Report> reports =  new ArrayList<Report>();
        List<ReportInputParameter> inputParameters = new ArrayList<ReportInputParameter>();
        List<ExportType> exportTypes = new ArrayList<ExportType>();
        Connection conn  = connect.getConnection();
        Statement stmt = null;
        Statement stmt2 = null;
        Statement stmt3 = null;
        String query;
        if(!ReportApplication.getReportType().equals("BANKS"))
        query = "SELECT r.* FROM Report r WHERE r.type = '"+ReportApplication.getReportType()+"' and r.id in (case when r.type='REPORT' then (select uu.report_id from report_user uu where uu.report_id=r.id and uu.user_id="+connect.getUserId()+") else r.id end) order by r.order_number, r.id";
        else
            query = "SELECT r.* FROM Report r WHERE r.type IN ('"+ReportApplication.getReportType()+"', 'REFERENCEBANKS') and r.id in (case when r.type='REPORT' then (select uu.report_id from report_user uu where uu.report_id=r.id and uu.user_id="+connect.getUserId()+") else r.id end) order by r.order_number, r.id";
        try
        {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next())
            {
                Report rep = new Report();
                rep.setId(rs.getLong("ID"));
                rep.setNameRu(rs.getString("NAME_RU"));
                rep.setNameKz(rs.getString("NAME_KZ"));
                rep.setName(rs.getString("NAME"));
                rep.setProcedureName(rs.getString("PROCEDURE_NAME"));
                rep.setType(rs.getString("TYPE"));
                rep.setOrderNumber(rs.getInt("ORDER_NUMBER"));
                String query2 = "select r.* from report_input_parameter r where r.report_id="+rs.getLong("ID");
                try
                {   stmt2 = conn.createStatement();
                    ResultSet rs2 = stmt2.executeQuery(query2);
                    if (rs2!=null) {
                        inputParameters.clear();
                        while (rs2.next()) {

                            ReportInputParameter input = new ReportInputParameter();
                            input.setId(rs2.getLong("ID"));
                            input.setType(rs2.getString("TYPE"));
                            input.setProcedureName(rs2.getString("PROCEDURE_NAME"));
                            input.setMaximum(rs2.getInt("MAXIMUM"));
                            input.setMinimum(rs2.getInt("MINIMUM"));
                            input.setNameRu(rs2.getString("NAME_RU"));
                            input.setNameKz(rs2.getString("NAME_KZ"));
                            input.setOrderNumber(rs2.getInt("ORDER_NUMBER"));
                            input.setParameterName("NAME");
                            if(rs2.getString("TYPE")!=null) {
                                input.setParameterType(ParameterType.fromString(rs2.getString("TYPE")));
                            }
                            input.setReport(rep);
                            inputParameters.add(input);
                        }
                        rep.setInputParameters(inputParameters);

                    }
                }
                catch (SQLException e){
                    e.printStackTrace();
                    log.log(Level.WARNING, "REPORT_INPUT_PARAMETER: ", e);
                    throw e;
                }
                String query3 = "select ep.* from reporter.report_export_type rep, reporter.export_type ep    where rep.export_type_id=ep.id and rep.report_id="+rs.getLong("ID");
                try
                {
                    stmt3 = conn.createStatement();
                    ResultSet rs3 = stmt3.executeQuery(query3);
                    if(rs3!=null) {
                        exportTypes.clear();
                        while (rs3.next()) {

                            ExportType exportType = new ExportType();
                            exportType.setId(rs3.getBigDecimal("ID"));
                            exportType.setName(rs3.getString("NAME"));
                            exportTypes.add(exportType);
                        }
                        rep.setExportTypeList(exportTypes);

                    }
                }
                catch(SQLException e)
                {e.printStackTrace();}

                reports.add(rep);

            }

        }
        catch (SQLException e){  e.printStackTrace(); }
        finally
        {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
        }

        /*EntityManager em = getEntityManager();

        try {
            em.clear();
            TypedQuery<Report> q = em.createQuery("SELECT r FROM Report r WHERE r.type like :type order by r.orderNumber, r.id", Report.class);
            ReportApplication.log.log(Level.INFO, ReportApplication.getReportType());
            q.setParameter("type", '%' + ReportApplication.getReportType() + '%');
            List<Report> reports = q.getResultList();
            return reports;
        } finally {
            em.close();
        }*/
        return reports;
    }

    public List<ReportLoad> loadUsersLoads(long portalUserId) {
        EntityManager em = getEntityManager();
        try {
            em.clear();
            TypedQuery<ReportLoad> q = em.createQuery("SELECT rl FROM ReportLoad rl WHERE rl.portalUserId = :userId ORDER BY rl.startTime desc", ReportLoad.class);
            q.setParameter("userId", portalUserId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public void insertOrUpdateReportLoad(ReportLoad reportLoad) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            if (reportLoad.getId() == null) {
                em.persist(reportLoad);
            } else {
                ReportLoad databaseLoad = em.find(ReportLoad.class, reportLoad.getId());
                List<ReportLoadFile> newFiles = reportLoad.getFiles();
                List<ReportLoadFile> filesToUpdate = new ArrayList<ReportLoadFile>();
                for (ReportLoadFile file : newFiles) {
                    ReportLoadFile fileInDatabase = em.find(ReportLoadFile.class, file.getId());
                    filesToUpdate.add(fileInDatabase == null ? file : fileInDatabase);
                }
                databaseLoad.setFiles(filesToUpdate);
                databaseLoad.setFinishTime(reportLoad.getFinishTime());
                databaseLoad.setNote(reportLoad.getNote());
                databaseLoad.setPortalUserId(reportLoad.getPortalUserId());
                databaseLoad.setReport(em.find(Report.class, reportLoad.getReport().getId()));
                databaseLoad.setStartTime(reportLoad.getStartTime());
            }
            transaction.commit();
        } finally {
            em.close();

        }
    }

    public List<ReportLoad> loadUsersLoads(List<User> coworkers) {
        EntityManager em = getEntityManager();
        try {
            List<Long> userIds = new ArrayList<Long>(coworkers.size());
            for(User coworker : coworkers) {
                userIds.add(coworker.getUserId());
            }
            em.clear();
            TypedQuery<ReportLoad> q = em.createQuery("SELECT rl FROM ReportLoad rl WHERE rl.portalUserId in :userIds ORDER BY rl.startTime desc", ReportLoad.class);
            q.setParameter("userIds", userIds);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
