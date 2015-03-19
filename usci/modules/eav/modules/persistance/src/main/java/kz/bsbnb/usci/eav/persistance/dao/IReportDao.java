package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;

import java.util.Date;
import java.util.List;

/**
 * Created by n.seitkozhayev on 2/18/15.
 */
public interface IReportDao {
    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors);
    public Date getFirstNotApprovedDate(Long creditorId);
    public Date getLastApprovedDate(Long creditorId);
}
