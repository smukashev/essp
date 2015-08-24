package com.bsbnb.usci.portlets.crosscheck.data;

import com.bsbnb.usci.portlets.crosscheck.dm.Creditor;
import com.bsbnb.usci.portlets.crosscheck.dm.CrossCheck;

import java.util.Date;
import java.util.List;

public interface DataProvider {
    List<Creditor> getCreditorsList(String CreditorId);

    List<CrossCheck> getCrossChecks(Creditor[] creditors, Date date);
    
    List<CrossCheckMessageDisplayWrapper> getMessages(CrossCheck crossCheck);
    
    Date getCreditorsReportDate(Creditor creditor);
}
