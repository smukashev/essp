update eav_m_classes set title='Контакты' where name='contact';
update eav_m_classes set title='' where name='remains_correction';
update eav_m_classes set title='Наименование' where name='organization_name';
update eav_m_classes set title='Портфель мсфо' where name='portfolio_flow_msfo';
update eav_m_classes set title='Портфель' where name='portfolio_data';
update eav_m_classes set title='Вид связи' where name='ref_contact_type';
update eav_m_classes set title='' where name='remains_debt_current';
update eav_m_classes set title='' where name='remains_discounted_value';
update eav_m_classes set title='' where name='turnover_issue_interest';
update eav_m_classes set title='Признак связанности с банком особыми отношениями' where name='ref_bank_relation';
update eav_m_classes set title='Вид экономической деятельности' where name='ref_econ_trade';
update eav_m_classes set title='Оффшорные зоны' where name='ref_offshore';
update eav_m_classes set title='БВУ/НО (филиалы)' where name='ref_creditor_branch';
update eav_m_classes set title='Договор займа/условного обязательства(кредит)' where name='credit';
update eav_m_classes set title='Вид идентификационных документов' where name='ref_doc_type';
update eav_m_classes set title='Номера балансовых счетов' where name='ref_balance_account';
update eav_m_classes set title='Страна заемщика' where name='ref_country';
update eav_m_classes set title='Классификационная категория займа и условного обязательства' where name='ref_classification';
update eav_m_classes set title='Объект кредитования' where name='ref_credit_object';
update eav_m_classes set title='Залоги' where name='pledge';
update eav_m_classes set title='Портфель' where name='portfolio_flow_kfn';
update eav_m_classes set title='Тип организации выкупившей (принявший) заем (условное обязательство)' where name='ref_subject_type';
update eav_m_classes set title='Области' where name='ref_region';
update eav_m_classes set title='Адрес' where name='address';
update eav_m_classes set title='' where name='remains_interest_write_off';
update eav_m_classes set title='' where name='remains_limit';
update eav_m_classes set title='Группа провизий' where name='provision_group';
update eav_m_classes set title='Остатки' where name='remains';
update eav_m_classes set title='Цель кредитования' where name='ref_credit_purpose';
update eav_m_classes set title='Вид займа и условного обязательства' where name='ref_credit_type';
update eav_m_classes set title='' where name='change';
update eav_m_classes set title='Провизия' where name='provision';
update eav_m_classes set title='' where name='remains_interest_pastdue';
update eav_m_classes set title='' where name='remains_debt';
update eav_m_classes set title='Организационно-правовая форма' where name='ref_legal_form';
update eav_m_classes set title='Связь с банком' where name='bank_relation';
update eav_m_classes set title='' where name='turnover';
update eav_m_classes set title='' where name='credit_flow';
update eav_m_classes set title='Контракт' where name='contract';
update eav_m_classes set title='Портфель' where name='portfolio';
update eav_m_classes set title='Вид обеспечения' where name='ref_pledge_type';
update eav_m_classes set title='соответсвтие балансовый счет - тип креидита' where name='ref_ba_ct';
update eav_m_classes set title='Вид (роль) субъекта кредитной истории' where name='ref_debtor_type';
update eav_m_classes set title='' where name='remains_debt_write_off';
update eav_m_classes set title='' where name='remains_interest';
update eav_m_classes set title='Портфель однородных кредитов' where name='ref_portfolio';
update eav_m_classes set title='' where name='turnover_issue';
update eav_m_classes set title='Договор' where name='primary_contract';
update eav_m_classes set title='Директор' where name='head';
update eav_m_classes set title='' where name='remains_discount';
update eav_m_classes set title='' where name='turnover_issue_debt';
update eav_m_classes set title='Вид валюты' where name='ref_currency';
update eav_m_classes set title='Сектор экономики' where name='ref_econ_sector';
update eav_m_classes set title='соответсвтие балансовый счет - тип остатка' where name='ref_ba_drt';
update eav_m_classes set title='Исключительный документ' where name='ref_exclusive_doc';
update eav_m_classes set title='Документ' where name='document';
update eav_m_classes set title='Имя' where name='person_name';
update eav_m_classes set title='БВУ/НО' where name='ref_creditor';
update eav_m_classes set title='' where name='remains_debt_pastdue';
update eav_m_classes set title='' where name='remains_interest_current';
update eav_m_classes set title='Код субъекта частного предпринимательства' where name='ref_enterprise_type';
update eav_m_classes set title='организация' where name='organization_info';
update eav_m_classes set title='физ лицо' where name='person_info';
update eav_m_classes set title='кредитор' where name='creditor_info';
update eav_m_classes set title='субъект кредитной истории' where name='subject';
update eav_m_classes set title='Источник финансирования организации, выдавшей заем' where name='ref_finance_source';
update eav_m_classes set title='Детали' where name='portfolio_flow_detail';
update eav_m_classes set title='виды остатков' where name='ref_debt_remains_type';

