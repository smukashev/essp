package com.bsbnb.creditregistry.portlets.crosscheck.impl;

import com.bsbnb.creditregistry.portlets.crosscheck.api.CrossCheckBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.model.Creditor;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheck;
import com.bsbnb.creditregistry.portlets.crosscheck.model.Shared;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckBean implements CrossCheckBeanRemoteBusiness {

    @PersistenceContext(unitName = "credit-registry-ejb-PU")
    private EntityManager em;

    @Override
    public List<CrossCheck> loadCrossCheck(List<BigInteger> creditorIds, Date date) {
        /*
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<CrossCheck> cq = cb.createQuery(CrossCheck.class);
         Root<CrossCheck> root = cq.from(CrossCheck.class);
         Predicate creditorsPredicate = root.get(CrossCheck_.creditor).get(Creditor_.id).in(creditorIds.toArray());
         Predicate datePredicate = cb.equal(root.get(CrossCheck_.reportDate), date);
         cq.where(cb.and(creditorsPredicate, datePredicate));
         cq.orderBy(cb.desc(root.get(CrossCheck_.dateBegin)), cb.asc(root.get(CrossCheck_.id)));

         return em.createQuery(cq).getResultList();*/
        Creditor sampleCreditor = new Creditor();
        sampleCreditor.setName("Sample creditor");
        sampleCreditor.setId(creditorIds.get(0));
        CrossCheck sampleCrossCheck = new CrossCheck();
        sampleCrossCheck.setCreditor(sampleCreditor);
        sampleCrossCheck.setDateBegin(new Date());
        sampleCrossCheck.setDateEnd(new Date());
        sampleCrossCheck.setId(1);
        sampleCrossCheck.setReportDate(new Date());
        Shared sampleCrossCheckStatus = new Shared();
        sampleCrossCheckStatus.setCode("FAILED");
        sampleCrossCheckStatus.setId(BigInteger.ONE);
        sampleCrossCheckStatus.setNameKz("Отконтролирован с ошибками");
        sampleCrossCheckStatus.setNameRu("Отконтролирован с ошибками");
        sampleCrossCheck.setStatus(sampleCrossCheckStatus);
        sampleCrossCheck.setUser("Test User");
        return Arrays.asList(sampleCrossCheck);
    }
}
