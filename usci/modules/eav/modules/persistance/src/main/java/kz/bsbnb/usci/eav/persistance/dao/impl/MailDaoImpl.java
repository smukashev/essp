package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.mail.*;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class MailDaoImpl extends JDBCSupport implements IMailDao {

    @Autowired
    DSLContext context;

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
            r.setStatus(MailMessageStatus.getFromInt(((BigDecimal) row.get("STATUS_ID")).intValue()));
            //r.setStatus(MailMessageStatus.SENT);
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
}
