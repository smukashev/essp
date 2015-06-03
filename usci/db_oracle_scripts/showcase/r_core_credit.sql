select
enr.report_date
, enr.id as credit_id
, c_creditor.entity_value_id as creditor_id
, s_creditor_code.value as creditor_code
, c_primary_contract.entity_value_id as primary_contract_id
, s_primary_contract_no.value as primary_contract_no
, s_primary_contract_date.value as primary_contract_date
, s_actual_issue_date.value as actual_issue_date
, s_amount.value as amount
, s_contract_maturity_date.value as contract_maturity_date
, s_has_currency_earn.value as has_currency_earn
, s_interest_rate_yearly.value as interest_rate_yearly
, s_maturity_date.value as maturity_date
, s_prolongation_date.value as prolongation_date
, c_contract.entity_value_id as contract_id
, s_contract_date.value as contract_no
, s_contract_no.value as contract_date
, c_credit_object.entity_value_id as credit_object_id
, s_credit_object_code.value as credit_object_code
, c_creditor_branch.entity_value_id as creditor_branch_id
, s_creditor_branch_code.value as creditor_branch_code
, c_credit_purpose.entity_value_id as credit_purpose_id
, s_credit_purpose_code.value as credit_purpose_code
, c_credit_type.entity_value_id as credit_type_id
, s_credit_type_code.value as credit_type_code
, c_currency.entity_value_id as currency_id
, s_currency_code.value as currency_code
, c_finance_source.entity_value_id as finance_source_id
, s_finance_source_code.value as finance_source_code
, c_change.entity_value_id as change_id
, c_portfolio.entity_value_id as portfolio1_id
, c_inner_portfolio.entity_value_id as portfolio2_id
, s_inner_portfolio_code.value as portfolio_code
, c_portfolio_msfo.entity_value_id as portfolio_msfo_id
, s_portfolio_msfo_code.value as portfolio_msfo_code

from
(
  select
    dat.id
    , dat.report_date
    , row_number() over(partition by dat.id order by report_date desc) as pp
  from (
    select en.id, re.report_date
    from eav_be_entities en
    join eav_be_entity_report_dates re on re.entity_id = en.id
    where en.class_id = 59 and re.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
  ) dat
) enr
-- contract
left join (
  select c_contract.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 58 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_contract where c_contract.p = 1
  ) c_contract on c_contract.entity_id = enr.id
-- contract_date
left join (
  select s_contract_date.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 119 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_contract_date where s_contract_date.p = 1
  ) s_contract_date on s_contract_date.entity_id = c_contract.entity_value_id
-- contract_no
left join (
  select s_contract_no.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_date_values cv where cv.attribute_id = 118 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_contract_no where s_contract_no.p = 1
  ) s_contract_no on s_contract_no.entity_id = c_contract.entity_value_id
-- credit_object
left join (
  select c_credit_object.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 59 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_credit_object where c_credit_object.p = 1
  ) c_credit_object on c_credit_object.entity_id = enr.id
-- credit_object_code
left join (
  select s_credit_object_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 123 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_credit_object_code where s_credit_object_code.p = 1
  ) s_credit_object_code on s_credit_object_code.entity_id = c_credit_object.entity_value_id
-- creditor_branch
left join (
  select c_creditor_branch.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 60 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_creditor_branch where c_creditor_branch.p = 1
  ) c_creditor_branch on c_creditor_branch.entity_id = enr.id
-- creditor_branch_code
left join (
  select s_creditor_branch_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 120 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_creditor_branch_code where s_creditor_branch_code.p = 1
  ) s_creditor_branch_code on s_creditor_branch_code.entity_id = c_creditor_branch.entity_value_id
-- credit_purpose
left join (
  select c_credit_purpose.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 61 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_credit_purpose where c_credit_purpose.p = 1
  ) c_credit_purpose on c_credit_purpose.entity_id = enr.id
-- credit_purpose_code
left join (
  select s_credit_purpose_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 126 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_credit_purpose_code where s_credit_purpose_code.p = 1
  ) s_credit_purpose_code on s_credit_purpose_code.entity_id = c_credit_purpose.entity_value_id
-- credit_type
left join (
  select c_credit_type.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 62 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_credit_type where c_credit_type.p = 1
  ) c_credit_type on c_credit_type.entity_id = enr.id
-- credit_type_code
left join (
  select s_credit_type_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 143 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_credit_type_code where s_credit_type_code.p = 1
  ) s_credit_type_code on s_credit_type_code.entity_id = c_credit_type.entity_value_id
-- currency
left join (
  select c_currency.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 63 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_currency where c_currency.p = 1
  ) c_currency on c_currency.entity_id = enr.id
-- currency_code
left join (
  select s_currency_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 129 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_currency_code where s_currency_code.p = 1
  ) s_currency_code on s_currency_code.entity_id = c_currency.entity_value_id
-- finance_source
left join (
  select c_finance_source.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 64 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_finance_source where c_finance_source.p = 1
  ) c_finance_source on c_finance_source.entity_id = enr.id