update eav_m_simple_attributes set title='Признак идентификационности' where name='is_identification';
update eav_m_simple_attributes set title='Признак принадлежности к физическим лицам' where name='is_person_doc';
update eav_m_simple_attributes set title='Шестое значение НПС' where name='sixth_sign';
update eav_m_simple_attributes set title='Язык' where name='lang';
update eav_m_simple_attributes set title='Отчество' where name='middlename';
update eav_m_simple_attributes set title='Значение' where name='value';
update eav_m_simple_attributes set title='Детали' where name='details';
update eav_m_simple_attributes set title='Дата закрытия' where name='close_date';
update eav_m_simple_attributes set title='Комментарий на русском' where name='comment_ru';
update eav_m_simple_attributes set title='Размер резерва (провизии) по займу, за исключением портфеля' where name='provision_debt';
update eav_m_simple_attributes set title='Краткое наименование на русском' where name='short_name_ru';
update eav_m_simple_attributes set title='Краткое наименование' where name='short_name';
update eav_m_simple_attributes set title='Тип' where name='type';
update eav_m_simple_attributes set title='Входящее значение' where name='is_input_value';
update eav_m_simple_attributes set title='Рейтинг' where name='rating';
update eav_m_simple_attributes set title='Контракт' where name='contract';
update eav_m_simple_attributes set title='Дата погашения' where name='maturity_date';
update eav_m_simple_attributes set title='Номер' where name='no_';
update eav_m_simple_attributes set title='Краткое наименование на казахском' where name='short_name_kz';
update eav_m_simple_attributes set title='Дата открытия' where name='open_date';
update eav_m_simple_attributes set title='Дата' where name='date';
update eav_m_simple_attributes set title='Комментарий на казахском' where name='comment_kz';
update eav_m_simple_attributes set title='Максимальный размер резерва (провизии)' where name='provision_portfolio_max';
update eav_m_simple_attributes set title='Признак конвертируемости' where name='is_convertible';
update eav_m_simple_attributes set title='Значение дискаунта' where name='discounted_value';
update eav_m_simple_attributes set title='Наименование (каз.)' where name='name_kz';
update eav_m_simple_attributes set title='Четырехзначный номер НПС' where name='first_fourth_signs';
update eav_m_simple_attributes set title='Имя' where name='firstname';
update eav_m_simple_attributes set title='Ликвидность' where name='is_liquid_all';
update eav_m_simple_attributes set title='Дата выпуска' where name='actual_issue_date';
update eav_m_simple_attributes set title='Дата погашения займа' where name='contract_maturity_date';
update eav_m_simple_attributes set title='Годовая процентная ставка' where name='interest_rate_yearly';
update eav_m_simple_attributes set title='Допустимое количество знаков' where name='sign_count';
update eav_m_simple_attributes set title='Признак принадлежности к юридическим лицам' where name='is_organization_doc';
update eav_m_simple_attributes set title='Наименование (рус.)' where name='name_ru';
update eav_m_simple_attributes set title='Номер' where name='display_no';
update eav_m_simple_attributes set title='В балансе' where name='is_in_balance';
update eav_m_simple_attributes set title='Номер' where name='code_numeric';
update eav_m_simple_attributes set title='физ лицо' where name='is_person';
update eav_m_simple_attributes set title='кредитор' where name='is_creditor';
update eav_m_simple_attributes set title='Валюта' where name='value_currency';
update eav_m_simple_attributes set title='Сумма' where name='amount';
update eav_m_simple_attributes set title='Дата пролонгации' where name='prolongation_date';
update eav_m_simple_attributes set title='Продолжительность отчетного периода в месяцах' where name='report_period_duration_months';
update eav_m_simple_attributes set title='Код' where name='code';
update eav_m_simple_attributes set title='Вид' where name='kind_id';
update eav_m_simple_attributes set title='Пятое значение НПС' where name='fifth_sign';
update eav_m_simple_attributes set title='Седьмое значение НПС' where name='seventh_sign';
update eav_m_simple_attributes set title='Фамилия' where name='lastname';
update eav_m_simple_attributes set title='Валюта' where name='amount_currency';
update eav_m_simple_attributes set title='Значение' where name='weight';
update eav_m_simple_attributes set title='Номер' where name='no';
update eav_m_simple_attributes set title='Наименование' where name='name';
update eav_m_simple_attributes set title='Код' where name='nokbdb_code';
update eav_m_simple_attributes set title='Признак юридического лица' where name='is_organization';
update eav_m_simple_attributes set title='Признак индивидуального предпринимателя' where name='is_se';
update eav_m_simple_attributes set title='Высоколиквидное обеспечение для инвестиционных займов' where name='is_liquid_invest';
update eav_m_simple_attributes set title='has_currency_earn' where name='has_currency_earn';
update eav_m_simple_attributes set title='Значение' where name='value_';
update eav_m_simple_attributes set title='Минимальный размер резерва (провизии)' where name='provision_portfolio_min';

