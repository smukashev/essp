with ba_path as (select 'change.remains.debt.current.balance_account.no_' as path,
                        'remains_debt_current' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по основному долгу (непросроченная задолженность)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.debt.pastdue.balance_account.no_' as path,
                        'remains_debt_pastdue' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по основному долгу (просроченная задолженность)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.debt.write_off.balance_account.no_' as path,
                        'remains_debt_write_off' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по основному долгу (списанная с баланса задолженность)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.interest.current.balance_account.no_' as path,
                        'remains_interest_current' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по вознаграждению (непросроченная задолженность)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.interest.pastdue.balance_account.no_' as path,
                        'remains_interest_pastdue' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по вознаграждению (просроченная задолженность)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.correction.balance_account.no_' as path,
                        'remains_correction' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по положительной/отрицательной корректировке\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.remains.discount.balance_account.no_' as path,
                        'remains_discount' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по дисконту/премии\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.credit_flow.provision.provision_kfn.balance_account.no_' as path,
                        'credit_flow_provision_kfn' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета, на котором отражена разница по  резервам (провизиям), сформированным по требованиям МСФО и требованиям уполномоченного органа по неоднородным кредитам\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.credit_flow.provision.provision_msfo.balance_account.no_' as path,
                        'credit_flow_provision_msfo' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по провизиям (резервам), сформированным по требованиям международных стандартов финансовой отчетности по неоднородным кредитам\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual
                 union
                 select 'change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_' as path,
                        'credit_flow_provision_msfo_over_balance' as name,
                        'Значение %balance_account_no% показателя \"Номер балансового счета по провизиям (резервам), сформированного по требованиям МСФО  (по лимиту кредитной карты/овердрафта)\" не соответствует значению %s показателя \"Вид займа, условного и возможного обязательства\"' as validation_error
                   from dual)
  select *
    from (select 'rule read rule_end' || chr(13) ||
                 'rule "rule_ba_'  || ba.no_ || '_by_ct_for_'  || p.name || '" ' || chr(13) ||
                 '  when $entity : BaseEntity(' || chr(13) ||
                 '    getEl("' || p.path || '") != null ' || chr(38) || chr(38) || ' ' || chr(13) ||
                 '    getEl("credit.credit_type.code") != null ' || chr(38) || chr(38) || ' ' || chr(13) ||
                 '    getEl("' || p.path || '") == "' || ba.no_ || '" ' || chr(38) || chr(38) || ' ' || chr(13) ||
                 '    getEl("credit.credit_type.code") != "' || listagg(ct.code, '" ' || chr(38) || chr(38) || ' getEl("credit.credit_type.code") != "') within group (order by ct.code) || '") ' || chr(13) ||
                 '  then $entity.addValidationError(String.format("' || replace(p.validation_error, '%balance_account_no%', ba.no_) || '", $entity.getEl("credit.credit_type.code"))); ' ||  chr(13) ||
                 'end' || chr(13) ||
                 'rule_end' || chr(13) || chr(13) ||
                 'rule save' || chr(13) || chr(13) as rule
            from ref.ba_ct ba_ct,
                 ref.balance_account ba,
                 ref.credit_type ct,
                 ba_path p
           where ba_ct.open_date <= &p_report_date
             and (ba_ct.close_date > &p_report_date or ba_ct.close_date is null)
             and ba_ct.balance_account_id = ba.parent_id
             and ba.open_date <= &p_report_date
             and (ba.close_date > &p_report_date or ba.close_date is null)
             and ba_ct.credit_type_id = ct.parent_id
             and ct.open_date <= &p_report_date
             and (ct.close_date > &p_report_date or ct.close_date is null)
           group by p.validation_error, p.name, p.path, ba.no_
           order by p.path, ba.no_) r
