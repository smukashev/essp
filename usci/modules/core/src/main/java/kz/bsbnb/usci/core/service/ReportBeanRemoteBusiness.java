package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;

import java.util.Date;

public interface ReportBeanRemoteBusiness {
    Date getReportDate(long creditorId);
    Report getByCreditor_ReportDate(Creditor creditor, Date reportDate);
}
