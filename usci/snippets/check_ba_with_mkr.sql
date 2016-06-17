select
  mkr.primary_contract_no, mkr.primary_contract_date, mkr.value,
  essp.primary_contract_no, essp.primary_contract_date, essp.value
from (
  select t2.primary_contract_no, t2.primary_contract_date, t1.value from debt_remains@credit t1
  join credit@credit t2 on t1.credit_id = t2.id
   where t2.creditor_id = 28 and t1.rep_date = rep_date and account_id = ba_mkr_id) mkr
   left outer join (select t2.primary_contract_no, t2.primary_contract_date, t1.value from reporter.v_core_remains t1
join showcase.r_core_credit t2 on t1.credit_id = t2.credit_id
where t1.creditor_id = 2388 and t1.rep_date = rep_date and t1.account_id = ba_essp_id) essp
on trim(mkr.primary_contract_no) = trim(essp.primary_contract_no) and trim(mkr.primary_contract_date) = trim(essp.primary_contract_date) and mkr.value = essp.value
where essp.primary_contract_no is null ;