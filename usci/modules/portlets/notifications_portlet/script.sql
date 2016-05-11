insert into EAV_A_SYSCONFIG(key_,value_) values ('IS_MAIL_HANDLING_ON', '1')

insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (5001, 'Кредитный регистр. Некорректные исторические записи', 'Текст запроса: %QUERY_TEXT%. <br/><br/>Результат: %RESULT%', 'HISTORY_TABLES', 'Исторические записи', 'Исторические записи', 137);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (3001, 'Кредитный регистр. Утверждение данных. %CREDITOR% - %REPORT_DATE%', 'Организация: <b>%CREDITOR%.</b><br/> ' || chr(10) || 'Отчетная дата: <b>%REPORT_DATE%.</b><br/>' || chr(10) || 'Статус: <b>%STATUS%.</b><br/>' || chr(10) || 'Пользователь <b>%USERNAME%</b> внес изменения.<br/>' || chr(10) || 'Время: <b>%UPDATE_TIME%.</b><br/>' || chr(10) || 'Текст сообщения: %TEXT%.<br/>' || chr(10) || '<hr/>' || chr(10) || 'Сообщения об утверждении/разутверждении данных отправляются автоматически.<br/>' || chr(10) || 'Вы можете отказаться от получения уведомлений об утверждении/разутверждении данных на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'APPROVAL_UPDATE', 'Утверждение данных', 'Утверждение данных', 138);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (4001, 'Кредитный регистр. Не идет обработка файлов', 'Состояние очереди не изменялось в течении %TIMEOUT% секунд. ' || chr(10) || 'Количество файлов в очереди: %FILES_IN_QUEUE%. ' || chr(10) || 'Время последней отправки файла в обработку: %LAST_SEND_TIME%. ' || chr(10) || 'Количество записей протокола по обрабатываемым файлам: %PROTOCOLS_COUNT%', 'QUEUE_MONITOR', 'Очередь', 'Очередь', 137);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (2001, 'Кредитный регистр. Данные утверждены', 'Организация: <b> %ORG%.</b><br/>' || chr(10) || 'Отчетная дата: <b>%REPORT_DATE%.</b><br/>' || chr(10) || 'Статус: <b>%STATUS%</b><br/>' || chr(10) || '<hr/>' || chr(10) || 'Сообщения об утверждении/разутверждении данных отправляются автоматически.<br/>' || chr(10) || 'Вы можете отказаться от получения уведомлений об утверждении/разутверждении данных на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'REPORT_APPROVED', 'Данные утверждены', 'Данные утверждены', 137);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (1001, 'Кредитный регистр. Обработка файла завершена', 'Обработка файла <b>%FILENAME%</b> завершена' || chr(10) || '<hr/>' || chr(10) || 'Сообщения о завершении обработки файлов отправляются автоматически.<br/>' || chr(10) || 'Вы можете отказаться от получения уведомлений о завершении обработки файлов на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'FILE_PROCESSING_COMPLETED', 'Обработка завершена', 'Обработка завершена', 138);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (5002, '%SUBJECT%', '%TEXT%', 'GENERIC_TEMPLATE', 'Уведомление', 'Уведомление', 137);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (3002, 'Дублирующиеся записи в выходной форме', '%DETAILS%', 'DUPLICATE_IN_OUTPUT_FORM', 'Дублирующиеся записи в выходной форме', 'Дублирующиеся записи в выходной форме', 137);
insert into MAIL_TEMPLATE (id, subject, text, code, name_ru, name_kz, configuration_type_id)
values (4002, 'Кредитный регистр. Формирование отчета %REPORT_NAME% завершено', 'Сформирован отчет <b>%REPORT_NAME%</b> <br/> Параметры:<br> %PARAMETERS%<br/>Время начала формирования: %BEGIN_DATE%.<br/> Время завершения формирования: %END_DATE%.', 'REPORT_GENERATION_FINISHED', 'Уведомление о завершении формирования отчета', 'Уведомление о завершении формирования отчета', 138);

insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4001, 5001, 'QUERY_TEXT', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4002, 5001, 'RESULT', 2);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2001, 3001, 'CREDITOR', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (3001, 4001, 'TIMEOUT', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (3002, 4001, 'FILES_IN_QUEUE', 2);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (3003, 4001, 'LAST_SEND_TIME', 3);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (3004, 4001, 'PROTOCOLS_COUNT', 4);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (1001, 2001, 'ORG', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (1002, 2001, 'REPORT_DATE', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (1003, 2001, 'STATUS', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2, 1001, 'FILENAME', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2002, 3001, 'STATUS', 2);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2003, 3001, 'USERNAME', 3);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2004, 3001, 'REPORT_DATE', 4);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2005, 3001, 'UPDATE_TIME', 5);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (2006, 3001, 'TEXT', 6);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4005, 4002, 'REPORT_NAME', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4006, 4002, 'PARAMETERS', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4007, 4002, 'BEGIN_DATE', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (4008, 4002, 'END_DATE', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (5001, 5002, 'SUBJECT', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (5002, 5002, 'TEXT', 1);
insert into MAIL_TEMPLATE_PARAMETER (id, mail_template_id, code, order_number)
values (3005, 3002, 'DETAILS', 1);


--enable 3 templates to all users

sql insert into MAIL_USER_MAIL_TEMPLATE(portal_user_id, mail_template_id, enabled) (select user_id as portal_user_id, 1001 as mail_template_id, 1 as enabled   from eav_a_user)
sql insert into MAIL_USER_MAIL_TEMPLATE(portal_user_id, mail_template_id, enabled) (select user_id as portal_user_id, 3001 as mail_template_id, 1 as enabled   from eav_a_user)
sql insert into MAIL_USER_MAIL_TEMPLATE(portal_user_id, mail_template_id, enabled) (select user_id as portal_user_id, 4002 as mail_template_id, 1 as enabled   from eav_a_user)


-- set status ORGANIZATION_APPROVED for january and february reports
update eav_report set status_id=7 where report_date = to_date('01.01.2016','dd.mm.yyyy');
update eav_report set status_id=7 where report_date = to_date('01.02.2016','dd.mm.yyyy');

-- find reports for january
select * from eav_report where report_date = to_date('01.01.2016','dd.mm.yyyy');

--insert february reports for januries
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2426,777,777, to_date('01.02.2016','dd.mm.yyyy'),to_date('01.02.2016','dd.mm.yyyy'), to_date('01.02.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2423,1,1, to_date('01.02.2016','dd.mm.yyyy'),to_date('01.02.2016','dd.mm.yyyy'), to_date('01.02.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2429,1,1, to_date('01.02.2016','dd.mm.yyyy'),to_date('01.02.2016','dd.mm.yyyy'), to_date('01.02.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2432,16,16, to_date('01.02.2016','dd.mm.yyyy'),to_date('01.02.2016','dd.mm.yyyy'), to_date('01.02.2016','dd.mm.yyyy'),7,'Неивестный');

--insert match reports for januries
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2426,777,777, to_date('01.03.2016','dd.mm.yyyy'),to_date('01.03.2016','dd.mm.yyyy'), to_date('01.03.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2423,1,1, to_date('01.03.2016','dd.mm.yyyy'),to_date('01.03.2016','dd.mm.yyyy'), to_date('01.03.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2429,1,1, to_date('01.03.2016','dd.mm.yyyy'),to_date('01.03.2016','dd.mm.yyyy'), to_date('01.03.2016','dd.mm.yyyy'),7,'Неивестный');
insert into eav_report(creditor_id,total_count, actual_count, beg_date,end_date,report_date,status_id,username)
values(2432,16,16, to_date('01.03.2016','dd.mm.yyyy'),to_date('01.03.2016','dd.mm.yyyy'), to_date('01.03.2016','dd.mm.yyyy'),7,'Неивестный');