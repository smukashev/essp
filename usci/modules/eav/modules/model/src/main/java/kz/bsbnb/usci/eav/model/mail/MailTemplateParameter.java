package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*@Entity
@SequenceGenerator(name = "MAIL_TEMPLATE_PARAMETER_SEQUENCE", sequenceName = "MAIL_TEMPLATE_PARAMETER_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
@Table(name = "MAIL_TEMPLATE_PARAMETER", schema = "MAINTENANCE")*/
public class MailTemplateParameter implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    /*@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAIL_TEMPLATE_PARAMETER_SEQUENCE")
    @Basic(optional = false)
    @Column(name = "ID")*/
    private Long id;
    /*@Basic(optional = false)
    @Column(name = "CODE")*/
    private String code;
    //@Column(name = "ORDER_NUMBER")
    private Long orderNumber;
    /*@JoinColumn(name = "MAIL_TEMPLATE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)*/
    private MailTemplate mailTemplate;

    public MailTemplateParameter() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
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
        if (!(object instanceof MailTemplateParameter)) {
            return false;
        }
        MailTemplateParameter other = (MailTemplateParameter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplateParameter[ id=" + id + " ]";
    }
}
