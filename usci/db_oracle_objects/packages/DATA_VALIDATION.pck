create or replace package DATA_VALIDATION is

  -- Author  : Bauyrzhan
  -- Created : 27.04.2015 11:05:34
  -- Purpose : Пакет межформенного контроля сверяет данные АИП "Кредитный регистр"
  --           с данными АИП "Статистика" и АИП "Небанковские организации"
  --           также производится ряд логических проверок в кредитном регистре, которые
  --           не могут быть произведены на уровне одного договора

  ADM_ERROR constant number := 5; --Минимальное расхождение, которое считается ошибкой при проведении контроля
  CROSS_CHECK_PROTOCOL_TYPE_ID number;
  CROSS_CHECK_MESSAGE_TYPE_ID  number;
  global_cross_check_id        number;
  user_name                    varchar2(500);
  user_email_address           varchar2(500);
  language                     varchar2(10) := 'RU';
  errors_count                 number;
  g_report_date                date;
  g_creditor_id                number;
  prev_report_date             date;

  procedure cross_check(p_creditor in number,
                        p_date     in date,
                        p_user_id  in number,
                        Err_Code   OUT INTEGER,
                        Err_Msg    OUT VARCHAR2);

  procedure cross_check_all(p_date    in date,
                            p_user_id in number,
                            Err_Code  OUT INTEGER,
                            Err_Msg   OUT VARCHAR2);

end DATA_VALIDATION;

