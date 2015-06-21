package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*
@Entity
@SequenceGenerator(name = "USER_MAIL_TEMPLATE_SEQUENCE", sequenceName = "USER_MAIL_TEMPLATE_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
@Table(name = "USER_MAIL_TEMPLATE", schema = "MAINTENANCE")
*/
public class UserMailTemplate implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    /*
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_MAIL_TEMPLATE_SEQUENCE")
    @Basic(optional = false)
    @Column(name = "ID")
    */
    private long id;
//    @Basic(optional = false)
//    @Column(name = "PORTAL_USER_ID", nullable = false)
    private long portalUserId;
    /*@JoinColumn(name = "MAIL_TEMPLATE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)*/
    private MailTemplate mailTemplate;
    /*@Column(name = "ENABLED", nullable = false)*/
    private boolean enabled;

    public UserMailTemplate() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPortalUserId() {
        return portalUserId;
    }

    public void setPortalUserId(long portalUserId) {
        this.portalUserId = portalUserId;
    }

    public MailTemplate getMailTemplate() {
        return mailTemplate;
    }

    public void setMailTemplate(MailTemplate mailTemplate) {
        this.mailTemplate = mailTemplate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserMailTemplate)) {
            return false;
        }
        UserMailTemplate other = (UserMailTemplate) object;
        /*if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }*/
        return this.id == other.id;
        //return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.dm.maintenance.mail.UserMailSetting[ id=" + id + " ]";
    }
}
