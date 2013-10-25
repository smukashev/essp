package com.bsbnb.creditregistry.portlets.crosscheck.api;

import java.math.BigInteger;
import java.util.Date;

/**
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a>
 */
public interface ReportBeanRemoteBusiness {

    public Date getReportDate(BigInteger creditorId);
}