create or replace package body DATA_VALIDATION is

  function get_stat_creditor_bik(g_creditor_id in number) return varchar2 is
    v_bik varchar2(50 char);
  begin
    select d.no
      into v_bik
      from r_ref_creditor c, v_creditor_doc_his d
     where c.ref_creditor_id = d.creditor_id
       and d.type_id = '15'
       and c.ref_creditor_id = g_creditor_id
       and d.open_date <= g_report_date
       and (d.close_date is null or d.close_date > g_report_date)
       and rownum = 1;

    return v_bik;

  exception
    when no_data_found then
      return null;
  end;

  function stat_f_pr_period(period_date in date) return integer is
  begin
    return(stat.f_prperiod@stdbafn(0, 4, period_date, ' '));
  end stat_f_pr_period;

  function get_message_id_by_code(p_message_code in varchar2) return number is
    v_message_id number;
  begin
    select m.id
      into v_message_id
      from message m
     where m.code = p_message_code;
    return v_message_id;
  end get_message_id_by_code;

  procedure write_message(p_message_id  in number,
                          p_description in varchar2,
                          p_inner_value in varchar2,
                          p_outer_value in varchar2,
                          p_diff        in varchar2,
                          p_is_error    in number) is
  begin
    insert into cross_check_message
      (id,
       inner_value,
       outer_value,
       diff,
       message_id,
       description,
       cross_check_id,
       is_error)
    values
      (cross_check_message_seq.nextval,
       p_inner_value,
       p_outer_value,
       p_diff,
       p_message_id,
       p_description,
       global_cross_check_id,
       p_is_error);
  end;
  procedure write_message_with_error_calc(p_message_id  in number,
                                          p_description in varchar2,
                                          p_inner_value in varchar2,
                                          p_outer_value in varchar2) is
    is_big_error number;
  begin
    if abs(p_inner_value - p_outer_value) > ADM_ERROR then
      is_big_error := 1;
      errors_count := errors_count + 1;
    else
      is_big_error := 0;
    end if;
    write_message(p_message_id,
                  p_description,
                  p_inner_value,
                  p_outer_value,
                  p_inner_value - p_outer_value,
                  is_big_error);
  end;
  procedure protocol_write(p_description in varchar2,
                           p_inner_value in varchar2,
                           p_outer_value in varchar2,
                           p_diff        in varchar2,
                           p_is_error    in number) is
  begin
    write_message(null,
                  p_description,
                  p_inner_value,
                  p_outer_value,
                  p_diff,
                  p_is_error);
  end;
  procedure protocol_write(p_description in varchar2,
                           p_inner_value in number,
                           p_outer_value in number) is
  begin
    write_message_with_error_calc(null,
                                  p_description,
                                  p_inner_value,
                                  p_outer_value);
  end;
  procedure protocol_write(p_description in varchar2) is
  begin
    write_message(null, p_description, NULL, NULL, NULL, 1);
  end;
  procedure protocol_write_message(p_message_id  in number,
                                   p_inner_value in number,
                                   p_outer_value in number) is
  begin
    write_message_with_error_calc(p_message_id,
                                  null,
                                  p_inner_value,
                                  p_outer_value);
  end;
  procedure protocol_write_message_by_code(p_message_code in varchar2) is
  begin
    write_message(get_message_id_by_code(p_message_code),
                  null,
                  null,
                  null,
                  null,
                  1);

  end;
  procedure protocol_write_message_by_code(p_message_code in varchar2,
                                           p_inner_value  in number,
                                           p_outer_value  in number) is
  begin
    write_message_with_error_calc(get_message_id_by_code(p_message_code),
                                  null,
                                  p_inner_value,
                                  p_outer_value);
  end;
  procedure protocol_write_message_by_code(p_message_code in varchar2,
                                           p_inner_value  in number,
                                           p_outer_value  in number,
                                           p_diff         in number,
                                           p_is_error     in number) is
  begin
    write_message(get_message_id_by_code(p_message_code),
                  null,
                  p_inner_value,
                  p_outer_value,
                  p_diff,
                  p_is_error);
  end;

  procedure check_33(stat_val in number, uo_bal in varchar2) is
    cred_value  number;
    msfo_value  number;
    cred_val    number;
    msfo_bal_no varchar2(10);
  begin

    if g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') then
      return;
    end if;
    msfo_bal_no := null;

    select case substr(uo_bal, 1, 4)
             when '3303' then
              '1319'
             when '3304' then
              '1329'
             when '3305' then
              '1428'
             when '3307' then
              '1463'
             when '3315' then
              '8917'
             when '3316' then
              '2875'
             else
              ''
           end
      into msfo_bal_no
      from dual;

    if msfo_bal_no is null then
      return;
    end if;

    select sum(nvl(tp.provision_value, 0))
      into cred_value
      from temp_provisions tp
     where tp.type = 1
       and tp.account_no like uo_bal || '%';
    cred_value := nvl(cred_value, 0);

    select sum(nvl(tp.provision_value, 0))
      into msfo_value
      from temp_provisions tp
     where tp.type = 2
       and tp.account_no like msfo_bal_no || '%';

    msfo_value := nvl(msfo_value, 0);
    cred_val   := round((cred_value - msfo_value) / 1000);
    if (cred_val <> 0 or stat_val <> 0) then
      protocol_write_message_by_code('CC_' || substr(uo_bal, 1, 4),
                                     cred_val,
                                     stat_val);
    end if;
  end;

  procedure provisions_msfo_credits_check(outer_value in number) is
    cred_val        number;
    abs_outer_value number;
  begin
    abs_outer_value := abs(nvl(outer_value, 0));
    select sum(nvl(tp.provision_value, 0))
      into cred_val
      from temp_provisions tp
     where substr(tp.account_no, 1, 4) in ('1319', '1329', '1428', '1463')
       and tp.type = 2;
    cred_val := abs(round(nvl(cred_val, 0) / 1000));

    protocol_write_message_by_code('CC_CR_MSFO', cred_val, abs_outer_value);
  end;

  procedure provisions_msfo_liab_check(outer_value in number) is
    provisions_value number;
  begin
    select sum(nvl(tp.provision_value, 0))
      into provisions_value
      from temp_provisions tp
     where substr(tp.account_no, 1, 4) in ('8917', '2875')
       and tp.type = 2;
    provisions_value := round((nvl(provisions_value, 0)) / 1000);
    protocol_write_message_by_code('CC_CL_MSFO',
                                   provisions_value,
                                   outer_value);
  end;

  procedure provisions_uo_credits_check(outer_value in number) is
    provisions_positive_sum number;
    provisions_negative_sum number;
    cred_val                number;
  begin
    if g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') then
      return;
    end if;

    select sum(nvl(tp.provision_value, 0))
      into provisions_positive_sum
      from temp_provisions tp
     where substr(tp.account_no, 1, 4) in ('1319', '1329', '1428', '1463')
       and tp.type = 1;
    provisions_positive_sum := nvl(provisions_positive_sum, 0);

    select sum(nvl(tp.provision_value, 0))
      into provisions_negative_sum
      from temp_provisions tp
     where substr(tp.account_no, 1, 4) in ('3303', '3304', '3305', '3307')
       and tp.type = 1;
    provisions_negative_sum := abs(nvl(provisions_negative_sum, 0));

    cred_val := abs(round((provisions_positive_sum -
                          provisions_negative_sum) / 1000));

    protocol_write_message_by_code('CC_CR_AFN', cred_val, outer_value);
  end;

  procedure provisions_uo_liab_check(outer_value in number) is
    cred_provisions_sum number;
  begin
    if g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') then
      return;
    end if;
    select sum(nvl(tp.provision_value, 0))
      into cred_provisions_sum
      from temp_provisions tp
     where substr(tp.account_no, 1, 4) in ('8917', '2875', '3315', '3316')
       and tp.type = 1;
    cred_provisions_sum := round(abs(nvl(cred_provisions_sum, 0)) / 1000);
    protocol_write_message_by_code('CC_CL_AFN',
                                   cred_provisions_sum,
                                   outer_value);
  end;
  /*
  * 29.10.2014 - добавлена проверка на суммы остатков, по которым не указан номер счета
  */
  procedure check_sums_without_account_no is
    v_sum number;
  begin
    v_sum := 0;
    select round(sum(nvl(dr.value, 0)) / 1000)
      into v_sum
      from v_credit_his vch, v_core_remains /*core.debt_remains*/ dr --, ref.shared drt
     where vch.credit_id = dr.credit_id
          --and dr.type_id = drt.id

       and vch.creditor_id = g_creditor_id
          -- and vch.approved = 1
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date

       and dr.rep_date = g_report_date
       and dr.account_id is null

       and dr.type_code not in ('6', '9', '11', '12', '13');
    v_sum := nvl(v_sum, 0);
    if v_sum <> 0 then
      errors_count := errors_count + 1;
      protocol_write_message_by_code('REM_NOBA', v_sum, 0);
    end if;

    v_sum := 0;
    select round(sum(nvl(dr.value, 0)) / 1000)
      into v_sum
      from v_credit_his /*core.v_credit_his*/   vch,
           v_core_remains /*core.debt_remains*/ dr --, ref.shared drt
     where vch.credit_id = dr.credit_id
          --and dr.type_id = drt.id

       and vch.creditor_id = g_creditor_id
          -- and vch.approved = 1
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date

       and dr.rep_date = g_report_date
       and dr.account_id is null

       and dr.type_code in ('12', '13');
    v_sum := nvl(v_sum, 0);
    if v_sum <> 0 then
      errors_count := errors_count + 1;
      protocol_write_message_by_code('MSFO_NOBA', v_sum, 0);
    end if;

    if g_report_date < to_date('01.08.2013', 'dd.mm.yyyy') then
      v_sum := 0;
      select round(sum(nvl(dr.value, 0)) / 1000)
        into v_sum
        from v_credit_his /*core.v_credit_his*/   vch,
             v_core_remains /*core.debt_remains*/ dr --, ref.shared drt
       where vch.credit_id = dr.credit_id
            --  and dr.type_id = drt.id

         and vch.creditor_id = g_creditor_id
            --  and vch.approved = 1
         and (vch.maturity_date >= prev_report_date or
             vch.maturity_date is null)

         and (vch.close_date > g_report_date or vch.close_date is null)
         and vch.open_date <= g_report_date
         and vch.primary_contract_date < g_report_date

         and dr.rep_date = g_report_date
         and dr.account_id is null

         and dr.type_code in ('11');
      v_sum := nvl(v_sum, 0);
      if v_sum <> 0 then
        errors_count := errors_count + 1;
        protocol_write_message_by_code('UO_NOBA', v_sum, 0);
      end if;
    end if;
  end;
  /*
  * 05.11.2014 - добавлена проверка на неутвержденные договора
  */
  procedure check_not_approved_contracts is
    v_count number;
  begin
    select count(1)
      into v_count
      from v_credit_his vch
     where vch.open_date <= g_report_date
       and (vch.close_date is null or vch.close_date > g_report_date)
       and vch.creditor_id = g_creditor_id
       and vch.approved = 0;
    if v_count <> 0 then
      errors_count := errors_count + 1;
      protocol_write_message_by_code('NOT_APPR', v_count, 0, 0, 1);
    end if;
  end;

  /*
  * 04.03.2015 - добавляются проверки на дублирующиеся залоги
  *            пока проверки работают в экспериментальном режиме
  *            и должны рассчитываться только для сотрудников НБ РК и БСБ
  */
  procedure check_duplicate_pledges is
    v_count number;
  begin
    select count(1)
      into v_count
      from (select vph.contract_no, vph.pledge_type_id
              from v_credit_his vch, v_pledge_his vph
             where vch.creditor_id = g_creditor_id
               and vch.open_date <= g_report_date
               and (vch.close_date is null or vch.close_date > g_report_date)
               and (vch.maturity_date is null or
                   vch.maturity_date > prev_report_date)
               and vph.open_date <= g_report_date
               and (vph.close_date is null or vph.close_date > g_report_date)
               and vch.credit_id = vph.credit_id
             group by vph.contract_no, vph.pledge_type_id
            having count(distinct vph.value_) > 1);
    protocol_write_message_by_code('DUP_PLEDGES', v_count, 0, 0, 3);
  end;

  procedure check_different_pledge_lists is
    v_count number;
  begin
    select count(1)
      into v_count
      from (select x.contract_no
              from (select vph.contract_no,
                           vch.credit_id as id,
                           listagg(vph.pledge_type_id, ',') within group(order by vph.pledge_type_id) types
                      from v_credit_his vch, v_pledge_his vph
                     where vch.creditor_id = g_creditor_id
                       and vch.open_date <= g_report_date
                       and (vch.close_date is null or
                           vch.close_date > g_report_date)
                       and (vch.maturity_date is null or
                           vch.maturity_date > prev_report_date)
                       and vph.open_date <= g_report_date
                       and (vph.close_date is null or
                           vph.close_date > g_report_date)
                       and vch.credit_id = vph.credit_id
                     group by vph.contract_no, vch.credit_id) x
             group by x.contract_no
            having count(distinct x.types) > 1);
    protocol_write_message_by_code('PLEDGE_LIST', v_count, 0, 0, 3);
  end;

  procedure check_pledges is
  begin
    if user_email_address like '%@bsbnb.kz' or
       user_email_address like '%@nationalbank.kz' then
      check_duplicate_pledges();
      check_different_pledge_lists();
    end if;
  end;

  procedure check_fs_zpd_msfo is
    v_cr_sum number;
    v_st_sum number;
  begin
    if g_creditor_id = 10000 then
      g_creditor_id := 22;
    end if;
    delete from R_TEMP_FS_ZPD;
    insert into R_TEMP_FS_ZPD
      (drt_code, subj_type, is_se, value)
      select dr.type_code,
             (case
               when vdh.person_id is not null then
                1
               else
                2
             end),
             nvl(voh.is_se, 0),
             sum(nvl(dr.value, 0))
        from v_credit_his          vch,
             v_core_remains        dr,
             r_ref_balance_account ba,
             --ref.shared              drt,
             v_debtor_his       vdh,
             v_organization_his voh,
             r_ref_credit_type  ct
       where vch.creditor_id = g_creditor_id
            --  and vch.approved = 1

         and (vch.maturity_date >= prev_report_date or
             vch.maturity_date is null)

         and (vch.close_date > g_report_date or vch.close_date is null)
         and vch.open_date <= g_report_date
         and vch.primary_contract_date < g_report_date
         and vch.credit_id = dr.credit_id
         and dr.rep_date = g_report_date
         and dr.account_id = ba.ref_balance_account_id
         and ((ba.no_ like '1%') or (ba.no_ in ('8911', '8912')))
         and ba.no_ not like '1880%'
            --and dr.type_id = drt.id
         and vch.credit_id = vdh.credit_id
         and (vdh.close_date > g_report_date or vdh.close_date is null)
         and vdh.open_date <= g_report_date
         and (vdh.org_id is not null or vdh.person_id is not null or
             vdh.creditor_id is not null)
         and vdh.org_id = voh.org_id(+)
         and (voh.close_date > g_report_date or voh.close_date is null)
         and (voh.open_date is null or voh.open_date <= g_report_date)
         and vch.credit_type_id = ct.ref_credit_type_id
         and ct.code not in ('17', '18')
       group by dr.type_id,
                dr.type_code,
                dr.name_ru,
                (case
                  when vdh.person_id is not null then
                   1
                  else
                   2
                end),
                voh.is_se;
    for cont in (select *
                   from R_FS_ZPD_CONTROL /*maintenance.fs_zpd_control*/ fzc
                  order by fzc.id) loop
      select sum(tfz.value)
        into v_cr_sum
        from r_temp_fs_zpd tfz, R_FS_ZPD_DRT fzd
       where tfz.subj_type = cont.subj_type
         and tfz.is_se = cont.is_se
         and fzd.control_id = cont.id
         and tfz.drt_code = fzd.code;
      v_cr_sum := round(nvl(v_cr_sum, 0) / 1000);

      select sum(st.znac)
        into v_st_sum
        from R_STAT_TEMP st, r_fs_zpd_st_code fs
       where fs.control_id = cont.id
         and fs.pokaz_code = st.code;
      v_st_sum := nvl(v_st_sum, 0);

      protocol_write_message(cont.message_id, v_cr_sum, v_st_sum);
    end loop;
  end;

  procedure stat_check(is_brk in integer) is
    v_mfo  varchar2(50);
    v_date integer;

    stat_val                      number;
    cred_val                      number;
    stat_provision_credits_value  number;
    stat_provision_liab_value     number;
    stat_msfo_credits_value       number;
    stat_msfo_liab_value          number;
    n                             integer;
    is_balance_account_controlled integer;
    stat_sum_bal_act              number;
    cred_sum_bal_act              number;
    next_bal_act_no               varchar2(50);
    cur_no                        varchar2(50);
    provision_portfolio_value     number;
    v_val_7130                    number;
    v_val_8923                    number;
    v_3316                        number;
    v_temp_value                  number;
    v_is_error                    number;

  begin
    check_sums_without_account_no();
    n                            := 0;
    stat_provision_credits_value := 0;
    stat_provision_liab_value    := 0;
    stat_msfo_liab_value         := 0;
    stat_msfo_credits_value      := 0;
    v_mfo                        := get_stat_creditor_bik(g_creditor_id);

    if (v_mfo is null) then
      -- Creditor not found
      protocol_write_message_by_code('NOBIK', 0, 0, 0, 1);
      return;
    end if;
    v_date := stat_f_pr_period(g_report_date);

    delete from r_stat_temp;
    if is_brk <> 0 then
      begin
        insert into r_stat_temp
          (id, znac, code)
          select rownum, od.znac, od.code_pokaz
            from v_statistic_detail od
           where od.mfo = v_mfo
             and od.pr_period = v_date
             and (od.dat_end >= g_report_date or od.dat_end is null);
      end;
    else
      begin
        insert into r_stat_temp
          (id, znac, code)
          select rownum, od.znac, od.code_pokaz
            from v_statistic_operdata od
           where od.mfo = v_mfo
             and od.pr_period = v_date
             and (od.dat_end >= g_report_date or od.dat_end is null);
      end;
    end if;

    if is_brk = 0 and g_report_date >= to_date('01.11.2014', 'dd.mm.yyyy') then
      check_fs_zpd_msfo();
    end if;

    stat_sum_bal_act := 0;
    cred_sum_bal_act := 0;
    next_bal_act_no  := '';
    v_3316           := 0;
    for ba in (select vba.no no,
                      nvl(st.znac, 0) st_sum,
                      nvl(cr.cr_sum, 0) cr_sum
                 from v_bal_act vba,
                      r_stat_temp st,
                      (select bal.id,
                              round(sum(nvl(d.value, 0)) / 1000) cr_sum

                         from v_core_remains d,
                              v_credit_his   vch,
                              v_bal_act      bal
                        where vch.creditor_id = g_creditor_id
                          and d.credit_id = vch.credit_id(+)
                             --     and vch.approved = 1
                          and d.rep_date = g_report_date
                          and bal.id = d.account_id(+)
                          and (vch.maturity_date >= prev_report_date or
                              vch.maturity_date is null)

                          and (vch.close_date > g_report_date or
                              vch.close_date is null)
                          and vch.open_date <= g_report_date
                          and vch.primary_contract_date < g_report_date
                        group by bal.id) cr
                where vba.no = st.code(+)
                  and vba.id = cr.id(+)
                  and (vba.open_date is null or
                      vba.open_date <= g_report_date)
                  and (vba.close_date is null or
                      vba.close_date >= g_report_date)

                order by vba.no) loop

      if (ba.no > '3316' and v_3316 = 0) then
        check_33(0, '3316');
        v_3316 := 1;
      end if;
      is_balance_account_controlled := 1;
      if g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') and
         (substr(ba.no, 1, 4) in (1310,
                                  1311,
                                  1312,
                                  1313,
                                  1324,
                                  1325,
                                  1330,
                                  1331,
                                  1430,
                                  1431,
                                  1432,
                                  1433,
                                  1434,
                                  1435)) then
        if (length(ba.no) = 4) then
          stat_sum_bal_act := stat_sum_bal_act + nvl(ba.st_sum, 0);
        end if;
        cred_sum_bal_act              := cred_sum_bal_act +
                                         nvl(ba.cr_sum, 0);
        is_balance_account_controlled := 0;
        cur_no                        := substr(ba.no, 1, 4);
        select no
          into next_bal_act_no
          from (select vba.no
                  from v_bal_act vba
                 where vba.no > ba.no
                 order by vba.no)
         where rownum = 1;
        if substr(next_bal_act_no, 1, 4) <> substr(ba.no, 1, 4) then
          is_balance_account_controlled := 1;
          stat_val                      := stat_sum_bal_act;
          cred_val                      := cred_sum_bal_act;
          stat_sum_bal_act              := 0;
          cred_sum_bal_act              := 0;
        end if;
      else
        stat_val := nvl(ba.st_sum, 0);
        cred_val := nvl(ba.cr_sum, 0);
        cur_no   := ba.no;
      end if;

      if ((nvl(stat_val, 0) <> 0 or nvl(cred_val, 0) <> 0)) then

        if substr(cur_no, 1, 4) in ('1319', '1329', '1428', '1463') then

          n                            := n + 1;
          stat_msfo_credits_value      := stat_msfo_credits_value +
                                          stat_val;
          stat_provision_credits_value := stat_provision_credits_value +
                                          stat_val;

          select sum(nvl(tp.provision_value, 0))
            into provision_portfolio_value
            from temp_provisions tp
           where tp.account_no = cur_no
             and tp.type = 2;

          cred_val := round(nvl(provision_portfolio_value, 0) / 1000);
          if g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') then
            select sum(nvl(tp.provision_value, 0))
              into provision_portfolio_value
              from temp_provisions tp
             where tp.account_no = cur_no
               and tp.type = 1;
            cred_val := cred_val +
                        round(nvl(provision_portfolio_value, 0) / 1000);
          end if;
          cred_val := abs(cred_val);
          stat_val := abs(stat_val);
          if g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') then
            v_is_error := 0;
          else
            if abs(cred_val - stat_val) <= ADM_ERROR then
              v_is_error := 0;
            else
              v_is_error := 1;
            end if;
          end if;
          if v_is_error <> 0 then
            errors_count := errors_count + 1;
          end if;
          protocol_write(cur_no,
                         cred_val,
                         stat_val,
                         cred_val - stat_val,
                         v_is_error);

        else

          if substr(cur_no, 1, 4) in ('3303', '3304', '3305', '3307') then

            n := n + 1;
            if length(cur_no) = 4 then
              stat_provision_credits_value := stat_provision_credits_value -
                                              stat_val;
              check_33(stat_val, ba.no);
            end if;
          else

            if ba.no = '7130' or ba.no = '6675' or ba.no = '8923' then
              n := n + 1;
              if ba.no = '7130' then
                select sum(nvl(dr.value, 0))
                  into v_val_7130
                  from v_credit_his vch, v_core_remains dr
                 where vch.creditor_id = g_creditor_id
                   and dr.credit_id = vch.credit_id(+)
                      -- and vch.approved = 1
                   and dr.rep_date = g_report_date
                   and (vch.maturity_date >= prev_report_date or
                       vch.maturity_date is null)

                   and (vch.close_date > g_report_date or
                       vch.close_date is null)
                   and vch.open_date <= g_report_date
                   and vch.primary_contract_date < g_report_date
                   and dr.type_id = 60
                   and dr.account_id is null;
                cred_val := cred_val + round(nvl(v_val_7130, 0) / 1000);
              end if;
              if ba.no = '8923' then
                select sum(nvl(dr.value, 0))
                  into v_val_8923
                  from v_credit_his vch, v_core_remains dr
                 where vch.creditor_id = g_creditor_id
                   and dr.credit_id = vch.credit_id(+)
                   and dr.rep_date = g_report_date
                      --   and vch.approved = 1
                   and (vch.maturity_date >= prev_report_date or
                       vch.maturity_date is null)

                   and (vch.close_date > g_report_date or
                       vch.close_date is null)
                   and vch.open_date <= g_report_date
                   and vch.primary_contract_date < g_report_date
                   and dr.type_id = 60
                   and dr.account_id is null;
                cred_val := cred_val + round(nvl(v_val_8923, 0) / 1000);
              end if;
              if cred_val <= stat_val + ADM_ERROR then
                protocol_write(cur_no,
                               cred_val,
                               stat_val,
                               cred_val - stat_val,
                               0);
              else

                protocol_write(cur_no,
                               cred_val,
                               stat_val,
                               cred_val - stat_val,
                               1);
                errors_count := errors_count + 1;
              end if;
            else
              if substr(cur_no, 1, 4) in ('8917', '2875', '3315', '3316') then
                n := n + 1;
                if length(cur_no) = 4 or
                   (substr(cur_no, 1, 4) = '2875' and length(ba.no) = 7) then
                  stat_provision_liab_value := stat_provision_liab_value +
                                               stat_val;

                  if substr(cur_no, 1, 4) in ('3315', '3316') then
                    if (substr(cur_no, 1, 4) = '3316') then
                      v_3316 := 1;
                    end if;
                    check_33(stat_val, cur_no);

                  end if;
                  if substr(cur_no, 1, 4) in ('8917', '2875') then
                    if g_report_date >= to_date('01.09.2013', 'dd.mm.yyyy') then
                      if cred_val <> 0 OR stat_val <> 0 then

                        n := n + 1;

                        if is_balance_account_controlled <> 0 then

                          select sum(nvl(tp.provision_value, 0))
                            into v_temp_value
                            from temp_provisions tp
                           where tp.account_no like cur_no || '%'
                             and tp.is_portfolio = 1;

                          cred_val := cred_val +
                                      round(nvl(v_temp_value, 0) / 1000);
                          protocol_write(cur_no, cred_val, stat_val);
                        end if;

                      end if;
                    end if;
                    stat_msfo_liab_value := stat_msfo_liab_value + stat_val;
                  end if;
                end if;
              else
                if cred_val <> 0 OR stat_val <> 0 then

                  n := n + 1;

                  if is_balance_account_controlled <> 0 then

                    protocol_write(cur_no, cred_val, stat_val);
                  end if;

                end if;
              end if;
            end if;
          end if;
        end if;
      end if;
    end loop;

    if (v_3316 = 0) then
      check_33(0, '3316');
    end if;

    stat_msfo_credits_value := abs(nvl(stat_msfo_credits_value, 0));
    provisions_msfo_credits_check(stat_msfo_credits_value);

    stat_msfo_liab_value := abs(nvl(stat_msfo_liab_value, 0));
    provisions_msfo_liab_check(stat_msfo_liab_value);

    stat_provision_credits_value := abs(nvl(stat_provision_credits_value, 0));
    provisions_uo_credits_check(stat_provision_credits_value);

    stat_provision_liab_value := abs(nvl(stat_provision_liab_value, 0));
    provisions_uo_liab_check(stat_provision_liab_value);

    if n = 0 then
      protocol_write_message_by_code('NO_REMAINS', null, null, null, 1);
      errors_count := errors_count + 1;
    end if;
  end;

  procedure ipoteque_check is
    nokbdb_val                   number;
    cred_val                     number;
    n                            number;
    nokbdb_provision_credits_sum number;
    nokbdb_provision_liab_value  number;
    nokbdb_msfo_credits_value    number;
    nokbdb_msfo_liab_value       number;
  begin
    check_sums_without_account_no();
    nokbdb_msfo_credits_value    := 0;
    nokbdb_msfo_liab_value       := 0;
    nokbdb_provision_credits_sum := 0;
    nokbdb_provision_liab_value  := 0;

    n := 0;

    insert into r_stat_temp
      (id, znac, code)
      select rownum, ni.sum, ni.no
        from v_nokbdb_ipoteque ni
       where ni.rep_date = g_report_date
         and ni.creditor_id = g_creditor_id;

    for ba in (select substr(vba.no, 1, 4) no,
                      nvl(st.znac, 0) st_sum,
                      nvl(cr.cr_sum, 0) cr_sum
                 from (select distinct substr(no, 1, 4) no from v_bal_act) vba,
                      r_stat_temp st,
                      (select substr(bal.no, 1, 4) no,
                              round(sum(nvl(d.value, 0)) / 1000) cr_sum

                         from v_core_remains d,
                              v_credit_his   vch,
                              v_bal_act      bal

                        where vch.creditor_id = g_creditor_id
                          and d.credit_id = vch.credit_id(+)
                             -- and vch.approved = 1
                          and d.rep_date = g_report_date
                          and bal.id = d.account_id(+)
                          and (vch.maturity_date >= prev_report_date or
                              vch.maturity_date is null)

                          and (vch.close_date > g_report_date or
                              vch.close_date is null)
                          and vch.open_date <= g_report_date
                          and vch.primary_contract_date < g_report_date
                        group by substr(bal.no, 1, 4)) cr
                where vba.no = st.code(+)
                  and vba.no = cr.no(+)
                  and (nvl(st.znac, 0) <> 0 or nvl(cr.cr_sum, 0) <> 0)
                order by vba.no) loop
      nokbdb_val := nvl(ba.st_sum, 0);

      cred_val := nvl(ba.cr_sum, 0);

      if substr(ba.no, 1, 4) in ('1319', '1329', '1428', '1463') then
        n                            := n + 1;
        nokbdb_msfo_credits_value    := nokbdb_msfo_credits_value +
                                        nokbdb_val;
        nokbdb_provision_credits_sum := nokbdb_provision_credits_sum +
                                        nokbdb_val;
      else

        if ba.no in ('3303', '3304', '3305', '3307') then
          n                            := n + 1;
          nokbdb_provision_credits_sum := nokbdb_provision_credits_sum -
                                          nokbdb_val;
          check_33(nokbdb_val, ba.no);
        else

          if ba.no = '7130' or ba.no = '6675' or ba.no = '8923' then
            n := n + 1;
            if cred_val <= nokbdb_val then
              protocol_write(ba.no,
                             cred_val,
                             nokbdb_val,
                             cred_val - nokbdb_val,
                             0);
            else

              protocol_write(ba.no,
                             cred_val,
                             nokbdb_val,
                             cred_val - nokbdb_val,
                             1);
              errors_count := errors_count + 1;
            end if;
          else
            if substr(ba.no, 1, 4) in ('8917', '2875', '3315', '3316') then
              n                           := n + 1;
              nokbdb_provision_liab_value := nokbdb_provision_liab_value +
                                             nokbdb_val;

              if substr(ba.no, 1, 4) in ('3315', '3316') then
                check_33(nokbdb_val, ba.no);
              end if;
              if substr(ba.no, 1, 4) in ('8917', '2875') then
                nokbdb_msfo_liab_value := nokbdb_msfo_liab_value +
                                          nokbdb_val;
              end if;
            else
              if cred_val <> 0 OR nokbdb_val <> 0 then
                n := n + 1;
                protocol_write(ba.no, cred_val, nokbdb_val);
              end if;
            end if;
          end if;
        end if;
      end if;
    end loop;

    nokbdb_msfo_credits_value := abs(nvl(nokbdb_msfo_credits_value, 0));
    provisions_msfo_credits_check(nokbdb_msfo_credits_value);

    nokbdb_msfo_liab_value := abs(nvl(nokbdb_msfo_liab_value, 0));
    provisions_msfo_liab_check(nokbdb_msfo_liab_value);

    nokbdb_provision_credits_sum := abs(nvl(nokbdb_provision_credits_sum, 0));
    provisions_uo_credits_check(nokbdb_provision_credits_sum);

    nokbdb_provision_liab_value := abs(nvl(nokbdb_provision_liab_value, 0));
    provisions_uo_liab_check(nokbdb_provision_liab_value);

    if n = 0 then
      -- no data found
      protocol_write_message_by_code('NO_REMAINS');
      errors_count := errors_count + 1;
    end if;
  end;

  procedure nokbdb_od_opd(p_creditor_code in varchar2) is
    nokb_value number;
    cred_val   number;
  begin
    select round(sum(nvl(dr.value, 0)) / 1000, 0)
      into cred_val
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared      drt,
           r_ref_credit_type ct --,
    --ref.shared      kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --  and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = drt.id
       and (dr.type_code = '1' or dr.type_code = '2')
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id in (14, 75) -- kind.code = 'CR'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;

    cred_val := nvl(cred_val, 0);

    select sum(nvl(vnr.main_dept, 0))
      into nokb_value
      from v_nokbdb_report vnr
     where vnr.creditor_code = p_creditor_code
       and vnr.report_date = g_report_date;

    nokb_value := nvl(nokb_value, 0);

    protocol_write_message_by_code('CC_CR_OD', cred_val, nokb_value);
  end;

  procedure nokbdb_ov_opv(p_creditor_code in varchar2) is
    nokb_value number;
    cred_val   number;
  begin
    select round(sum(nvl(dr.value, 0)) / 1000, 0)
      into cred_val
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared      s,
           r_ref_credit_type ct --,
    --ref.shared      kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --  and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = s.id
       and (dr.type_code = '4' or dr.type_code = '5')
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id in (14, 75) --kind.code = 'CR'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;

    cred_val := nvl(cred_val, 0);

    select sum(nvl(vnr.prem, 0))
      into nokb_value
      from v_nokbdb_report vnr
     where vnr.creditor_code = p_creditor_code
       and vnr.report_date = g_report_date;
    nokb_value := nvl(nokb_value, 0);

    protocol_write_message_by_code('CC_CR_OV', cred_val, nokb_value);
  end;

  procedure nokbdb_pnuo_pouo(p_creditor_code in varchar2) is
    nokb_value      number;
    cred_flow_value number;
    portfolio_value number;
    cred_val        number;
  begin
    select round(sum(nvl(dr.value, 0)) / 1000)
      into cred_flow_value
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared      drt,
           r_ref_credit_type ct --,
    --ref.shared      kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --  and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = drt.id
       and dr.type_code = '11'
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id in (14, 75) --kind.code = 'CR'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;
    cred_flow_value := nvl(cred_flow_value, 0);

    select round(sum(nvl(pf.value, 0)) / 1000)
      into portfolio_value
      from r_core_portfolio_flow_kfn pf
     where pf.creditor_id = g_creditor_id
       and pf.rep_date = g_report_date
       and exists
     (select vch.credit_id
              from v_credit_his vch
             where --vch.approved = 1
            --and
             (vch.portfolio1_id = pf.portfolio_id or
             vch.portfolio2_id = pf.portfolio_id)
          and (vch.maturity_date >= prev_report_date or
             vch.maturity_date is null)

          and (vch.close_date > g_report_date or vch.close_date is null)
          and vch.open_date <= g_report_date
          and vch.primary_contract_date < g_report_date);

    portfolio_value := nvl(portfolio_value, 0);
    cred_val        := cred_flow_value + portfolio_value;
    select sum(nvl(vnr.fact_vict, 0))
      into nokb_value
      from v_nokbdb_report vnr
     where vnr.creditor_code = p_creditor_code
       and vnr.report_date = g_report_date;
    nokb_value := nvl(nokb_value, 0);

    protocol_write_message_by_code('CC_CR_PNUO', cred_val, nokb_value);
  end;

  procedure nokbdb_uo_check(p_creditor_code in varchar2) is
    main_debt    number;
    prem         number;
    fact_vict    number;
    od_opd_value number;
    ov_opv_value number;
    pnuo_value   number;
  begin
    begin
      select vnlc.main_dept, vnlc.prem, vnlc.fact_vict
        into main_debt, prem, fact_vict
        from v_nokbdb_liab_class vnlc
       where vnlc.creditor_code = p_creditor_code
         and vnlc.report_date = g_report_date;
    exception
      when no_data_found then
        main_debt := 0;
        prem      := 0;
        fact_vict := 0;
    end;

    select round(sum(nvl(dr.value, 0)) / 1000, 0)
      into od_opd_value
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared      s,
           r_ref_credit_type ct --,
    --ref.shared      kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = s.id
       and (dr.type_code = '1' or dr.type_code = '2')
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id = 15 --kind.code = 'CL'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;

    od_opd_value := nvl(od_opd_value, 0);

    select round(sum(nvl(dr.value, 0)) / 1000, 0)
      into ov_opv_value
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared      s,
           r_ref_credit_type ct --,
    --ref.shared      kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --  and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = s.id
       and (dr.type_code = '4' or dr.type_code = '5')
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id = 15 --kind.code = 'CL'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;

    ov_opv_value := nvl(ov_opv_value, 0);

    select round(sum(nvl(dr.value, 0)) / 1000)
      into pnuo_value
      from v_credit_his   vch,
           v_core_remains dr,
           --ref.shared        drt,
           r_ref_credit_type ct --,
    --ref.shared        kind
     where vch.creditor_id = g_creditor_id
       and vch.credit_id = dr.credit_id
          --  and vch.approved = 1
       and dr.rep_date = g_report_date
          --and dr.type_id = drt.id
       and dr.type_code = '11'
       and vch.credit_type_id = ct.ref_credit_type_id
          --and ct.kind_id = kind.id
       and ct.kind_id = 15 --kind.code = 'CL'
       and (vch.maturity_date >= prev_report_date or
           vch.maturity_date is null)

       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date;
    pnuo_value := nvl(pnuo_value, 0);

    protocol_write_message_by_code('CC_CL_OD', od_opd_value, main_debt);
    protocol_write_message_by_code('CC_CL_OV', ov_opv_value, prem);
    protocol_write_message_by_code('CC_CL_PNUO', pnuo_value, fact_vict);
  end;

  procedure nokbdb_check is
    nokbdb_creditor_code varchar2(50);
  begin
    select crn.nokbdb_code
      into nokbdb_creditor_code
      from r_ref_creditor crn
     where crn.ref_creditor_id = g_creditor_id
       and crn.nokbdb_code is not null;
    nokbdb_od_opd(nokbdb_creditor_code);
    nokbdb_ov_opv(nokbdb_creditor_code);
    nokbdb_pnuo_pouo(nokbdb_creditor_code);
    nokbdb_uo_check(nokbdb_creditor_code);
  end;

  procedure build_temp_provisions is
  begin
    delete from temp_provisions;

    insert into temp_provisions
      (provision_value, account_no, type, is_portfolio)
      select sum(nvl(dr.value, 0)), ba.no_, 1, 0
        from v_credit_his   vch,
             v_core_remains dr,
             --ref.shared          drt,
             r_ref_balance_account ba
       where --vch.approved = 1
      --and
       (vch.maturity_date >= prev_report_date or vch.maturity_date is null)
       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date
       and vch.credit_id = dr.credit_id
      --and dr.type_id = drt.id
       and dr.account_id = ba.ref_balance_account_id
       and dr.rep_date = g_report_date
       and vch.creditor_id = g_creditor_id
       and dr.type_code = '11'
       group by ba.no_;

    insert into temp_provisions
      (provision_value, account_no, type, is_portfolio)
      select sum(nvl(dr.value, 0)), ba.no_, 2, 0
        from v_credit_his   vch,
             v_core_remains dr,
             --ref.shared          drt,
             r_ref_balance_account ba
       where --vch.approved = 1
      --and
       (vch.maturity_date >= prev_report_date or vch.maturity_date is null)
       and (vch.close_date > g_report_date or vch.close_date is null)
       and vch.open_date <= g_report_date
       and vch.primary_contract_date < g_report_date
       and vch.credit_id = dr.credit_id
      --and dr.type_id = drt.id
       and dr.account_id = ba.ref_balance_account_id
       and dr.rep_date = g_report_date
       and vch.creditor_id = g_creditor_id
       and dr.type_code in ('12', '13')
       group by ba.no_;

    insert into temp_provisions
      (provision_value, account_no, type, is_portfolio)
      select sum(nvl(pf.value, 0)), ba.no_, 1, 1
        from r_core_portfolio_flow_kfn pf, r_ref_balance_account ba
       where pf.rep_date = g_report_date
         and pf.creditor_id = g_creditor_id
         and pf.balance_account_id = ba.ref_balance_account_id
         and exists
       (select vch.credit_id
                from v_credit_his vch
               where --vch.approved = 1
              --and
               (vch.portfolio1_id = pf.portfolio_id or
               vch.portfolio2_id = pf.portfolio_id)
            and (vch.maturity_date >= prev_report_date or
               vch.maturity_date is null)

            and (vch.close_date > g_report_date or vch.close_date is null)
            and vch.open_date <= g_report_date
            and vch.primary_contract_date < g_report_date
            and vch.creditor_id = g_creditor_id)
       group by ba.no_;

    insert into temp_provisions
      (provision_value, account_no, type, is_portfolio)
      select sum(nvl(pf.value, 0)), ba.no_, 2, 1
        from r_core_portfolio_flow_msfo pf, r_ref_balance_account ba
       where pf.rep_date = g_report_date
         and pf.creditor_id = g_creditor_id
         and pf.balance_account_id = ba.ref_balance_account_id
         and exists
       (select vch.credit_id
                from v_credit_his vch
               where --vch.approved = 1
              --and
               vch.portfolio_msfo_id = pf.portfolio_id
            and (vch.maturity_date >= prev_report_date or
               vch.maturity_date is null)

            and (vch.close_date > g_report_date or vch.close_date is null)
            and vch.open_date <= g_report_date
            and vch.primary_contract_date < g_report_date
            and vch.creditor_id = g_creditor_id)
       group by ba.no_;
    if g_report_date < to_date('01.07.2013', 'dd.mm.yyyy') then
      for empty_port in (select p.code, sum(nvl(pf.value, 0)) as sm
                           from r_core_portfolio_flow_kfn pf,
                                r_ref_portfolio           p
                          where pf.rep_date = g_report_date
                            and pf.creditor_id = g_creditor_id
                            and pf.portfolio_id = p.ref_portfolio_id
                            and not exists
                          (select vch.credit_id
                                   from v_credit_his vch
                                  where --vch.approved = 1
                                 --and
                                  (vch.portfolio1_id = p.ref_portfolio_id or
                                  vch.portfolio2_id = p.ref_portfolio_id)
                               and (vch.maturity_date >= prev_report_date or
                                  vch.maturity_date is null)

                               and (vch.close_date > g_report_date or
                                  vch.close_date is null)
                               and vch.open_date <= g_report_date
                               and vch.primary_contract_date < g_report_date
                               and vch.creditor_id = g_creditor_id)
                          group by p.id, p.code) loop
        protocol_write_message_by_code('EMPPORTUO',
                                       'Нет договоров: [' ||
                                       empty_port.code || ']',
                                       '0',
                                       round(nvl(empty_port.sm, 0) / 1000),
                                       1);
      end loop;
    end if;
    for empty_port_msfo in (select p.code, sum(nvl(pf.value, 0)) as sm
                              from r_core_portfolio_flow_msfo pf,
                                   r_ref_portfolio            p
                             where pf.rep_date = g_report_date
                               and pf.creditor_id = g_creditor_id
                               and pf.portfolio_id = p.ref_portfolio_id
                               and not exists
                             (select vch.credit_id
                                      from v_credit_his vch
                                     where --vch.approved = 1
                                    --and
                                     vch.portfolio_msfo_id =
                                     p.ref_portfolio_id
                                  and (vch.maturity_date >= prev_report_date or
                                     vch.maturity_date is null)

                                  and (vch.close_date > g_report_date or
                                     vch.close_date is null)
                                  and vch.open_date <= g_report_date
                                  and vch.primary_contract_date <
                                     g_report_date
                                  and vch.creditor_id = g_creditor_id)
                             group by p.id, p.code
                            having sum(nvl(pf.value, 0)) <> 0) loop
      protocol_write_message_by_code('EMPPORTMSF',
                                     'Нет договоров: [' ||
                                     empty_port_msfo.code || ']',
                                     '0',
                                     round(nvl(empty_port_msfo.sm, 0) / 1000),
                                     1);
    end loop;

  end;

  procedure run_cross_check(p_creditor in number,
                            p_date     in date,
                            Err_Code   OUT INTEGER,
                            Err_Msg    OUT VARCHAR2) is
    ProcName CONSTANT VARCHAR2(30) := 'cross_check';
    report_type_failed_id   integer;
    report_type_errors_id   integer;
    report_type_success_id  integer;
    report_type_finished_id integer;
    with_error_status_id    integer; --input info status: ""Executed with errors""
    without_error_status_id integer; --input info status: ""Executed withour errors""
    subject_type_code       integer;
    creditor_code           varchar2(10);
    org_approved_status_id  integer;
    v_report_period_months  number;
    v_now                   DATE;
  begin

    select st.report_period_duration_months
      into v_report_period_months
      from r_ref_creditor c, r_ref_subject_type st
     where c.ref_creditor_id = p_creditor
       and c.subject_type_id = st.ref_subject_type_id;

    v_report_period_months := nvl(v_report_period_months, 1);

    g_report_date    := p_date;
    prev_report_date := add_months(p_date, -v_report_period_months);
    g_creditor_id    := p_creditor;

    report_type_failed_id   := 91;
    report_type_success_id  := 77;
    report_type_errors_id   := 76;
    report_type_finished_id := 92;
    org_approved_status_id  := 128;
    --Variable initialization
    without_error_status_id      := 44;
    with_error_status_id         := 43;
    CROSS_CHECK_PROTOCOL_TYPE_ID := 94;
    CROSS_CHECK_MESSAGE_TYPE_ID  := 93;

    select st.code
      into subject_type_code
      from r_ref_creditor c, r_ref_subject_type st
     where c.ref_creditor_id = p_creditor
       and c.subject_type_id = st.ref_subject_type_id;
    --Getting next input info id
    select cross_check_seq.nextval into global_cross_check_id from dual;

    select c.code
      into creditor_code
      from r_ref_creditor c
     where c.ref_creditor_id = p_creditor;

    insert into cross_check
      (id, user_name, date_begin, creditor_id, report_date, status_id)
    values
      (global_cross_check_id,
       user_name,
       sysdate,
       p_creditor,
       p_date,
       with_error_status_id);
    commit;
    errors_count := 0;
    check_pledges();
    check_not_approved_contracts();
    build_temp_provisions();
    if creditor_code = '907' then
      stat_check(1);
    else
      if subject_type_code = '0001' then
        stat_check(0);
      else
        if subject_type_code = '0002' then
          ipoteque_check();
        else
          nokbdb_check();
        end if;
      end if;
    end if;
    commit;
    SELECT SYSDATE INTO v_now FROM DUAL;
    LOOP
      EXIT WHEN v_now +(60 / 86400) = SYSDATE;
    END LOOP;
    if errors_count = 0 then
      update cross_check cc
         set cc.status_id = without_error_status_id
       where cc.id = global_cross_check_id;
    end if;
    update cross_check cc
       set cc.date_end = sysdate
     where cc.id = global_cross_check_id;

    commit;
    if errors_count = 0 then
      update R_CORE_REPORT r
         set r.status_id = report_type_success_id
       where r.creditor_id = g_creditor_id
         and r.report_date = g_report_date
         and r.status_id <> report_type_finished_id
         and r.status_id <> org_approved_status_id;
    else
      update R_CORE_REPORT r
         set r.status_id = report_type_errors_id
       where r.creditor_id = g_creditor_id
         and r.report_date = g_report_date
         and r.status_id <> report_type_finished_id
         and r.status_id <> org_approved_status_id;
      commit;
    end if;
    commit;

    commit;

  EXCEPTION

    WHEN OTHERS THEN
      ROLLBACK;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' - ' || '/E\' || SUBSTR(SQLERRM, 1, 255) ||
                  '/R\' || 'Ошибка в контроле';
      begin
        protocol_write('ERROR ' || Err_Code || ': ' || Err_Msg);
        commit;
      exception
        when others then
          return;
      end;
  end;

  procedure cross_check(p_creditor in number,
                        p_date     in date,
                        p_user_id  in number,
                        Err_Code   OUT INTEGER,
                        Err_Msg    OUT VARCHAR2) is

  begin
    select pu.first_name || ' ' || pu.last_name, pu.email
      into user_name, user_email_address
      from eav_a_user pu
     where pu.user_id = p_user_id;
    run_cross_check(p_creditor, p_date, Err_Code, Err_Msg);

  end;

  procedure cross_check_all(p_date    in date,
                            p_user_id in number,
                            Err_Code  OUT INTEGER,
                            Err_Msg   OUT VARCHAR2) is
    ProcName CONSTANT VARCHAR2(30) := 'cross_check_all';
  begin
    for c in (select rc.ref_creditor_id as id
                from r_ref_creditor rc, r_ref_creditor_branch cb
               where rc.code = cb.code
                 and cb.main_office_id is null
                 and rc.subject_type_id = 1) loop
      cross_check(c.id, p_date, p_user_id, Err_Code, Err_Msg);
    end loop;
  EXCEPTION

    WHEN OTHERS THEN
      ROLLBACK;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' - ' || '/E\' || SUBSTR(SQLERRM, 1, 255) ||
                  '/R\' || 'Procedure error';

  end;
end DATA_VALIDATION;

