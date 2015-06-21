package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*
@Entity
@SequenceGenerator(name = "MAIL_MESSAGE_SEQUENCE", sequenceName = "MAIL_MESSAGE_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
@Table(name = "MAIL_MESSAGE", schema = "MAINTENANCE")
*/
public class MailMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    /*
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAIL_MESSAGE_SEQUENCE")
    @Basic(optional = false)
    @Column(name = "ID")
    */
    private Long id;
    /*
    @JoinColumn(name = "STATUS_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    */
    //private Shared status;
    private MailMessageStatus status;
    /*
    @Basic(optional = false)
    @Column(name = "RECIPIENT_USER_ID")
    */
    private Long recipientUserId;
    /*
    @JoinColumn(name = "MAIL_TEMPLATE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    */
    private MailTemplate mailTemplate;
    /*
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_DATE", nullable = true)
    */
    private Date creationDate;
    /*
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SENDING_DATE", nullable = true)
    */
    private Date sendingDate;

    public MailMessage() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MailMessageStatus getStatus() {
        return status;
    }

    public void setStatus(MailMessageStatus status) {
        this.status = status;
    }

    /*
    public Shared getStatus() {
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }
    */

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public MailTemplate getMailTemplate() {
        return mailTemplate;
    }

    public void setMailTemplate(MailTemplate mailTemplateId) {
        this.mailTemplate = mailTemplateId;
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
        if (!(object instanceof MailMessage)) {
            return false;
        }
        MailMessage other = (MailMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage[ id=" + id + " ]";
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the sendingDate
     */
    public Date getSendingDate() {
        return sendingDate;
    }

    /**
     * @param sendingDate the sendingDate to set
     */
    public void setSendingDate(Date sendingDate) {
        this.sendingDate = sendingDate;
    }
}
