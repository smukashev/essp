create or replace package pkg_notification is

  procedure send_approval_notification(p_creditor_id   in number,
                                      p_creditor_name in varchar2, 
                                      p_user_name     in varchar2,
                                      p_report_date   in varchar2);


  procedure send_dev_notification(p_title in varchar2, p_text in varchar2);

  procedure speed_test;

  procedure approval_scanner;

  procedure error_scanner;

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

    procedure approval_scanner
    is
       v_report_date date;
       v_org_name varchar2(2000);
       v_approved_cnt number;
       v_total_cnt number;
       v_title varchar2(2000);
       v_body varchar2(2000);
    begin

        for cr in ( select id from mail_message where recipient_user_id = 15854 and mail_template_id = 3001 and creation_date > ( sysdate - 1/24))
        loop

           select value
             into v_org_name
             from mail_message_parameter
            where mail_message_id = cr.id and mail_template_parameter_id = 2001;

           select to_date(value, 'dd.MM.yyyy')
             into v_report_date
             from mail_message_parameter
            where mail_message_id = cr.id and mail_template_parameter_id = 2004;

           select count(1) into v_approved_cnt
             from eav_report
            where report_date = v_report_date
              and status_id in (5,6,7);

           select count(1) into v_total_cnt
             from eav_report
            where report_date = v_report_date;

            send_dev_notification('approval_scanner ' || v_org_name || ', дата=' || to_char(v_report_date,'dd.MM.yyyy'), 'Количество: ' || v_approved_cnt || '/' || v_total_cnt);
        end loop;
    end;

    /*
     Requirement:
     grant select on v_$instance to core
    */
    procedure error_scanner
    is
       v_scan_time date;
       v_procedure_name varchar2(200);
       v_start_time date;
       v_res_text varchar2(4000);
       v_detail varchar2(4000);
       v_exec_time number;
    begin
        v_procedure_name:= 'error_scanner';
        v_scan_time := sysdate - 1 / 24;
        v_start_time := systimestamp;

        select v_procedure_name || '@' ||instance_name
          into v_procedure_name
         from sys.v_$instance;

         v_detail := chr(13) || 'details: ' || chr(13);

        for cr in ( select error_code, count(1) cnt, max(id) mid from eav_entity_statuses where error_code is not null and receipt_date > v_scan_time group by error_code)
        loop
           select v_res_text || cr.error_code ||  ': ' || cr.cnt || ' times' || chr(13)
             into v_res_text
             from dual;


             v_detail:= v_detail || cr.error_code || ': ';

          select v_detail || 'batch_id= ' || s.batch_id || ', file_name= ' || b.file_name || ', index=' || s.index_
                          || ', description = ' || s.description || ',dev_description=' || s.dev_description || chr(13)
            into v_detail
            from eav_entity_statuses s,
                 eav_batches b
            where s.id = cr.mid
              and s.batch_id = b.id;
        end loop;

        v_res_text := v_res_text || v_detail;

        select extract( day from diff )*24*60*60*1000 +
           extract( hour from diff )*60*60*1000 +
           extract( minute from diff )*60*1000 +
           round(extract( second from diff )*1000) total_milliseconds
           into v_exec_time
      from (select (systimestamp - v_start_time)  diff
             from dual);

        v_res_text := v_res_text || chr(13) || ' executed in ' || v_exec_time || ' ms';
        pkg_notification.send_dev_notification@messp(v_procedure_name, v_res_text);

    exception
    when others then
        pkg_notification.send_dev_notification@messp(v_procedure_name || ' error ' || SQLERRM, '');
    end;

end pkg_notification;
/

