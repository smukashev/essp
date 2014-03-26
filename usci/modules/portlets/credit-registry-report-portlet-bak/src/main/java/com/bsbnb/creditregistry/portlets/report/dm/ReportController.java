package com.bsbnb.creditregistry.portlets.report.dm;

import com.liferay.portal.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportController {

    public List<Report> loadReports() {
        ArrayList<Report> reports = new ArrayList<Report>();

        Report r = new Report();

        r.setName("BanksWithData");
        r.setNameKz("Организации предоставившие данные");
        r.setId(1L);
        r.setNameRu("Организации предоставившие данные");
        r.setOrderNumber(1);
        r.setType("REPORTBANK");

        ReportInputParameter reportInputParameter = new ReportInputParameter();

        reportInputParameter.setNameRu("Date");
        reportInputParameter.setNameKz("Date");
        reportInputParameter.setParameterType(ParameterType.DATE);
        reportInputParameter.setParameterName("Date");

        r.getInputParameters().add(reportInputParameter);

        reports.add(r);

        return reports;
    }

    public List<ReportLoad> loadUsersLoads(long portalUserId) {
        ArrayList<ReportLoad> list = new ArrayList<ReportLoad>();

        return list;
    }

    public void insertOrUpdateReportLoad(ReportLoad reportLoad) {
    }

    public List<ReportLoad> loadUsersLoads(List<User> coworkers) {
        ArrayList<ReportLoad> list = new ArrayList<ReportLoad>();

        return list;
    }
}
