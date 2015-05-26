update eav_m_classes set title = 	'Номера балансовых счетов'	Where name = 'ref_balance_account';
update eav_m_classes set title = 	'Признак связанности с банком особыми отношениями'	WHERE NAME = 'ref_bank_relation';
update eav_m_classes set title = 	'Классификационная категория займа и условного обязательства'	WHERE NAME = 'ref_classification';
update eav_m_classes set title = 	'Вид связи'	WHERE NAME = 'ref_contact_type';
update eav_m_classes set title = 	'Страна заемщика'	WHERE NAME = 'ref_country';
update eav_m_classes set title = 	'Объект кредитования'	WHERE NAME = 'ref_credit_object';
update eav_m_classes set title = 	'Цель кредитования'	WHERE NAME = 'ref_credit_purpose';
update eav_m_classes set title = 	'Вид займа и условного обязательства'	WHERE NAME = 'ref_credit_type';
update eav_m_classes set title = 	'БВУ/НО'	WHERE NAME = 'ref_creditor';
update eav_m_classes set title = 	'БВУ/НО (филиалы)'	WHERE NAME = 'ref_creditor_branch';
update eav_m_classes set title = 	'Вид валюты'	WHERE NAME = 'ref_currency';
update eav_m_classes set title = 	'Вид (роль) субъекта кредитной истории'	WHERE NAME = 'ref_debtor_type';
update eav_m_classes set title = 	'Вид идентификационных документов'	WHERE NAME = 'ref_doc_type';
update eav_m_classes set title = 	'Вид экономической деятельности'	WHERE NAME = 'ref_econ_sector';
update eav_m_classes set title = 	'Источник финансирования организации, выдавшей заем'	WHERE NAME = 'ref_econ_trade';
update eav_m_classes set title = 	'Организационно-правовая форма'	WHERE NAME = 'ref_enterprise_type';
update eav_m_classes set title = 	'Оффшорные зоны'	WHERE NAME = 'ref_offshore'; --'ref_finance_source';
update eav_m_classes set title = 	'Вид обеспечения'	WHERE NAME = 'ref_legal_form';
update eav_m_classes set title = 	'Портфель однородных кредитов'	WHERE NAME = 'ref_portfolio';
update eav_m_classes set title = 	'Вид обеспечения'	WHERE NAME = 'ref_pledge_type';
--update eav_m_classes set title = 	'Тип организации выкупившей (принявший) заем (условное обязательство)'	WHERE NAME = 'ref_portfolio';
update eav_m_classes set title = 	'Области'	WHERE NAME = 'ref_region';
update eav_m_classes set title = 	'Тип организации выкупившей (принявший) заем (условное обязательство)'	WHERE NAME = 'ref_subject_type';
update eav_m_classes set title = 	'Договор займа/условного обязательства(кредит)'	WHERE NAME = 'credit';
update eav_m_classes set title = 	'Физическое лицо'	WHERE NAME = 'person';
update eav_m_classes set title = 	'Юридическое лицо'	WHERE NAME = 'organization';
update eav_m_classes set title = 	'Договор'	WHERE NAME = 'primary_contract';

update eav_m_simple_attributes set title = 'номер' where name='no';
update eav_m_simple_attributes set title = 'дата' where name='date';

update eav_m_complex_attributes set title = 'тип субъекта' where name = 'subject_type';

update eav_m_complex_set set title = 'документы' where name = 'docs';
