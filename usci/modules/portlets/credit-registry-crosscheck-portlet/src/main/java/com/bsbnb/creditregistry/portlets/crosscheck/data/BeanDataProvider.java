package com.bsbnb.creditregistry.portlets.crosscheck.data;

import com.bsbnb.creditregistry.portlets.crosscheck.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Creditor;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Creditor_;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.CrossCheck;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.CrossCheckMessage;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.CrossCheckMessage_;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.CrossCheck_;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.DataTypeUtil;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.PortalUser_;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Report;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Report_;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.SharedIds;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Shared_;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("eav-crosscheck-pu");
        }
        return emf.createEntityManager();
    }
    private PortletEnvironmentFacade facade;

    public BeanDataProvider(PortletEnvironmentFacade facade) throws DataException {
        this.facade = facade;
    }

    @Override
    public List<Creditor> getCreditorsList() {
        EntityManager em = getEntityManager();
        try {
            em.clear();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Creditor> cq = cb.createQuery(Creditor.class);
            Root<Creditor> creditorRoot = cq.from(Creditor.class);

            Path<BigInteger> userIdPath = creditorRoot.join(Creditor_.portalUserList).get(PortalUser_.userId);
            cq.where(cb.equal(userIdPath, BigInteger.valueOf(facade.getUserID())));
            cq.orderBy(cb.asc(creditorRoot.get(Creditor_.name)));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<CrossCheck> getCrossChecks(Creditor[] creditors, Date date) {
        for(Creditor creditor: creditors) {
            System.out.println(creditor);
        }
        EntityManager em = getEntityManager();
        try {
            em.clear();
            BigInteger[] creditorIds = new BigInteger[creditors.length];
            for (int creditorIndex = 0; creditorIndex < creditors.length; creditorIndex++) {
                creditorIds[creditorIndex] = creditors[creditorIndex].getId();
            }
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<CrossCheck> cq = cb.createQuery(CrossCheck.class);
            Root<CrossCheck> root = cq.from(CrossCheck.class);
            Predicate creditorsPredicate = root.get(CrossCheck_.creditor).get(Creditor_.id).in(creditorIds);
            Predicate datePredicate = cb.equal(root.get(CrossCheck_.reportDate), date);
            cq.where(cb.and(creditorsPredicate, datePredicate));
            cq.orderBy(cb.desc(root.get(CrossCheck_.dateBegin)), cb.asc(root.get(CrossCheck_.id)));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    public List<CrossCheckMessageDisplayWrapper> getMessages(CrossCheck crossCheck) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<CrossCheckMessage> cq = cb.createQuery(CrossCheckMessage.class);
            Root<CrossCheckMessage> root = cq.from(CrossCheckMessage.class);

            Predicate crossCheckPredicate = cb.equal(root.get(CrossCheckMessage_.crossCheck).get(CrossCheck_.id), crossCheck.getId());

            cq.where(crossCheckPredicate);
            cq.orderBy(cb.asc(root.get(CrossCheckMessage_.id)));

            List<CrossCheckMessage> list = em.createQuery(cq).getResultList();
            ArrayList<CrossCheckMessageDisplayWrapper> result = new ArrayList<CrossCheckMessageDisplayWrapper>(list.size());
            for (CrossCheckMessage crossCheckMessage : list) {
                result.add(new CrossCheckMessageDisplayWrapper(crossCheckMessage));
            }
            return result;
        } finally {
            em.close();
        }
    }

    private Date getFirstNotApprovedDate(BigInteger creditorId) {
        EntityManager em = getEntityManager();
        try {
            em.clear();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Date> cq = cb.createQuery(Date.class);
            Root<Creditor> root = cq.from(Creditor.class);

            cq.select(cb.greatest(root.get(Creditor_.changeDate)));
            /*Root<Report> root = cq.from(Report.class);

            cq.select(cb.least(root.get(Report_.reportDate)));
            cq.where(
                    cb.and(
                    cb.equal(root.get(Report_.creditor).get(Creditor_.id), creditorId),
                    cb.not(cb.equal(root.get(Report_.status).get(Shared_.id), SharedIds.REPORT_STATUS_COMPLETED)),
                    cb.not(cb.equal(root.get(Report_.status).get(Shared_.id), SharedIds.REPORT_STATUS_ORGANIZATION_APPROVED))));
*/
            return em.createQuery(cq).getSingleResult();
        } finally {
            em.close();
        }
    }

    private Date getLastApprovedDate(BigInteger creditorId) {
        EntityManager em = getEntityManager();
        try {
            em.clear();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Date> cq = cb.createQuery(Date.class);
            Root<Creditor> root = cq.from(Creditor.class);

            cq.select(cb.greatest(root.get(Creditor_.changeDate)));
           
            
            /* Root<Report> root = cq.from(Report.class);

            cq.select(cb.greatest(root.get(Report_.reportDate)));
            cq.where(
                    cb.and(
                    cb.equal(root.get(Report_.creditor).get(Creditor_.id), creditorId),
                    cb.or(
                    cb.equal(root.get(Report_.status).get(Shared_.id), SharedIds.REPORT_STATUS_COMPLETED),
                    cb.equal(root.get(Report_.status).get(Shared_.id), SharedIds.REPORT_STATUS_ORGANIZATION_APPROVED))));
*/
            return em.createQuery(cq).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public Date getCreditorsReportDate(Creditor creditor) {
        EntityManager em = getEntityManager();
        try {
            em.clear();
            BigInteger creditorId = creditor.getId();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            Date firstNotApprovedDate = getFirstNotApprovedDate(creditorId);

            if (firstNotApprovedDate != null) {
                return firstNotApprovedDate;
            }

            Date lastApprovedDate = getLastApprovedDate(creditorId);

            if (lastApprovedDate != null) {
                Creditor targetCreditor = em.find(Creditor.class, creditorId);
                if (targetCreditor == null) {
                    throw new RuntimeException("No creditor");
                }

                if (targetCreditor.getSubjectType() == null) {
                    throw new RuntimeException(String.format("Subject type of the creditor with ID {0} is null", creditorId));
                }

                return DataTypeUtil.plus(lastApprovedDate, Calendar.MONTH, targetCreditor.getSubjectType().getReportPeriodDurationMonths());
            }

            // TODO Пока что начальная дата как константа, нужно сделать настройку

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(2014, Calendar.JANUARY, 13);
            return calendar.getTime();
        } finally {
            em.close();
        }
    }
}
