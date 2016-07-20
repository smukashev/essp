package kz.bsbnb.usci.eav.model.mail;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Aidar.Myrzahanov
 */
/*
@Entity
@SequenceGenerator(name = "MAIL_TEMPLATE_SEQUENCE", sequenceName = "MAIL_TEMPLATE_SEQ", schema = "MAINTENANCE", initialValue = 1000, allocationSize = 1)
@Table(name = "MAIL_TEMPLATE", schema = "MAINTENANCE")
*/
public class MailTemplate implements Serializable {

    public static final String FILE_PROCESSING_COMPLETED = "FILE_PROCESSING_COMPLETED";
    public static final String MAINTENANCE_REQUEST = "MAINTENANCE_REQUEST";

    private static final long serialVersionUID = 1L;
    /*@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAIL_TEMPLATE_SEQUENCE")
    @Basic(optional = false)
    @Column(name = "ID")*/
    private long id;
//    @Column(name = "SUBJECT")
    private String subject;
//    @Column(name = "TEXT")
    private String text;
//    @Column(name = "CODE")
    private String code;
//    @Column(name = "NAME_RU")
    private String nameRu;
//    @Column(name = "NAME_KZ")
    private String nameKz;
    /*@JoinColumn(name = "CONFIGURATION_TYPE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)*/
    //private Shared configurationType;
    private Long configurationTypeId;

    public MailTemplate() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the nameRu
     */
    public String getNameRu() {
        return nameRu;
    }

    /**
     * @param nameRu the nameRu to set
     */
    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    /**
     * @return the nameKz
     */
    public String getNameKz() {
        return nameKz;
    }

    /**
     * @param nameKz the nameKz to set
     */
    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    public Long getConfigurationTypeId() {
        return configurationTypeId;
    }

    public void setConfigurationTypeId(Long configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }

    /**
     * @return the configurationType
     */

    /*
    public Shared getConfigurationType() {
        return configurationType;
    }


    public void setConfigurationType(Shared configurationType) {
        this.configurationType = configurationType;
    }*/

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailTemplate)) {
            return false;
        }
        MailTemplate other = (MailTemplate) object;
        /*if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }*/
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplate[ id=" + id + " ]";
    }
}
