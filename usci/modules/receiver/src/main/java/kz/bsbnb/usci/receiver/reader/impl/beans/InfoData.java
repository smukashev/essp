package kz.bsbnb.usci.receiver.reader.impl.beans;

import java.util.Date;

public class InfoData {
    private Date reportDate;

    private Date accountDate;

    private Long actualCreditCount;

    private String code;

    private String docType;

    private String docValue;

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Long getActualCreditCount() {
        return actualCreditCount;
    }

    public void setActualCreditCount(Long actualCreditCount) {
        this.actualCreditCount = actualCreditCount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocValue() {
        return docValue;
    }

    public void setDocValue(String docValue) {
        this.docValue = docValue;
    }

    public Date getAccountDate() {
        return accountDate;
    }

    public void setAccountDate(Date accountDate) {
        this.accountDate = accountDate;
    }
}
