package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*@Entity
@SequenceGenerator(name = "MAIL_MESSAGE_PARAMETER_SEQUENCE", sequenceName = "MAIL_MESSAGE_PARAMETER_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
@Table(name = "MAIL_MESSAGE_PARAMETER", schema = "MAINTENANCE")*/
public class MailMessageParameter implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    /*@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAIL_MESSAGE_PARAMETER_SEQUENCE")
    @Basic(optional = false)
    @Column(name = "ID")*/
    private Long id;
    /*@Column(name = "VALUE")*/
    private String value;
    /*@JoinColumn(name = "MAIL_TEMPLATE_PARAMETER_ID", referencedColumnName = "ID")
    @ManyToOne(fetch = FetchType.EAGER)*/
    private MailTemplateParameter mailTemplateParameter;
    /*@JoinColumn(name = "MAIL_MESSAGE_ID", referencedColumnName = "ID")
    @ManyToOne(fetch = FetchType.EAGER)*/
    private MailMessage mailMessage;

    public MailMessageParameter() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MailTemplateParameter getMailTemplateParameter() {
        return mailTemplateParameter;
    }

    public void setMailTemplateParameter(MailTemplateParameter mailTemplateParameterId) {
        this.mailTemplateParameter = mailTemplateParameterId;
    }

    public MailMessage getMailMessage() {
        return mailMessage;
    }

    public void setMailMessage(MailMessage mailMessageId) {
        this.mailMessage = mailMessageId;
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
        if (!(object instanceof MailMessageParameter)) {
            return false;
        }
        MailMessageParameter other = (MailMessageParameter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.dm.maintenance.mail.MailMessageParameter[ id=" + id + " ]";
    }
}
