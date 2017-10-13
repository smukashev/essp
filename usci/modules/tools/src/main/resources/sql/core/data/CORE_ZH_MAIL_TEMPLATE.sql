INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (5001, 'ЕССП. Некорректные исторические записи', 'Текст запроса: %QUERY_TEXT%. <br/><br/>Результат: %RESULT%', 'HISTORY_TABLES', 'Исторические записи', 'Исторические записи', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (3001, 'ЕССП. Утверждение данных. %CREDITOR% - %REPORT_DATE%', 'Организация: <b>%CREDITOR%.</b><br/> 
Отчетная дата: <b>%REPORT_DATE%.</b><br/>
Статус: <b>%STATUS%.</b><br/>
Пользователь <b>%USERNAME%</b> внес изменения.<br/>
Время: <b>%UPDATE_TIME%.</b><br/>
Текст сообщения: %TEXT%.<br/>
<hr/>
Сообщения об утверждении/разутверждении данных отправляются автоматически.<br/>
Вы можете отказаться от получения уведомлений об утверждении/разутверждении данных на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'APPROVAL_UPDATE', 'Утверждение данных', 'Утверждение данных', 138);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (4001, 'ЕССП. Не идет обработка файлов', 'Состояние очереди не изменялось в течении %TIMEOUT% секунд. 
Количество файлов в очереди: %FILES_IN_QUEUE%. 
Время последней отправки файла в обработку: %LAST_SEND_TIME%. 
Количество записей протокола по обрабатываемым файлам: %PROTOCOLS_COUNT%', 'QUEUE_MONITOR', 'Очередь', 'Очередь', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (2001, 'ЕССП. Данные утверждены', 'Организация: <b> %ORG%.</b><br/>
Отчетная дата: <b>%REPORT_DATE%.</b><br/>
Статус: <b>%STATUS%</b><br/>
<hr/>
Сообщения об утверждении/разутверждении данных отправляются автоматически.<br/>
Вы можете отказаться от получения уведомлений об утверждении/разутверждении данных на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'REPORT_APPROVED', 'Данные утверждены', 'Данные утверждены', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (2002, 'ЕССП. Одобрена обработка изменений в утвержденном периоде', 'Данные успешно одобрены. <br/> Организация: <b> %ORG%.</b><br/>
Файлы: <b>%FILE_NAMES%</b><br/>
<hr/>
 Подробнее смотрите в протоколе', 'MAINTENANCE_APPROVED', 'Одобрена обработка изменений в утвержденном периоде', 'Одобрена обработка изменений в утвержденном периоде', 138);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (2004, 'ЕССП. Отклонена обработка изменений в утвержденном периоде', 'Данные отклонены. <br/> Организация: <b> %ORG%.</b><br/>
Файлы: <b>%FILE_NAMES%</b><br/>
<hr/>
 Подробнее смотрите в протоколе', 'MAINTENANCE_DECLINED', 'Отклонена обработка изменений в утвержденном периоде', 'Отклонена обработка изменений в утвержденном периоде', 138);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (2003, 'ЕССП. Запрос на изменение за утвержденый период', 'Отчетная дата: <b> %REPORT_DATE%.</b><br/>
Организация: <b> %ORG%.</b><br/>
Файл: <b>%FILE_NAME%</b><br/>
<hr/>
 Подробнее смотрите в протоколе', 'MAINTENANCE_REQUEST', 'Запрос на изменение за утвержденый период', 'Запрос на изменение за утвержденый период', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (1001, 'ЕССП. Обработка файла завершена', 'Обработка файла <b>%FILENAME%</b> завершена
<hr/>
Сообщения о завершении обработки файлов отправляются автоматически.<br/>
Вы можете отказаться от получения уведомлений о завершении обработки файлов на странице "Почтовые уведомления" портала (во вкладке "Настройка почтовых уведомлений") ', 'FILE_PROCESSING_COMPLETED', 'Обработка завершена', 'Обработка завершена', 138);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (5002, '%SUBJECT%', '%TEXT%', 'GENERIC_TEMPLATE', 'Уведомление', 'Уведомление', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (3002, 'Дублирующиеся записи в выходной форме', '%DETAILS%', 'DUPLICATE_IN_OUTPUT_FORM', 'Дублирующиеся записи в выходной форме', 'Дублирующиеся записи в выходной форме', 137);
INSERT INTO MAIL_TEMPLATE (ID, SUBJECT, TEXT, CODE, NAME_RU, NAME_KZ, CONFIGURATION_TYPE_ID) VALUES (4002, 'ЕССП. Формирование отчета %REPORT_NAME% завершено', 'Сформирован отчет <b>%REPORT_NAME%</b> <br/> Параметры:<br> %PARAMETERS%<br/>Время начала формирования: %BEGIN_DATE%.<br/> Время завершения формирования: %END_DATE%.', 'REPORT_GENERATION_FINISHED', 'Уведомление о завершении формирования отчета', 'Уведомление о завершении формирования отчета', 138);