-- finance_source_code
left join (
  select s_finance_source_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 135 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_finance_source_code where s_finance_source_code.p = 1
  ) s_finance_source_code on s_finance_source_code.entity_id = c_finance_source.entity_value_id
-- portfolio
left join (
  select c_portfolio.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 65 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_portfolio where c_portfolio.p = 1
  ) c_portfolio on c_portfolio.entity_id = enr.id
-- inner_portfolio
left join (
  select c_inner_portfolio.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 46 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_inner_portfolio where c_inner_portfolio.p = 1
  ) c_inner_portfolio on c_inner_portfolio.entity_id = c_portfolio.entity_value_id
-- inner_portfolio_code
left join (
  select s_inner_portfolio_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 111 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_inner_portfolio_code where s_inner_portfolio_code.p = 1
  ) s_inner_portfolio_code on s_inner_portfolio_code.entity_id = c_inner_portfolio.entity_value_id
-- portfolio_msfo
left join (
  select c_portfolio_msfo.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 47 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_portfolio_msfo where c_portfolio_msfo.p = 1
  ) c_portfolio_msfo on c_portfolio_msfo.entity_id = c_portfolio.entity_value_id
-- portfolio_msfo_code
left join (
  select s_portfolio_msfo_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 111 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_portfolio_msfo_code where s_portfolio_msfo_code.p = 1
  ) s_portfolio_msfo_code on s_portfolio_msfo_code.entity_id = c_portfolio_msfo.entity_value_id
-- change
-- FINAL
left join (
  select c_change.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 66 and cv.report_date = to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_change where c_change.p = 1
  ) c_change on c_change.entity_id = enr.id
-- creditor
left join (
  select c_creditor.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 67 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_creditor where c_creditor.p = 1
  ) c_creditor on c_creditor.entity_id = enr.id
-- creditor_code
left join (
  select s_creditor_code.* from (
    select  cv.entity_id, cv.value, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_string_values cv where cv.attribute_id = 40 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_creditor_code where s_creditor_code.p = 1
  ) s_creditor_code on s_creditor_code.entity_id = c_creditor.entity_value_id
-- primary_contract
left join (
  select c_primary_contract.* from (
    select  cv.entity_id, cv.entity_value_id, row_number() over (partition by cv.entity_id order by cv.report_date desc) p from eav_be_complex_values cv where cv.attribute_id = 68 and cv.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) c_primary_contract where c_primary_contract.p = 1
  ) c_primary_contract on c_primary_contract.entity_id = enr.id
-- primary_contract_no
left join (
  select s_primary_contract_no.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_string_values attr where attr.attribute_id = 150 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_primary_contract_no where s_primary_contract_no.p = 1
  ) s_primary_contract_no on s_primary_contract_no.entity_id = c_primary_contract.entity_value_id
-- primary_contract_date
left join (
  select s_primary_contract_date.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_date_values attr where attr.attribute_id = 149 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_primary_contract_date where s_primary_contract_date.p = 1
  ) s_primary_contract_date on s_primary_contract_date.entity_id = c_primary_contract.entity_value_id
-- actual_issue_date
left join (
  select s_actual_issue_date.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_date_values attr where attr.attribute_id = 151 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_actual_issue_date where s_actual_issue_date.p = 1
  ) s_actual_issue_date on s_actual_issue_date.entity_id = enr.id
-- amount
left join (
  select s_amount.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_double_values attr where attr.attribute_id = 152 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_amount where s_amount.p = 1
  ) s_amount on s_amount.entity_id = enr.id
-- contract_maturity_date
left join (
  select s_contract_maturity_date.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_date_values attr where attr.attribute_id = 153 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_contract_maturity_date where s_contract_maturity_date.p = 1
  ) s_contract_maturity_date on s_contract_maturity_date.entity_id = enr.id
-- has_currency_earn
left join (
  select s_has_currency_earn.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_boolean_values attr where attr.attribute_id = 154 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_has_currency_earn where s_has_currency_earn.p = 1
  ) s_has_currency_earn on s_has_currency_earn.entity_id = enr.id
  -- interest_rate_yearly
left join (
  select s_interest_rate_yearly.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_double_values attr where attr.attribute_id = 155 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_interest_rate_yearly where s_interest_rate_yearly.p = 1
  ) s_interest_rate_yearly on s_interest_rate_yearly.entity_id = enr.id
-- maturity_date
left join (
  select s_maturity_date.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_date_values attr where attr.attribute_id = 156 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_maturity_date where s_maturity_date.p = 1
  ) s_maturity_date on s_maturity_date.entity_id = enr.id
-- prolongation_date
left join (
  select s_prolongation_date.* from (
    select  attr.entity_id, attr.value, row_number() over (partition by attr.entity_id order by attr.report_date desc) p from eav_be_date_values attr where attr.attribute_id = 157 and attr.report_date <= to_date('01.01.2015' , 'dd.mm.yyyy')
    ) s_prolongation_date where s_prolongation_date.p = 1
  ) s_prolongation_date on s_prolongation_date.entity_id = enr.id
where enr.pp = 1 and enr.report_date = to_date('01.01.2015' , 'dd.mm.yyyy');