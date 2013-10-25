package com.bsbnb.creditregistry.portlets.crosscheck.impl;

import com.bsbnb.creditregistry.portlets.crosscheck.api.ReportBeanRemoteBusiness;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/*
 * Пустая реализация бина
 */
public class ReportBean implements ReportBeanRemoteBusiness {
       
    public Date getReportDate(BigInteger creditorId) {
        /*
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        Date firstNotApprovedDate = getFirstNotApprovedDate(creditorId);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("First not approved report date is %s.", firstNotApprovedDate == null ? "null" : dateFormat.format(firstNotApprovedDate)));
        
        if (firstNotApprovedDate != null)
            return firstNotApprovedDate;
        
        Date lastApprovedDate = getLastApprovedDate(creditorId);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Last approved report date is %s.", lastApprovedDate == null ? "null" : dateFormat.format(lastApprovedDate)));
        
        if (lastApprovedDate != null) {
            Creditor creditor = em.find(Creditor.class, creditorId);
            if (creditor == null)
                throw new com.bsbnb.creditregistry.ejb.ref.exception.ObjectNotFoundException(creditorId, Creditor.class);
            
            if (creditor.getSubjectType() == null)
                throw new RuntimeException(String.format("Subject type of the creditor with ID {0} is null", creditorId));
                
            return DataTypeUtil.plus(lastApprovedDate, Calendar.MONTH, creditor.getSubjectType().getReportPeriodDurationMonths());
        }
        
        // TODO Пока что начальная дата как константа, нужно сделать настройку
        
        try {
            Sysconfig initialReportDate = sysconfigBean.getSysconfigByKey("INITIAL_REPORT_DATE");
            return dateFormat.parse(initialReportDate.getValue());
        } catch (ResultInconsistentException ex) {
            throw new RuntimeException("Can not get system config by key INITIAL_REPORT_DATE.");
        } catch (ResultNotFoundException ex) {
            throw new RuntimeException("Can not get system config by key INITIAL_REPORT_DATE.");
        } catch (ParseException ex) {
            throw new RuntimeException("Unable to parse the initial report date."); 
        }
        */
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2013, 5, 1);
        return calendar.getTime();
    }
    
}
