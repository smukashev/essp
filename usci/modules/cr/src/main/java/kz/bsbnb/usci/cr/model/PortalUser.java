package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class PortalUser implements Serializable {
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(BigInteger id) {
        this.id = id;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the ScreenName
     */
    public String getScreenName() {
        return ScreenName;
    }

    /**
     * @param ScreenName the ScreenName to set
     */
    public void setScreenName(String ScreenName) {
        this.ScreenName = ScreenName;
    }

    /**
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param emailAddress the emailAddress to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return the modifiedDate
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return the creditorList
     */
    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    /**
     * @param creditorList the creditorList to set
     */
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

