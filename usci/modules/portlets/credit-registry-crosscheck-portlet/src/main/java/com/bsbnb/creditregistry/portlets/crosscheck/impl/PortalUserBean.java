package com.bsbnb.creditregistry.portlets.crosscheck.impl;

import com.bsbnb.creditregistry.portlets.crosscheck.api.PortalUserBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.model.Creditor;
import com.bsbnb.creditregistry.portlets.crosscheck.model.SubjectType;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * EJB реализует методы для работы с пользователями портала
 * {@link com.bsbnb.creditregistry.dm.maintenance.PortalUser}.
 *
 * @see com.bsbnb.creditregistry.dm.maintenance.PortalUser_.
 * @author alexandr.motov
 */
/*
 @Stateless
 @Remote(value = PortalUserBeanRemoteBusiness.class)
 @Local(value = PortalUserBeanLocalBusiness.class)
 */
public class PortalUserBean implements /*PortalUserBeanLocalBusiness,*/ PortalUserBeanRemoteBusiness {
    /*
     @PersistenceContext(unitName = "credit-registry-ejb-PU")
     private EntityManager em;
     */

    @Override
    public List<Creditor> getMainCreditorsInAlphabeticalOrder(long userId) {
        /*
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<Creditor> cq = cb.createQuery(Creditor.class);
         Root<Creditor> creditorRoot = cq.from(Creditor.class);

         Path<Long> userIdPath = creditorRoot.join(Creditor_.portalUserList).get(PortalUser_.userId);
         cq.where(cb.equal(userIdPath, userId));
         cq.orderBy(cb.asc(creditorRoot.get(Creditor_.name)));

         return em.createQuery(cq).getResultList();
         */
        Creditor sampleCreditor = new Creditor();
        sampleCreditor.setName("Sample creditor");
        sampleCreditor.setId(BigInteger.ONE);
        SubjectType subjectType = new SubjectType();
        subjectType.setCode("Bank");
        subjectType.setNameKz("Банк второго уровня");
        subjectType.setNameRu("Банк второго уровня");
        sampleCreditor.setSubjectType(subjectType);
        return Arrays.asList(sampleCreditor);
    }
}
