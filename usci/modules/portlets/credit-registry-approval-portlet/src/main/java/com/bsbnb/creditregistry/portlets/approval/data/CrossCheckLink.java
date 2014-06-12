package com.bsbnb.creditregistry.portlets.approval.data;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckLink extends Link implements Comparable<Object> {

    public static final String URL_PREFIX = "https://rcredit.nationalbank.kz/cross-check#";
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private BigInteger orderValue;

    public CrossCheckLink(String caption, BigInteger orderValue, int creditorId, Date reportDate) {
        super(caption, new ExternalResource(URL_PREFIX + creditorId + "/" + DEFAULT_DATE_FORMAT.format(reportDate)));
        this.orderValue = orderValue;
        setTargetName("_blank");
    }

    public CrossCheckLink(String caption, int creditorId, Date reportDate) {
        this(caption, BigInteger.ZERO, creditorId, reportDate);
    }

    @Override
    public int compareTo(Object o) {
        CrossCheckLink other = (CrossCheckLink) o;
        return orderValue.compareTo(other.orderValue);
    }
}
