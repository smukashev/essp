INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (1, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_debtor_type', 'Вид (роль) субъекта кредитной истории', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (2, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_doc_type', 'Вид идентификационных документов', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (3, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'document', 'Документ', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (4, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_subject_type', 'Тип организации выкупившей (принявший) заем (условное обязательство)', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (5, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_balance_account', 'Номера балансовых счетов', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (6, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'person_name', 'Имя', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (7, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_country', 'Страна заемщика', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (8, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_creditor', 'БВУ/НО', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (9, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_region', 'Области', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (10, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_contact_type', 'Вид связи', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (11, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'provision', 'Информация о резервах (провизиях) по неоднородным кредитам', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (12, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'head', 'Руководитель юридического лица', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (13, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'address', 'Адрес', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (14, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'contact', 'Контакты', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (15, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_debt_pastdue', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (16, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_debt_current', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (17, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_debt_write_off', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (18, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_interest_pastdue', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (19, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_interest_current', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (20, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_interest_write_off', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (21, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_limit', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (22, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_debt', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (23, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_interest', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (24, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_correction', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (25, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_discount', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (26, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains_discounted_value', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (27, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'turnover_issue_debt', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (28, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'turnover_issue_interest', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (29, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'provision_group', 'Группа провизий', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (30, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_bank_relation', 'Признак связанности с банком особыми отношениями', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (31, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_classification', 'Классификационная категория займа и условного обязательства', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (32, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_econ_trade', 'Вид экономической деятельности', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (33, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_enterprise_type', 'Код субъекта частного предпринимательства', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (34, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_legal_form', 'Организационно-правовая форма', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (35, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_offshore', 'Оффшорные зоны', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (36, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_portfolio', 'Портфель однородных кредитов', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (37, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'organization_name', 'Наименование', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (38, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'bank_relation', 'Связь с банком', 0, 1, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (39, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'organization_info', 'организация', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (40, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'person_info', 'Информация по физическим лицам', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (41, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'creditor_info', 'кредитор', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (42, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'subject', 'субъект кредитной истории', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (43, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'remains', 'Остатки', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (44, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'turnover_issue', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (45, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'turnover', 'Информация о движении средств за отчетный период', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (46, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'credit_flow', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (47, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'contract', 'Контракт', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (48, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'portfolio', 'Портфель', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (49, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_creditor_branch', 'БВУ/НО (филиалы)', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (50, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_credit_object', 'Объект кредитования', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (51, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_credit_purpose', 'Цель кредитования', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (52, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_currency', 'Вид валюты', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (53, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_finance_source', 'Источник финансирования организации, выдавшей заем', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (54, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_pledge_type', 'Вид обеспечения', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (55, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_credit_type', 'Вид займа и условного обязательства', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (56, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'pledge', 'Залоги', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (57, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'change', null, 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (58, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'primary_contract', 'Договор', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (59, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'credit', 'Заем/условное обязательство', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (60, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'portfolio_flow_detail', 'Детали', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (61, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'portfolio_flow_kfn', 'Портфель', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (62, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'portfolio_flow_msfo', 'Портфель мсфо', 1, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (63, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'portfolio_data', 'Портфель', 0, 0, 0);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (64, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_econ_sector', 'Сектор экономики', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (65, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_ba_ct', 'соответствие балансовый счет - тип кредита', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (66, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_debt_remains_type', 'виды остатков', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (67, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_ba_drt', 'соответствие балансовый счет - тип остатка', 0, 0, 1);
INSERT INTO EAV_M_CLASSES (ID, COMPLEX_KEY_TYPE, BEGIN_DATE, IS_DISABLED, NAME, TITLE, PARENT_IS_KEY, IS_CLOSABLE, IS_REFERENCE) VALUES (68, 'ALL', TO_DATE('2017-09-04 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 0, 'ref_exclusive_doc', 'Исключительный документ', 0, 0, 1);