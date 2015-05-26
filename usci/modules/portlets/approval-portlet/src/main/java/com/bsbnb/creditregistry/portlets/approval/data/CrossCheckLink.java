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

    public static final String URL_PREFIX = "http://localhost:8081/cross-check?";
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private Long orderValue;

    public CrossCheckLink(String caption, Long orderValue, Long creditorId, Date reportDate) {
        super(caption, new ExternalResource(
                URL_PREFIX + "creditorId=" + creditorId + "&repDate=" + DEFAULT_DATE_FORMAT.format(reportDate)));
        this.orderValue = orderValue;
        setTargetName("_blank");
    }

    public CrossCheckLink(String caption, Long creditorId, Date reportDate) {
        this(caption, 0l, creditorId, reportDate);
    }

    @Override
    public int compareTo(Object o) {
        CrossCheckLink other = (CrossCheckLink) o;
        return orderValue.compareTo(other.orderValue);
    }
}
