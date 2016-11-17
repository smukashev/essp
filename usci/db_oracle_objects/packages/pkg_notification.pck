create or replace package pkg_notification is

  procedure send_approval_notification(p_creditor_id   in number,
                                      p_creditor_name in varchar2, 
                                      p_user_name     in varchar2,
                                      p_report_date   in varchar2);


  procedure send_dev_notification(p_title in varchar2, p_text in varchar2);

  procedure speed_test;

end pkg_notification;
/

create or replace package body pkg_notification is
 -- Function and procedure implementations
  procedure send_approval_notification(p_creditor_id   in number,
                                       p_creditor_name in varchar2, 
                                       p_user_name     in varchar2,
                                       p_report_date   in varchar2) 
    
is
  v_mail_message_id number;
  v_approval_date date;
begin
v_approval_date := sysdate;
  for cr in (select user_id from eav_a_creditor_user where creditor_id = p_creditor_id) 
  loop
    insert 
      into mail_message (recipient_user_id,
                         status_id,
                         mail_template_id,
                         creation_date)
                 values  (cr.user_id,
                          132,
                          3001,
                          v_approval_date)
                 returning ID into v_mail_message_id;
                 
    insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2001,
                                       p_creditor_name);
                                       
    insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2002,
                                       'Утвержден'); 
                                       
                                       
    insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2003,
                                       p_user_name);    
                                                                        
     insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2004,
                                       p_report_date);
                                       
      insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2005,
                                       to_char(v_approval_date,'dd.MM.yy hh24:mi:ss'));
                                       
      insert into mail_message_parameter(mail_message_id,
                                       mail_template_parameter_id,
                                       value)
                             values   (v_mail_message_id,
                                       2006,
                                       'Статус изменен на Утвержден'); 
                                       
      commit;                                      
      
  end loop;  
end;

   procedure send_dev_notification(p_title in varchar2, p_text in varchar2)
      is
         v_mail_message_id number;
      begin
            insert into mail_message (recipient_user_id,
                               status_id,
                               mail_template_id,
                               creation_date)
                       values  (129405,
                                132,
                                3001,
                                sysdate)
                       returning ID into v_mail_message_id;


            insert into mail_message_parameter(mail_message_id,
                                   mail_template_parameter_id,
                                   value)
                         values   (v_mail_message_id,
                                   2001,
                                   p_title);

            insert into mail_message_parameter(mail_message_id,
                                          mail_template_parameter_id,
                                          value)
            values   (v_mail_message_id, 2006, p_text);
      end;


    procedure speed_test
    is
        v_max_id1 number;
        v_max_id2 number;
    begin
        select max(id) into v_max_id1
          from eav_entity_statuses;

        dbms_lock.sleep(60);

        select max(id) into v_max_id2
          from eav_entity_statuses;

        send_dev_notification('speed test', 'Обработано ' || (v_max_id2 - v_max_id1) || '/min, maxId = ' || v_max_id2);
    end;

end pkg_notification;
/

