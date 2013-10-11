package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;

import java.util.Date;

public interface ReportBeanRemoteBusiness
{
    public Date getReportDate(long creditorId);
    public Report getByCreditor_ReportDate(Creditor creditor, Date reportDate);
}
