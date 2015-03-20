package com.bsbnb.usci.portlets.crosscheck.data;

import com.bsbnb.usci.portlets.crosscheck.dm.Creditor;
import com.bsbnb.usci.portlets.crosscheck.dm.CrossCheck;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
    /*
     * Метод возвращает список всех кредиторов
     */
    public List<Creditor> getCreditorsList();
    /*
     * Метод возвращает записи по межформенного контроля по кредитору
     * Если creditor равен null возвращаются записи по всем кредиторам
     */
    public List<CrossCheck> getCrossChecks(Creditor[] creditors, Date date);
    
    public List<CrossCheckMessageDisplayWrapper> getMessages(CrossCheck crossCheck);
    
    public Date getCreditorsReportDate(Creditor creditor);
}
