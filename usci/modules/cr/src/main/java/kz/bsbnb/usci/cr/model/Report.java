package kz.bsbnb.usci.cr.model;

//import com.bsbnb.creditregistry.dm.api.BaseStateModel;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.util.DataTypeUtil;
import java.math.BigInteger;
import java.util.Date;

public class Report {
    private BigInteger id;
    private Creditor creditor;
    private Shared status;
    private BigInteger totalCount;
    private BigInteger actualCount;
    private Date reportDate;
    private Date beginningDate;
    private Date endDate;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public Shared getStatus() {
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }

    public BigInteger getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(BigInteger totalCount) {
        this.totalCount = totalCount;
    }

    public BigInteger getActualCount() {
        return actualCount;
    }

    public void setActualCount(BigInteger actualCount) {
        this.actualCount = actualCount;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Date getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(Date beginningDate) {
        this.beginningDate = beginningDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{id=");
        sb.append(getId());

        if (getCreditor() != null) {
            sb.append(", creditor={id=");
            sb.append(getCreditor().getId());
            sb.append("}");
        } else {
            sb.append(", creditor=null");
        }

        sb.append(", actualCount=");
        sb.append(getActualCount());
        sb.append(", totalCount=");
        sb.append(getTotalCount());
        sb.append(", beginingDate=");
        sb.append(DataTypeUtil.convertDateToString(DataTypeUtil.LONG_DATE_FORMAT, getBeginningDate()));
        sb.append(", endDate=");
        sb.append(DataTypeUtil.convertDateToString(DataTypeUtil.LONG_DATE_FORMAT, getEndDate()));
        
        if (getStatus() != null) {
            sb.append(", status={id=");
            sb.append(getStatus().getId());
            sb.append("}");
        } else {
            sb.append(", status=null");
        }
        sb.append("}");

        return sb.toString();
    }
}
