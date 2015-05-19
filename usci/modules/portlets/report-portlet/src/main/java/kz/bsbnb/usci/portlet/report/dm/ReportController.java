package kz.bsbnb.usci.portlet.report.dm;

import com.liferay.portal.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import kz.bsbnb.usci.portlet.report.ReportApplication;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportController {

    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("report-portlet2PU");
        }
        return emf.createEntityManager();
    }

    public List<Report> loadReports() {
        EntityManager em = getEntityManager();
        try {
            em.clear();
            TypedQuery<Report> q = em.createQuery("SELECT r FROM Report r WHERE r.type like :type order by r.orderNumber, r.id", Report.class);
            ReportApplication.log.log(Level.INFO, ReportApplication.getReportType());
            q.setParameter("type", '%' + ReportApplication.getReportType() + '%');
            List<Report> reports = q.getResultList();
            return reports;
        } finally {
            em.close();
        }
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
