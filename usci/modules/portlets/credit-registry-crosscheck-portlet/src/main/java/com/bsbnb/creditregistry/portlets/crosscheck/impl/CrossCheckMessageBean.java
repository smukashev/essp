package com.bsbnb.creditregistry.portlets.crosscheck.impl;

import com.bsbnb.creditregistry.portlets.crosscheck.api.CrossCheckMessageBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheck;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheckMessage;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*
 @Stateless
 @Local(value=CrossCheckMessageBeanLocalBusiness.class)
 @Remote(value=CrossCheckMessageBeanRemoteBusiness.class) 
 */
public class CrossCheckMessageBean implements /*CrossCheckMessageBeanLocalBusiness,*/ CrossCheckMessageBeanRemoteBusiness {

    /*
     @PersistenceContext(unitName="credit-registry-ejb-PU")
     private EntityManager em;
     */
    @Override
    public List<CrossCheckMessage> getMessagesByCrossCheck(CrossCheck crossCheck) {
        /*
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<CrossCheckMessage> cq = cb.createQuery(CrossCheckMessage.class);
         Root<CrossCheckMessage> root = cq.from(CrossCheckMessage.class);
        
         Predicate crossCheckPredicate = cb.equal(root.get(CrossCheckMessage_.crossCheck).get(CrossCheck_.id), crossCheck.getId());

         cq.where(crossCheckPredicate);
         cq.orderBy(cb.asc(root.get(CrossCheckMessage_.id)));

         return em.createQuery(cq).getResultList();
         */
        CrossCheckMessage sampleSuccessMessage = new CrossCheckMessage();
        sampleSuccessMessage.setCrossCheck(crossCheck);
        sampleSuccessMessage.setDescription("Sample balance account no");
        sampleSuccessMessage.setDifference("0");
        sampleSuccessMessage.setId(1);
        sampleSuccessMessage.setInnerValue("1234");
        sampleSuccessMessage.setIsError(0);
        sampleSuccessMessage.setMessage(null);
        sampleSuccessMessage.setOuterValue("1234");

        CrossCheckMessage sampleFailedMessage = new CrossCheckMessage();
        sampleFailedMessage.setCrossCheck(crossCheck);
        sampleFailedMessage.setDescription("Sample balance account no");
        sampleFailedMessage.setDifference("200");
        sampleFailedMessage.setId(1);
        sampleFailedMessage.setInnerValue("1000");
        sampleFailedMessage.setIsError(1);
        sampleFailedMessage.setMessage(null);
        sampleFailedMessage.setOuterValue("800");

        return Arrays.asList(sampleSuccessMessage, sampleFailedMessage);
    }
}
