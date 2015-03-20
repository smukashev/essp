package com.bsbnb.usci.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "CROSS_CHECK_MESSAGE")
public class CrossCheckMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigInteger id;
    @Size(max = 500)
    @Column(name = "INNER_VALUE")
    private String innerValue;
    @Size(max = 500)
    @Column(name = "OUTER_VALUE")
    private String outerValue;
    @Size(max = 500)
    @Column(name = "DIFF")
    private String diff;
    @Size(max = 2000)
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "IS_ERROR")
    private BigInteger isError;
    @JoinColumn(name = "MESSAGE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Message message;
    @JoinColumn(name = "CROSS_CHECK_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private CrossCheck crossCheck;

    public CrossCheckMessage() {
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getInnerValue() {
        return innerValue;
    }

    public void setInnerValue(String innerValue) {
        this.innerValue = innerValue;
    }

    public String getOuterValue() {
        return outerValue;
    }

    public void setOuterValue(String outerValue) {
        this.outerValue = outerValue;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsError() {
        return BigInteger.ONE.equals(isError);
    }

    public void setIsError(boolean isError) {
        this.isError = isError ? BigInteger.ONE : BigInteger.ZERO;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public CrossCheck getCrossCheck() {
        return crossCheck;
    }

    public void setCrossCheck(CrossCheck crossCheck) {
        this.crossCheck = crossCheck;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CrossCheckMessage)) {
            return false;
        }
        CrossCheckMessage other = (CrossCheckMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CrossCheckMessage[ id=" + id + " ]";
    }

}
