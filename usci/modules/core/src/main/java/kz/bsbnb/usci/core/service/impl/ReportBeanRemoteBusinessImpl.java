package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportType;
import kz.bsbnb.usci.cr.model.Shared;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReportBeanRemoteBusinessImpl implements ReportBeanRemoteBusiness
{
    @Override
    public Date getReportDate(long creditorId)
    {
        return new Date();
    }

    @Override
    public Report getByCreditor_ReportDate(Creditor creditor, Date reportDate)
    {
        Report r = new Report();
        Shared s = new Shared();

        s.setCode(ReportType.RECIPIENCY_IN_PROGRESS.getCode());

        r.setStatus(s);

        return null;
    }
}
