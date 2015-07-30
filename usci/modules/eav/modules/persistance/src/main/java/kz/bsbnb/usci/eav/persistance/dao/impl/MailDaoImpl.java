package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.mail.*;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class MailDaoImpl extends JDBCSupport implements IMailDao {

    @Autowired
    DSLContext context;

    private final Logger logger = LoggerFactory.getLogger(BaseEntityDaoImpl.class);

    @Override
    public List<UserMailTemplate> getUserMailTemplates(long userId) {

        Table root = MAIL_USER_MAIL_TEMPLATE.as("root");
        Table templ = MAIL_TEMPLATE.as("tmpl");


        Select select = context.select(root.field(MAIL_USER_MAIL_TEMPLATE.ID),
                root.field(MAIL_USER_MAIL_TEMPLATE.ENABLED),
                templ.field(MAIL_TEMPLATE.ID).as("mail_template_id"),
                templ.field(MAIL_TEMPLATE.CODE),
                templ.field(MAIL_TEMPLATE.NAME_RU),
                templ.field(MAIL_TEMPLATE.NAME_KZ),
                templ.field(MAIL_TEMPLATE.CONFIGURATION_TYPE_ID),
                templ.field(MAIL_TEMPLATE.SUBJECT),
                templ.field(MAIL_TEMPLATE.TEXT)
        ).from(root).join(templ)
                .on(root.field(MAIL_USER_MAIL_TEMPLATE.MAIL_TEMPLATE_ID).eq(templ.field(MAIL_TEMPLATE.ID)))
                .where(root.field(MAIL_USER_MAIL_TEMPLATE.PORTAL_USER_ID).eq(userId));

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<UserMailTemplate> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            UserMailTemplate r = new UserMailTemplate();
            r.setId(((BigDecimal)row.get("ID")).longValue());
            r.setEnabled(((BigDecimal)row.get("ENABLED")).longValue() == 1);
            r.setPortalUserId(userId);
            r.setMailTemplate(readMailTemplate(row));
            ret.add(r);
        }

        return ret;
    }

    @Override
    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates) {
        List<Long> enabled = new LinkedList<>();
        List<Long> disabled = new LinkedList<>();
        for(UserMailTemplate u : userTemplates) {
            if(u.isEnabled()) enabled.add(u.getId());
            else disabled.add(u.getId());
        }

        Update update = context.update(MAIL_USER_MAIL_TEMPLATE)
                .set(MAIL_USER_MAIL_TEMPLATE.ENABLED, DataUtils.convert(true))
                .where(MAIL_USER_MAIL_TEMPLATE.ID.in(enabled));

        jdbcTemplate.update(update.getSQL(),update.getBindValues().toArray());

        update = context.update(MAIL_USER_MAIL_TEMPLATE)
                .set(MAIL_USER_MAIL_TEMPLATE.ENABLED, DataUtils.convert(false))
                .where(MAIL_USER_MAIL_TEMPLATE.ID.in(disabled));

        jdbcTemplate.update(update.getSQL(),update.getBindValues().toArray());
    }

    public MailTemplate readMailTemplate(Map<String, Object> row){
        MailTemplate mt = new MailTemplate();
        mt.setId(((BigDecimal)row.get("MAIL_TEMPLATE_ID")).longValue());
        mt.setCode((String)row.get("CODE"));
        mt.setNameRu((String)row.get("NAME_RU"));
        mt.setNameKz((String) row.get("NAME_KZ"));
        mt.setConfigurationTypeId(((BigDecimal) row.get("CONFIGURATION_TYPE_ID")).longValue());
        mt.setSubject((String)row.get("SUBJECT"));
        mt.setText((String)row.get("TEXT"));
        return mt;
    }

    @Override
    public List<MailMessage> getMailMessagesByUser(Long userId) {
        Table root = MAIL_MESSAGE.as("root");
        Table templ = MAIL_TEMPLATE.as("tmpl");


        Select select = context.select(root.field(MAIL_MESSAGE.ID),
                root.field(MAIL_MESSAGE.CREATION_DATE),
                root.field(MAIL_MESSAGE.SENDING_DATE),
                root.field(MAIL_MESSAGE.STATUS_ID),
                templ.field(MAIL_TEMPLATE.ID).as("mail_template_id"),
                templ.field(MAIL_TEMPLATE.CODE),
                templ.field(MAIL_TEMPLATE.NAME_RU),
                templ.field(MAIL_TEMPLATE.NAME_KZ),
                templ.field(MAIL_TEMPLATE.CONFIGURATION_TYPE_ID),
                templ.field(MAIL_TEMPLATE.SUBJECT),
                templ.field(MAIL_TEMPLATE.TEXT)
        ).from(root).join(templ)
                .on(root.field(MAIL_MESSAGE.MAIL_TEMPLATE_ID).eq(templ.field(MAIL_TEMPLATE.ID)))
                .where(root.field(MAIL_MESSAGE.RECIPIENT_USER_ID).eq(userId));

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<MailMessage> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            MailMessage r = new MailMessage();
            r.setId(((BigDecimal)row.get("ID")).longValue());
            r.setCreationDate((Date) row.get("CREATION_DATE"));
            r.setSendingDate((Date) row.get("SENDING_DATE"));
            r.setStatusId(((BigDecimal) row.get("STATUS_ID")).intValue());
            r.setRecipientUserId(userId);
            r.setMailTemplate(readMailTemplate(row));
            ret.add(r);
        }

        return ret;
    }

    @Override
    public List<MailMessageParameter> getParametersByMessage(MailMessage message) {
        Table root = MAIL_MESSAGE_PARAMETER.as("root");
        Table templ = MAIL_TEMPLATE_PARAMETER.as("tmpl");


        Select select = context.select(root.field(MAIL_MESSAGE_PARAMETER.ID),
                root.field(MAIL_MESSAGE_PARAMETER.VALUE),
                templ.field(MAIL_TEMPLATE_PARAMETER.ID).as("mail_template_parameter_id"),
                templ.field(MAIL_TEMPLATE_PARAMETER.CODE),
                templ.field(MAIL_TEMPLATE_PARAMETER.ORDER_NUMBER)
        ).from(root).join(templ)
                .on(root.field(MAIL_MESSAGE_PARAMETER.MAIL_TEMPLATE_PARAMETER_ID).eq(templ.field(MAIL_TEMPLATE_PARAMETER.ID)))
                .where(root.field(MAIL_MESSAGE_PARAMETER.MAIL_MESSAGE_ID).eq(message.getId()));

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<MailMessageParameter> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            MailMessageParameter r = new MailMessageParameter();
            r.setId(((BigDecimal)row.get("ID")).longValue());
            r.setValue((String)row.get("VALUE"));

            MailTemplateParameter parameter = new MailTemplateParameter();
            parameter.setId(((BigDecimal)row.get("MAIL_TEMPLATE_PARAMETER_ID")).longValue());
            parameter.setCode((String)row.get("CODE"));
            parameter.setId(((BigDecimal) row.get("ORDER_NUMBER")).longValue());
            r.setMailTemplateParameter(parameter);
            ret.add(r);
        }

        return ret;
    }

    @Override
    public MailTemplate getMailTemplateByCode(String templateCode){
        Select select = context.selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.CODE.eq(templateCode));

        return (MailTemplate) jdbcTemplate.queryForObject(select.getSQL(), new BeanPropertyRowMapper(MailTemplate.class),
                new Object[]{templateCode});
    }

    @Override
    public void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode) {
        //try {
            MailMessage mailMessage = new MailMessage();

            MailTemplate mailTemplate;
            //try {
                mailTemplate = getMailTemplateByCode(templateCode);
            /*
            } catch (NoResultException nre) {
                Logger.getLogger(MailMessageBean.class.getCanonicalName()).log(Level.INFO, null, nre);
                throw new IllegalArgumentException("Template with code: " + templateCode + " not found");
            }*/

            mailMessage.setMailTemplate(mailTemplate);
            mailMessage.setCreationDate(new Date());
            mailMessage.setRecipientUserId(recipientUserId);
            /*Shared newMessageStatus = sharedBusiness.findByC_T(
                    MailMessageStatus.PROCESSING.getCode(),
                    SharedType.MAIL_MESSAGE_STATUS.getType());
            mailMessage.setStatus(newMessageStatus);*/
            mailMessage.setStatusId(MailMessageStatuses.PROCESSING);
            //em.persist(mailMessage);
            Insert insert = context.insertInto(MAIL_MESSAGE)
                    .set(MAIL_MESSAGE.RECIPIENT_USER_ID, mailMessage.getRecipientUserId())
                    .set(MAIL_MESSAGE.CREATION_DATE, DataUtils.convertToTimestamp(mailMessage.getCreationDate()))
                    .set(MAIL_MESSAGE.STATUS_ID, mailMessage.getStatusId().longValue())
                    .set(MAIL_MESSAGE.MAIL_TEMPLATE_ID, mailTemplate.getId());


        long id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        //jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
        mailMessage.setId(id);

            if (parametersByCode != null) {
                List<MailTemplateParameter> templateParameters = getParametersByTemplate(mailTemplate);
                for (MailTemplateParameter mailTemplateParameter : templateParameters) {
                    if (parametersByCode.containsKey(mailTemplateParameter.getCode())) {
                        //MailMessageParameter mailMessageParameter = new MailMessageParameter();
                        /*mailMessageParameter.setMailMessage(mailMessage);
                        mailMessageParameter.setMailTemplateParameter(mailTemplateParameter);
                        mailMessageParameter.setValue(parametersByCode.getProperty(mailTemplateParameter.getCode()));
                        mailMessageParameter.setMailMessage(mailMessage);
                        em.persist(mailMessageParameter);*/
                        insert = context.insertInto(MAIL_MESSAGE_PARAMETER)
                                .set(MAIL_MESSAGE_PARAMETER.MAIL_MESSAGE_ID, mailMessage.getId())
                                .set(MAIL_MESSAGE_PARAMETER.MAIL_TEMPLATE_PARAMETER_ID, mailTemplateParameter.getId())
                                .set(MAIL_MESSAGE_PARAMETER.VALUE, (String) parametersByCode.get(mailTemplateParameter.getCode()));

                        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
                    }
                }
            }

        /*
        } catch (ResultNotFoundException rnfe) {
            //Logger(MailMessageBean.class.getCanonicalName()).log(Level.INFO, null, rnfe);
            throw new RuntimeException("Initial status not found");
        } catch (ResultInconsistentException rie) {
            //Logger.getLogger(MailMessageBean.class.getCanonicalName()).log(Level.INFO, null, rie);
            throw new RuntimeException("Initial status inconsistent");
        }*/
    }

    private List<MailTemplateParameter> getParametersByTemplate(MailTemplate mailTemplate) {
        Select select = context.selectFrom(MAIL_TEMPLATE_PARAMETER)
                .where(MAIL_TEMPLATE_PARAMETER.MAIL_TEMPLATE_ID.eq(mailTemplate.getId()));

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<MailTemplateParameter> ret = new LinkedList<>();

        for(Map<String,Object> row : rows) {
            MailTemplateParameter mailTemplateParameter = new MailTemplateParameter();
            mailTemplateParameter.setId(((BigDecimal) row.get("ID")).longValue());
            mailTemplateParameter.setMailTemplate(mailTemplate);
            mailTemplateParameter.setCode(((String) row.get("CODE")));
            mailTemplateParameter.setOrderNumber(((BigDecimal) row.get("ORDER_NUMBER")).longValue());
            ret.add(mailTemplateParameter);
        }

        /*
        List<MailTemplateParameter> ret =
                jdbcTemplate.query(select.getSQL(),new BeanPropertyRowMapper<MailTemplateParameter>(), MailTemplateParameter.class);
        */
        return ret;
    }

    @Override
    public void updateMailMessage(MailMessage message) {
        Update update = context.update(MAIL_MESSAGE)
                .set(MAIL_MESSAGE.STATUS_ID, message.getStatusId().longValue())
                .set(MAIL_MESSAGE.SENDING_DATE, DataUtils.convertToTimestamp(message.getSendingDate()))
                .where(MAIL_MESSAGE.ID.eq(message.getId()));

        jdbcTemplate.update(update.getSQL(),update.getBindValues().toArray());
    }

    @Override
    public boolean isTemplateEnabledForUser(Long templateId, long userId) {
        Select select = context.select(DSL.count())
                .from(MAIL_USER_MAIL_TEMPLATE)
                .where(MAIL_USER_MAIL_TEMPLATE.PORTAL_USER_ID.eq(userId))
                .and(MAIL_USER_MAIL_TEMPLATE.MAIL_TEMPLATE_ID.eq(templateId))
                .and(MAIL_USER_MAIL_TEMPLATE.ENABLED.eq(DataUtils.convert(false)));

        int ans = jdbcTemplate.queryForInt(select.getSQL(),select.getBindValues().toArray());

        return ans == 0;
    }

    @Override
    public List<MailMessage> getPendingMessages() {
        Table root = MAIL_MESSAGE.as("root");
        Table templ = MAIL_TEMPLATE.as("tmpl");


        Select select = context.select(root.field(MAIL_MESSAGE.ID),
                root.field(MAIL_MESSAGE.CREATION_DATE),
                root.field(MAIL_MESSAGE.SENDING_DATE),
                root.field(MAIL_MESSAGE.STATUS_ID),
                root.field(MAIL_MESSAGE.RECIPIENT_USER_ID),
                templ.field(MAIL_TEMPLATE.ID).as("mail_template_id"),
                templ.field(MAIL_TEMPLATE.CODE),
                templ.field(MAIL_TEMPLATE.NAME_RU),
                templ.field(MAIL_TEMPLATE.NAME_KZ),
                templ.field(MAIL_TEMPLATE.CONFIGURATION_TYPE_ID),
                templ.field(MAIL_TEMPLATE.SUBJECT),
                templ.field(MAIL_TEMPLATE.TEXT)
        ).from(root).join(templ)
                .on(root.field(MAIL_MESSAGE.MAIL_TEMPLATE_ID).eq(templ.field(MAIL_TEMPLATE.ID)))
                .where(root.field(MAIL_MESSAGE.STATUS_ID).eq(((long) MailMessageStatuses.PROCESSING)))
                .limit(30);

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<MailMessage> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            MailMessage r = new MailMessage();
            r.setId(((BigDecimal)row.get("ID")).longValue());
            r.setCreationDate((Date) row.get("CREATION_DATE"));
            r.setSendingDate((Date) row.get("SENDING_DATE"));
            r.setStatusId(((BigDecimal) row.get("STATUS_ID")).intValue());
            r.setRecipientUserId(((BigDecimal) row.get("RECIPIENT_USER_ID")).longValue());
            r.setMailTemplate(readMailTemplate(row));
            ret.add(r);
        }

        return ret;
    }

    @Override
    public boolean isMailHandlingOn() {
        Select select = context.select(DSL.count())
                .from(EAV_A_SYSCONFIG)
                .where(EAV_A_SYSCONFIG.KEY_.eq("IS_MAIL_HANDLING_ON"))
                .and(EAV_A_SYSCONFIG.VALUE_.eq("1"));

        int ans = jdbcTemplate.queryForInt(select.getSQL(),select.getBindValues().toArray());

        return ans > 0;
    }

    @Override
    public List<MailTemplate> getUserConfiguredTemplates() {
        Select select = context.selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.CONFIGURATION_TYPE_ID.eq(((long) MailConfigurationTypes.USER_SET)));


        List<MailTemplate> ret = jdbcTemplate.query(select.getSQL(), select.getBindValues().toArray(),
                new BeanPropertyRowMapper(MailTemplate.class));

        return ret;
    }

    @Override
    public void insertUserMailTemplate(UserMailTemplate userMailTemplate) {
        Insert insert = context.insertInto(MAIL_USER_MAIL_TEMPLATE)
                .set(MAIL_USER_MAIL_TEMPLATE.PORTAL_USER_ID, userMailTemplate.getPortalUserId())
                .set(MAIL_USER_MAIL_TEMPLATE.MAIL_TEMPLATE_ID, userMailTemplate.getMailTemplate().getId())
                .set(MAIL_USER_MAIL_TEMPLATE.ENABLED, DataUtils.convert(true));

        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
    }
}
