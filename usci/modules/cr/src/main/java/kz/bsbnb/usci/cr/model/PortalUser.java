package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class PortalUser implements Serializable {
    private static final long serialVersionUID = 8626348715892412242L;

    private BigInteger id;

    private Long userId;

    private String ScreenName;

    private String emailAddress;

    private String firstName;

    private String lastName;

    private String middleName;

    private Date modifiedDate;

    private Boolean isActive;

    private List<Creditor> creditorList;

    public PortalUser() {
        super();
    }

    public PortalUser(Long userId, String emailAddress, Date modifiedDate) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.modifiedDate = modifiedDate;
    }

    public PortalUser(BigInteger id, Long userId, String ScreenName, String emailAddress, String firstName, 
            String lastName, String middleName, List<Creditor> creditorList, Date modifiedDate) {
        this.id = id;
        this.userId = userId;
        this.ScreenName = ScreenName;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.creditorList = creditorList;
        this.modifiedDate = modifiedDate;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getScreenName() {
        return ScreenName;
    }

    public void setScreenName(String ScreenName) {
        this.ScreenName = ScreenName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    public void setCreditorList(List<Creditor> creditorList) {
        this.creditorList = creditorList;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean isActive) {
        this.isActive = isActive;
    }
}