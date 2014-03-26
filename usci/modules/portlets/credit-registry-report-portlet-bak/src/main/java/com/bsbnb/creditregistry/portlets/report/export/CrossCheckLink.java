package com.bsbnb.creditregistry.portlets.report.export;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckLink extends Link implements Comparable<CrossCheckLink> {

    public static final String URL_PREFIX = "https://rcredit.nationalbank.kz/cross-check#";
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public CrossCheckLink(String caption, int creditorId, Date reportDate) {
        super(caption, new ExternalResource(URL_PREFIX + creditorId + "/" + DEFAULT_DATE_FORMAT.format(reportDate)));
        setTargetName("_blank");
    }

    @Override
    public int compareTo(CrossCheckLink o) {
        if (getCaption() == null) {
            return -1;
        }
        return getCaption().compareTo(o.getCaption());
    }
}
