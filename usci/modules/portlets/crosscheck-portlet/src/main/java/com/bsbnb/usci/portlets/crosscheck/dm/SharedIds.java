package com.bsbnb.usci.portlets.crosscheck.dm;

import java.math.BigInteger;

/**
 *
 * @author Alexandr.Motov
 */
public interface SharedIds {
    
    public static final BigInteger REPORT_STATUS_IN_PROGRESS = new BigInteger("90");
    public static final BigInteger REPORT_STATUS_ORGANIZATION_APPROVED = new BigInteger("128");
    public static final BigInteger REPORT_STATUS_COMPLETED = new BigInteger("92");
    
    public static final BigInteger DEBT_REMAINS_TYPE_DEBT_CURRENT = new BigInteger("55");
    public static final BigInteger DEBT_REMAINS_TYPE_DEBT_PAST_DUE = new BigInteger("56");
    public static final BigInteger DEBT_REMAINS_TYPE_DEBT_WRITE_OFF = new BigInteger("57");
    public static final BigInteger DEBT_REMAINS_TYPE_INTEREST_CURRENT = new BigInteger("58");
    public static final BigInteger DEBT_REMAINS_TYPE_INTEREST_PAST_DUE = new BigInteger("59");
    public static final BigInteger DEBT_REMAINS_TYPE_INTEREST_WRITE_OFF = new BigInteger("60");
    public static final BigInteger DEBT_REMAINS_TYPE_DISCOUNT = new BigInteger("61");
    public static final BigInteger DEBT_REMAINS_TYPE_CORRECTION = new BigInteger("62");
    public static final BigInteger DEBT_REMAINS_TYPE_DISCOUNTED_VALUE = new BigInteger("62");
    public static final BigInteger DEBT_REMAINS_TYPE_LIMIT = new BigInteger("102");
    public static final BigInteger DEBT_REMAINS_TYPE_PROVISION_AFN = new BigInteger("103");
    public static final BigInteger DEBT_REMAINS_TYPE_PROVISION_MSFO = new BigInteger("104");
    
    public static final BigInteger CREDIT_KIND_CREDIT = new BigInteger("14");
    public static final BigInteger CREDIT_KIND_CONTINGENT_LIABILITY = new BigInteger("15");
    
    
}