update eav_m_complex_attributes set title='Тип контакта' where name='contact_type';
update eav_m_complex_attributes set title='БВУ/НО' where name='creditor';
update eav_m_complex_attributes set title='Признак связанности с банком особыми отношениями' where name='bank_relation';
update eav_m_complex_attributes set title='Портфель однородных кредитов' where name='portfolio';
update eav_m_complex_attributes set title='Остатки' where name='remains';
update eav_m_complex_attributes set title='Источник финансирования организации, выдавшей заем' where name='finance_source';
update eav_m_complex_attributes set title='Текущий' where name='current';
update eav_m_complex_attributes set title='Оффшорная зона' where name='offshore';
update eav_m_complex_attributes set title='БВУ/НО' where name='creditor_info';
update eav_m_complex_attributes set title='Вид обеспечения' where name='pledge_type';
update eav_m_complex_attributes set title='credit_flow' where name='credit_flow';
update eav_m_complex_attributes set title='turnover' where name='turnover';
update eav_m_complex_attributes set title='Договор' where name='primary_contract';
update eav_m_complex_attributes set title='виды остатков' where name='debt_remains_type';
update eav_m_complex_attributes set title='write_off' where name='write_off';
update eav_m_complex_attributes set title='Директор' where name='head';
update eav_m_complex_attributes set title='Организационно-правовая форма' where name='legal_form';
update eav_m_complex_attributes set title='Классификация' where name='classification';
update eav_m_complex_attributes set title='БВУ/НО (филиал)' where name='creditor_branch';
update eav_m_complex_attributes set title='физ лицо' where name='person_info';
update eav_m_complex_attributes set title='Значение дискаунта' where name='discounted_value';
update eav_m_complex_attributes set title='Главный офис' where name='main_office';
update eav_m_complex_attributes set title='Субьект' where name='subject';
update eav_m_complex_attributes set title='Тип документа' where name='doc_type';
update eav_m_complex_attributes set title='Провизия мсфо' where name='provision_msfo';
update eav_m_complex_attributes set title='provision_msfo_over_balance' where name='provision_msfo_over_balance';
update eav_m_complex_attributes set title='Портфель однородных кредитов мсфо' where name='portfolio_msfo';
update eav_m_complex_attributes set title='Объект кредитования' where name='credit_object';
update eav_m_complex_attributes set title='Тип организации' where name='subject_type';
update eav_m_complex_attributes set title='pastdue' where name='pastdue';
update eav_m_complex_attributes set title='Провизия кфн' where name='provision_kfn';
update eav_m_complex_attributes set title='enterprise_type' where name='enterprise_type';
update eav_m_complex_attributes set title='Организация' where name='organization_info';
update eav_m_complex_attributes set title='issue' where name='issue';
update eav_m_complex_attributes set title='Контракт' where name='contract';
update eav_m_complex_attributes set title='Балансовый счет' where name='balance_account';
update eav_m_complex_attributes set title='Страна' where name='country';
update eav_m_complex_attributes set title='Долг' where name='debt';
update eav_m_complex_attributes set title='discount' where name='discount';
update eav_m_complex_attributes set title='limit' where name='limit';
update eav_m_complex_attributes set title='Вид займа и условного обязательства' where name='credit_type';
update eav_m_complex_attributes set title='Область' where name='region';
update eav_m_complex_attributes set title='Вид экономической деятельности' where name='econ_trade';
update eav_m_complex_attributes set title='Коррекция' where name='correction';
update eav_m_complex_attributes set title='процентная ставка' where name='interest';
update eav_m_complex_attributes set title='Провизия' where name='provision';
update eav_m_complex_attributes set title='Цель кредитования' where name='credit_purpose';
update eav_m_complex_attributes set title='Валюта' where name='currency';
update eav_m_complex_attributes set title='Изменение' where name='change';

update eav_m_complex_set  set title='Наименование' where name='names';
update eav_m_complex_set  set title='Займы' where name='pledges';
update eav_m_complex_set  set title='Детали' where name='details';
update eav_m_complex_set  set title='Документы' where name='docs';
update eav_m_complex_set  set title='Портфолио' where name='portfolio_flows_kfn';
update eav_m_complex_set  set title='Портфолио мсфо' where name='portfolio_flows_msfo';
update eav_m_complex_set  set title='Признак связанности с банком особыми отношениями' where name='bank_relations';
update eav_m_complex_set  set title='Адрес' where name='addresses';
update eav_m_complex_set  set title='Контакты' where name='contacts';

