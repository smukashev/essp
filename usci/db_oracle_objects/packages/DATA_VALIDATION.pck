CREATE OR REPLACE PACKAGE DATA_VALIDATION is
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
  FUNCTION get_pastdue_days (p_credit_id in number) return number;
end DATA_VALIDATION;

CREATE OR REPLACE PACKAGE BODY DATA_VALIDATION_TEST IS
  FUNCTION get_stat_creditor_bik(g_creditor_id IN NUMBER) RETURN VARCHAR2 IS
    v_bik VARCHAR2(50 CHAR);
  BEGIN
    SELECT d.no
      INTO v_bik
      FROM showcase.r_ref_creditor c, v_creditor_doc_his d
     WHERE c.ref_creditor_id = d.creditor_id
       AND d.type_id = '15'
       AND c.ref_creditor_id = g_creditor_id
       AND d.open_date <= g_report_date
       AND (d.close_date IS NULL OR d.close_date > g_report_date)
       AND rownum = 1;
    RETURN v_bik;
  EXCEPTION
    WHEN no_data_found THEN
      RETURN NULL;
  END;
  FUNCTION stat_f_pr_period(period_date IN DATE) RETURN INTEGER IS
  BEGIN
    RETURN(stat.f_prperiod@stdb(0, 4, period_date, ' '));
  END stat_f_pr_period;
  FUNCTION get_message_id_by_code(p_message_code IN VARCHAR2) RETURN NUMBER IS
    v_message_id NUMBER;
  BEGIN
    SELECT m.id
      INTO v_message_id
      FROM MESSAGE m
     WHERE m.code = p_message_code;
    RETURN v_message_id;
  END get_message_id_by_code;
  PROCEDURE write_message(p_message_id  IN NUMBER,
                          p_description IN VARCHAR2,
                          p_inner_value IN VARCHAR2,
                          p_outer_value IN VARCHAR2,
                          p_diff        IN VARCHAR2,
                          p_is_error    IN NUMBER) IS
  BEGIN
    INSERT INTO cross_check_message
      (id,
       inner_value,
       outer_value,
       diff,
       message_id,
       description,
       cross_check_id,
       is_error)
    VALUES
      (cross_check_message_seq.nextval,
       p_inner_value,
       p_outer_value,
       p_diff,
       p_message_id,
       p_description,
       global_cross_check_id,
       p_is_error);
  END;
  PROCEDURE write_message_with_error_calc(p_message_id  IN NUMBER,
                                          p_description IN VARCHAR2,
                                          p_inner_value IN VARCHAR2,
                                          p_outer_value IN VARCHAR2) IS
    is_big_error NUMBER;
  BEGIN
    IF ABS(p_inner_value - p_outer_value) > ADM_ERROR THEN
      is_big_error := 1;
      errors_count := errors_count + 1;
    ELSE
      is_big_error := 0;
    END IF;
    write_message(p_message_id,
                  p_description,
                  p_inner_value,
                  p_outer_value,
                  p_inner_value - p_outer_value,
                  is_big_error);
  END;
  PROCEDURE protocol_write(p_description IN VARCHAR2,
                           p_inner_value IN VARCHAR2,
                           p_outer_value IN VARCHAR2,
                           p_diff        IN VARCHAR2,
                           p_is_error    IN NUMBER) IS
  BEGIN
    write_message(NULL,
                  p_description,
                  p_inner_value,
                  p_outer_value,
                  p_diff,
                  p_is_error);
  END;
  PROCEDURE protocol_write(p_description IN VARCHAR2,
                           p_inner_value IN NUMBER,
                           p_outer_value IN NUMBER) IS
  BEGIN
    write_message_with_error_calc(NULL,
                                  p_description,
                                  p_inner_value,
                                  p_outer_value);
  END;
  PROCEDURE protocol_write(p_description IN VARCHAR2) IS
  BEGIN
    write_message(NULL, p_description, NULL, NULL, NULL, 1);
  END;
  PROCEDURE protocol_write_message(p_message_id  IN NUMBER,
                                   p_inner_value IN NUMBER,
                                   p_outer_value IN NUMBER) IS
  BEGIN
    write_message_with_error_calc(p_message_id,
                                  NULL,
                                  p_inner_value,
                                  p_outer_value);
  END;
  PROCEDURE protocol_write_message_by_code(p_message_code IN VARCHAR2) IS
  BEGIN
    write_message(get_message_id_by_code(p_message_code),
                  NULL,
                  NULL,
                  NULL,
                  NULL,
                  1);
  END;
  PROCEDURE protocol_write_message_by_code(p_message_code IN VARCHAR2,
                                           p_inner_value  IN NUMBER,
                                           p_outer_value  IN NUMBER) IS
  BEGIN
    write_message_with_error_calc(get_message_id_by_code(p_message_code),
                                  NULL,
                                  p_inner_value,
                                  p_outer_value);
  END;
  PROCEDURE protocol_write_message_by_code(p_message_code IN VARCHAR2,
                                           p_inner_value  IN NUMBER,
                                           p_outer_value  IN NUMBER,
                                           p_diff         IN NUMBER,
                                           p_is_error     IN NUMBER) IS
  BEGIN
    write_message(get_message_id_by_code(p_message_code),
                  NULL,
                  p_inner_value,
                  p_outer_value,
                  p_diff,
                  p_is_error);
  END;
  PROCEDURE check_33(stat_val IN NUMBER, uo_bal IN VARCHAR2) IS
    cred_value  NUMBER;
    msfo_value  NUMBER;
    cred_val    NUMBER;
    msfo_bal_no VARCHAR2(10);
  BEGIN
    IF g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') THEN
      RETURN;
    END IF;
    msfo_bal_no := NULL;
    SELECT CASE SUBSTR(uo_bal, 1, 4)
             WHEN '3303' THEN
              '1319'
             WHEN '3304' THEN
              '1329'
             WHEN '3305' THEN
              '1428'
             WHEN '3307' THEN
              '1463'
             WHEN '3315' THEN
              '8917'
             WHEN '3316' THEN
              '2875'
             ELSE
              ''
           END
      INTO msfo_bal_no
      FROM dual;
    IF msfo_bal_no IS NULL THEN
      RETURN;
    END IF;
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO cred_value
      FROM temp_provisions tp
     WHERE tp.type = 1
       AND tp.account_no LIKE uo_bal || '%';
    cred_value := NVL(cred_value, 0);
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO msfo_value
      FROM temp_provisions tp
     WHERE tp.type = 2
       AND tp.account_no LIKE msfo_bal_no || '%';
    msfo_value := NVL(msfo_value, 0);
    cred_val   := ROUND((cred_value - msfo_value) / 1000);
    IF (cred_val <> 0 OR stat_val <> 0) THEN
      protocol_write_message_by_code('CC_' || SUBSTR(uo_bal, 1, 4),
                                     cred_val,
                                     stat_val);
    END IF;
  END;
  PROCEDURE provisions_msfo_credits_check(outer_value IN NUMBER) IS
    cred_val        NUMBER;
    abs_outer_value NUMBER;
  BEGIN
    abs_outer_value := ABS(NVL(outer_value, 0));
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO cred_val
      FROM temp_provisions tp
     WHERE SUBSTR(tp.account_no, 1, 4) IN ('1319', '1329', '1428', '1463')
       AND tp.type = 2;
    cred_val := ABS(ROUND(NVL(cred_val, 0) / 1000));
    protocol_write_message_by_code('CC_CR_MSFO', cred_val, abs_outer_value);
  END;
  PROCEDURE provisions_msfo_liab_check(outer_value IN NUMBER) IS
    provisions_value NUMBER;
  BEGIN
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO provisions_value
      FROM temp_provisions tp
     WHERE SUBSTR(tp.account_no, 1, 4) IN ('8708', '8709', '8917', '2875')
       AND tp.type = 2;
    provisions_value := ROUND((NVL(provisions_value, 0)) / 1000);
    protocol_write_message_by_code('CC_CL_MSFO',
                                   provisions_value,
                                   outer_value);
  END;
  PROCEDURE provisions_uo_credits_check(outer_value IN NUMBER) IS
    provisions_positive_sum NUMBER;
    provisions_negative_sum NUMBER;
    cred_val                NUMBER;
  BEGIN
    IF g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') THEN
      RETURN;
    END IF;
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO provisions_positive_sum
      FROM temp_provisions tp
     WHERE SUBSTR(tp.account_no, 1, 4) IN ('1319', '1329', '1428', '1463')
       AND tp.type = 1;
    provisions_positive_sum := NVL(provisions_positive_sum, 0);
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO provisions_negative_sum
      FROM temp_provisions tp
     WHERE SUBSTR(tp.account_no, 1, 4) IN ('3303', '3304', '3305', '3307')
       AND tp.type = 1;
    provisions_negative_sum := ABS(NVL(provisions_negative_sum, 0));
    cred_val                := ABS(ROUND((provisions_positive_sum -
                                         provisions_negative_sum) / 1000));
    protocol_write_message_by_code('CC_CR_AFN', cred_val, outer_value);
  END;
  PROCEDURE provisions_uo_liab_check(outer_value IN NUMBER) IS
    cred_provisions_sum NUMBER;
  BEGIN
    IF g_report_date >= to_date('01.08.2013', 'dd.mm.yyyy') THEN
      RETURN;
    END IF;
    SELECT SUM(NVL(tp.provision_value, 0))
      INTO cred_provisions_sum
      FROM temp_provisions tp
     WHERE SUBSTR(tp.account_no, 1, 4) IN ('8917', '2875', '3315', '3316')
       AND tp.type = 1;
    cred_provisions_sum := ROUND(ABS(NVL(cred_provisions_sum, 0)) / 1000);
    protocol_write_message_by_code('CC_CL_AFN',
                                   cred_provisions_sum,
                                   outer_value);
  END;
  /*
  * 29.10.2014 - добавлена проверка на суммы остатков, по которым не указан номер счета
  */
  PROCEDURE check_sums_without_account_no IS
    v_sum NUMBER;
  BEGIN
    v_sum := 0;
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000)
      INTO v_sum
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr
     WHERE vch.credit_id = dr.credit_id
       AND vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date
       AND dr.rep_date = g_report_date
       AND dr.account_id IS NULL
       AND dr.type_code NOT IN ('6', '9', '11', '12', '13');
    v_sum := NVL(v_sum, 0);
    IF v_sum <> 0 THEN
      errors_count := errors_count + 1;
      protocol_write_message_by_code('REM_NOBA', v_sum, 0, 0, 1);
    END IF;
    v_sum := 0;
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000)
      INTO v_sum
      FROM v_credit_his vch,
           showcase.r_cust_remains_vert dr
     WHERE vch.credit_id = dr.credit_id
       AND vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date
       AND dr.rep_date = g_report_date
       AND dr.account_id IS NULL
       AND dr.type_code IN ('12', '13');
    v_sum := NVL(v_sum, 0);
    IF v_sum <> 0 THEN
      errors_count := errors_count + 1;
      protocol_write_message_by_code('MSFO_NOBA', v_sum, 0, 0, 1);
    END IF;
    IF g_report_date < to_date('01.08.2013', 'dd.mm.yyyy') THEN
      v_sum := 0;
      SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000)
        INTO v_sum
        FROM v_credit_his vch,
             showcase.r_cust_remains_vert dr
       WHERE vch.credit_id = dr.credit_id

         AND vch.creditor_id = g_creditor_id
         and dr.creditor_id = g_creditor_id

         AND (vch.maturity_date >= prev_report_date OR
             vch.maturity_date IS NULL)
         AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
         AND vch.open_date <= g_report_date
         AND vch.primary_contract_date < g_report_date
         AND dr.rep_date = g_report_date
         AND dr.account_id IS NULL
         AND dr.type_code IN ('11');
      v_sum := NVL(v_sum, 0);
      IF v_sum <> 0 THEN
        errors_count := errors_count + 1;
        protocol_write_message_by_code('UO_NOBA', v_sum, 0, 0, 1);
      END IF;
    END IF;
  END;
  /*
  * 05.11.2014 - добавлена проверка на неутвержденные договора
  */
  PROCEDURE check_not_approved_contracts IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(1)
      INTO v_count
      FROM v_credit_his vch
     WHERE vch.open_date <= g_report_date
       AND (vch.close_date IS NULL OR vch.close_date > g_report_date)
       AND vch.creditor_id = g_creditor_id
       AND vch.approved = 0;
    IF v_count <> 0 THEN
      errors_count := errors_count + 1;
      protocol_write_message_by_code('NOT_APPR', v_count, 0, 0, 1);
    END IF;
  END;

 /*
  * 26.06.2015 - добавлена проверка на договора с неверными датами
  */

   procedure check_wrong_dates is
    v_count number;
  begin
    select count(1)
      into v_count
      from (select 1
              from v_credit_his vch, showcase.r_cust_remains_vert dr
             where ((vch.maturity_date is not null and
                   vch.maturity_date < prev_report_date) or
                   vch.primary_contract_date >= g_report_date)
               and (vch.close_date > g_report_date or vch.close_date is null)
               and vch.open_date <= g_report_date
               and vch.credit_id = dr.credit_id
               and dr.rep_date = g_report_date
               and vch.creditor_id = g_creditor_id
               and dr.creditor_id = g_creditor_id
             group by vch.credit_id);
    if v_count <> 0 then
      protocol_write_message_by_code('WRONG_DATE', v_count, 0, 0, 2);
    end if;
  end;

  /*
  * 04.03.2015 - добавляются проверки на дублирующиеся залоги
  *            пока проверки работают в экспериментальном режиме
  *            и должны рассчитываться только для сотрудников НБ РК и БСБ
  */
  PROCEDURE check_duplicate_pledges IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(1)
      INTO v_count
      FROM (SELECT vph.contract_no, vph.pledge_type_id
              FROM v_credit_his vch, v_pledge_his vph
             WHERE vch.creditor_id = g_creditor_id
               AND vch.open_date <  = g_report_date
               AND (vch.close_date IS NULL OR vch.close_date > g_report_date)
               AND (vch.maturity_date IS NULL OR
                   vch.maturity_date > prev_report_date)
               AND vph.open_date <= g_report_date
               AND (vph.close_date IS NULL OR vph.close_date > g_report_date)
               AND vch.credit_id = vph.credit_id
             GROUP BY vph.contract_no, vph.pledge_type_id
            HAVING COUNT(DISTINCT vph.value_) > 1);
     if v_count > 0 then
      errors_count := errors_count + 1;
    protocol_write_message_by_code('DUP_PLEDGES', v_count, 0, 0, 1);
    end if;
  END;

  PROCEDURE check_different_pledge_lists IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(1)
      INTO v_count
      FROM (SELECT x.contract_no
              FROM (SELECT contract_no,
                           credit_id AS id,
                           listagg(pledge_type_id, ',') within GROUP(ORDER BY pledge_type_id) types FROM
                      (SELECT distinct vph.contract_no, vch.credit_id, vph.pledge_type_id
                      FROM v_credit_his vch, v_pledge_his vph
                     WHERE vch.creditor_id = g_creditor_id
                       AND vch.open_date <= g_report_date
                       AND (vch.close_date IS NULL OR
                           vch.close_date > g_report_date)
                       AND (vch.maturity_date IS NULL OR
                           vch.maturity_date > prev_report_date)
                       AND vph.open_date <= g_report_date
                       AND (vph.close_date IS NULL OR
                           vph.close_date > g_report_date)
                       AND vch.credit_id = vph.credit_id)
                     GROUP BY contract_no, credit_id) x
             GROUP BY x.contract_no
            HAVING COUNT(DISTINCT x.types) > 1);
    if v_count > 0 then
      errors_count := errors_count + 1;
    protocol_write_message_by_code('PLEDGE_LIST', v_count, 0, 0, 1);
    end if;
  END;

  PROCEDURE check_pledges IS
  BEGIN
      check_duplicate_pledges();
      check_different_pledge_lists();
  END;

  PROCEDURE check_fs_zpd_msfo IS
    v_cr_sum NUMBER;
    v_st_sum NUMBER;
  BEGIN
    IF g_creditor_id = 10000 THEN
      g_creditor_id := 22;
    END IF;
    DELETE FROM R_TEMP_FS_ZPD;
    INSERT INTO R_TEMP_FS_ZPD
      (drt_code, subj_type, is_se, value)
      select type_code, subj_type, is_se, sum(value) from ( SELECT dr.type_code,
             (CASE
               WHEN vdh.IS_PERSON = 1
                THEN
                1
               ELSE
                2
             END) subj_type,
             (case when vdh.is_person=1 then
             0
             else nvl((select voh.is_se from v_organization_his         voh where vdh.SUBJECT_ID = voh.org_id(+)
         AND (voh.close_date > g_report_date OR voh.close_date IS NULL)
         AND (voh.open_date IS NULL OR voh.open_date <= g_report_date)), 0) end ) is_se,
             NVL(dr.value, 0) value
        FROM v_credit_his                   vch,
             showcase.r_cust_remains_vert                 dr,
             showcase.r_ref_balance_account ba,
             v_debtor_his               vdh,
             showcase.r_ref_credit_type ct
       WHERE vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
         AND (vch.maturity_date >= prev_report_date OR
             vch.maturity_date IS NULL)
         AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
         AND vch.open_date <= g_report_date
         AND vch.primary_contract_date < g_report_date
         AND vch.credit_id = dr.credit_id
         AND dr.rep_date = g_report_date
         AND dr.account_id = ba.ref_balance_account_id
         AND ((ba.no_ LIKE '1%') OR
             (ba.no_ IN ('8911', '8912', '8713', '8714')))
         AND ba.no_ NOT LIKE '1880%'
         AND vch.credit_id = vdh.credit_id
         AND (vdh.close_date > g_report_date OR vdh.close_date IS NULL)
         AND vdh.open_date <= g_report_date
         AND vch.credit_type_id = ct.ref_credit_type_id
         AND ct.code NOT IN ('17', '18'))
       GROUP BY
                type_code,
                subj_type,
                is_se;
    FOR cont IN (SELECT *
                   FROM R_FS_ZPD_CONTROL fzc
                  ORDER BY fzc.id) LOOP
      SELECT SUM(tfz.value)
        INTO v_cr_sum
        FROM r_temp_fs_zpd tfz, R_FS_ZPD_DRT fzd
       WHERE tfz.subj_type = cont.subj_type
         AND tfz.is_se = cont.is_se
         AND fzd.control_id = cont.id
         AND tfz.drt_code = fzd.code;
      v_cr_sum := ROUND(NVL(v_cr_sum, 0) / 1000);
      SELECT SUM(st.znac)
        INTO v_st_sum
        FROM R_STAT_TEMP st, r_fs_zpd_st_code fs
       WHERE fs.control_id = cont.id
         AND fs.pokaz_id = st.id_pokaz;
      v_st_sum := NVL(v_st_sum, 0);
      protocol_write_message(cont.message_id, v_cr_sum, v_st_sum);
    END LOOP;
  END;

  FUNCTION get_pastdue_days (p_credit_id in number) return number is
  pastdue_days number;
  BEGIN
    SELECT MAX(g_report_date - nvl(dr.PASTDUE_OPEN_DATE, g_report_date)) into pastdue_days
    FROM showcase.r_cust_remains_vert dr
    WHERE dr.type_code IN (2,5)
    AND dr.CREDIT_ID    = p_credit_id
    AND dr.REP_DATE     = g_report_date
    AND (dr.value      <>0
    AND dr.value       IS NOT NULL)
    GROUP BY dr.credit_id;
    return(pastdue_days);
    exception
  when no_data_found then
  return 0;
  END;

  PROCEDURE check_fs_zpd_pastdue is
   v_essp_sum NUMBER;
   v_st_sum NUMBER;
   begin
    IF g_creditor_id = 10000 THEN
      g_creditor_id := 22;
    END IF;
     DELETE FROM R_TEMP_FS_ZPD_PASTDUE;
    INSERT INTO R_TEMP_FS_ZPD_PASTDUE
    (PASTDUE, VALUE)
    select (case when cred.co=0 then 1
                              when cred.co between 1 and 15 then 2
                              when cred.co between 16 and 30 then 3
                              when cred.co between 31 and 60 then 4
                              when cred.co between 61 and 90 then 5
                              when cred.co between 91 and 180 then 6
                               else 7 end) co,
          sum(NVL(cred.value, 0)) value
from ( SELECT get_pastdue_days(vch.credit_id) as co,
       dr.value
  FROM v_credit_his                   vch,
       v_debtor_his                   vdh,
       v_organization_his             voh,
       showcase.r_cust_remains_vert   dr,
       showcase.r_ref_balance_account ba,
       showcase.r_ref_credit_type     ct
 WHERE vch.creditor_id = g_creditor_id
   AND dr.creditor_id = g_creditor_id
   AND vdh.SUBJECT_ID = voh.org_id(+)
   AND (vch.maturity_date >= prev_report_date OR vch.maturity_date IS NULL)
   AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
   AND vch.open_date <= g_report_date
   AND vch.primary_contract_date < g_report_date
   AND vch.credit_id = dr.credit_id
   AND dr.rep_date = g_report_date
   AND dr.account_id = ba.ref_balance_account_id
   AND (ba.no_ LIKE '1%' or ba.no_ like '8%')
   AND vch.credit_id = vdh.credit_id
   AND (vdh.close_date > g_report_date OR vdh.close_date IS NULL)
   AND vdh.open_date <= g_report_date
   AND (voh.close_date > g_report_date OR voh.close_date IS NULL)
   AND voh.open_date(+) <= g_report_date
   AND vch.credit_type_id = ct.ref_credit_type_id
   AND dr.type_code in (1, 2)
   AND ct.code NOT IN ('17', '18')) cred
   group by (case
      when cred.co=0 then 1
      when cred.co between 1 and 15 then 2
      when cred.co between 16 and 30 then 3
      when cred.co between 31 and 60 then 4
      when cred.co between 61 and 90 then 5
      when cred.co between 91 and 180 then 6
      else 7 end);

    FOR cont IN (SELECT *
                   FROM R_FS_ZPD_PASTDUE_CONTROL fzc
                  ORDER BY fzc.id) LOOP

      begin
        SELECT tfz.value
          INTO v_essp_sum
          FROM R_TEMP_FS_ZPD_PASTDUE tfz
        WHERE tfz.pastdue = cont.code;
        v_essp_sum := ROUND(NVL(v_essp_sum, 0) / 1000);
         exception
      when no_data_found then
      v_essp_sum :=0;
     end;
     begin
       SELECT SUM(st.znac)
        INTO v_st_sum
        FROM R_STAT_TEMP st, R_FS_ZPD_PASTDUE_ST_CODE fs
       WHERE fs.control_id = cont.id
         AND fs.code = st.code;
      v_st_sum := NVL(v_st_sum, 0);
       exception
    when no_data_found then
    v_st_sum :=0;
    end;
      protocol_write_message(cont.message_id, v_essp_sum, v_st_sum);
    END LOOP;

   end;


  PROCEDURE check_fs_pzo_msfo is
   v_essp_sum NUMBER;
   v_st_sum NUMBER;
   counter number;
   begin
    IF g_creditor_id = 10000 THEN
       g_creditor_id := 22;
    END IF;
     DELETE FROM R_TEMP_FS_PZO;
    INSERT INTO R_TEMP_FS_PZO
    (ECON_TRADE, CREDIT_TYPE, SUBJECT_TYPE, VALUE)
    select ECON_TRADE_ID, credit_type_code, subj_type, sum(value) from
      (SELECT voh.ECON_TRADE_id,
             (CASE
               WHEN vdh.is_bank_creditor = 1
                THEN  1
                ELSE  2
             END) subj_type,
             NVL(dr.value, 0) value,
             ct.code credit_type_code
        FROM v_credit_his                   vch,
             showcase.r_cust_remains_vert                 dr,
             showcase.r_ref_balance_account ba,
             v_debtor_his               vdh,
             v_organization_his voh,
             showcase.r_ref_credit_type ct
       WHERE vch.creditor_id = g_creditor_id
         AND dr.creditor_id = g_creditor_id
         and vdh.SUBJECT_ID = voh.org_id(+)
         AND (vch.maturity_date >= prev_report_date OR
             vch.maturity_date IS NULL)
         AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
         AND vch.open_date <= g_report_date
         AND vch.primary_contract_date < g_report_date
         AND vch.credit_id = dr.credit_id
         AND dr.rep_date = g_report_date
         AND dr.account_id = ba.ref_balance_account_id
         AND (ba.no_ LIKE '1%' or ba.no_ like '8%')
         AND vch.credit_id = vdh.credit_id(+)
         AND (vdh.close_date(+) > g_report_date OR vdh.close_date(+) IS NULL)
         AND vdh.open_date(+) <= g_report_date
         AND (voh.close_date(+) > g_report_date OR voh.close_date(+) IS NULL)
         AND voh.open_date(+) <= g_report_date
         AND vch.credit_type_id = ct.ref_credit_type_id
         and dr.type_code in (1, 2)) group by ECON_TRADE_ID, subj_type, credit_type_code;

    FOR cont IN (SELECT *
                   FROM R_FS_PZO_CONTROL fzc
                  ORDER BY fzc.id) LOOP
    begin
        if(cont.code='K') then
        select sum(value) into v_essp_sum from
            (select
               tfp.value
            from  R_TEMP_FS_PZO tfp, r_fs_pzo_drt fpd, showcase.r_ref_econ_trade et
            where tfp.econ_trade = et.ref_econ_trade_id
            and substr(et.code, 1,2) = fpd.code
            and fpd.control_id = cont.id
            union
            select
                    tfp.value
            from R_TEMP_FS_PZO tfp
            where (tfp.credit_type in ('17', '18') or tfp.SUBJECT_TYPE = 1));
        else
            select  sum(tfp.value) into v_essp_sum
            from  R_TEMP_FS_PZO tfp, r_fs_pzo_drt fpd, showcase.r_ref_econ_trade et
            where tfp.econ_trade = et.ref_econ_trade_id
            and substr(et.code, 1,2) = fpd.code
            and fpd.control_id = cont.id;
        end if;
        v_essp_sum := ROUND(NVL(v_essp_sum, 0) / 1000);
    exception
    when no_data_found then
    v_essp_sum :=0;
    end;
    begin
       SELECT SUM(st.znac)
        INTO v_st_sum
        FROM R_STAT_TEMP st, r_fs_pzo_st_code fs
       WHERE fs.control_id = cont.id
         AND fs.code = st.code;
      v_st_sum := NVL(v_st_sum, 0);

    exception
      when no_data_found then
      v_st_sum :=0;
    end;
     protocol_write_message(cont.message_id, v_essp_sum, v_st_sum);
   END LOOP;
   end;

   /*
  * 2015.09.23 - добавлена проверка неоднородных провизий по портфельным займам
  */
  procedure check_portfolio_provisions is
    v_count number;
  begin
    select count(distinct vch.credit_id) as credits_count
      into v_count
      from showcase.r_cust_remains_vert dr, v_credit_his vch
     where dr.credit_id = vch.credit_id
       and dr.type_code = 11
       and dr.rep_date = g_report_date
       and vch.open_date <= g_report_date
       and (vch.close_date is null or vch.close_date > g_report_date)
       and vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       and vch.portfolio_id is not null
       and dr.value <> 0;
    if v_count <> 0 then
      protocol_write_message_by_code('PORTPROV_UO', v_count, 0, 0, 1);
      errors_count := errors_count + 1;
    end if;

    select count(distinct vch.credit_id) as credits_count
      into v_count
      from showcase.r_cust_remains_vert dr,v_credit_his vch
     where dr.credit_id = vch.credit_id
       and dr.type_code in (12, 13)
       and dr.rep_date = g_report_date
       and vch.open_date <= g_report_date
       and (vch.close_date is null or vch.close_date > g_report_date)
       and vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       and vch.portfolio_msfo_id is not null
       and dr.value <> 0;
    if v_count <> 0 then
      protocol_write_message_by_code('PORTPROV_MSFO', v_count, 0, 0, 1);
      errors_count := errors_count + 1;
    end if;
  end;

  PROCEDURE stat_check(is_brk IN INTEGER) IS
    v_mfo                         VARCHAR2(50);
    v_date                        INTEGER;
    stat_val                      NUMBER;
    cred_val                      NUMBER;
    stat_provision_credits_value  NUMBER;
    stat_provision_liab_value     NUMBER;
    stat_msfo_credits_value       NUMBER;
    stat_msfo_liab_value          NUMBER;
    n                             INTEGER;
    is_balance_account_controlled INTEGER;
    stat_sum_bal_act              NUMBER;
    cred_sum_bal_act              NUMBER;
    next_bal_act_no               VARCHAR2(50);
    cur_no                        VARCHAR2(50);
    provision_portfolio_value     NUMBER;
    v_val_7130                    NUMBER;
    v_val_8923                    NUMBER;
    v_3316                        NUMBER;
    v_temp_value                  NUMBER;
    v_is_error                    NUMBER;
  BEGIN
    check_sums_without_account_no();
    n                            := 0;
    stat_provision_credits_value := 0;
    stat_provision_liab_value    := 0;
    stat_msfo_liab_value         := 0;
    stat_msfo_credits_value      := 0;
    v_mfo                        := get_stat_creditor_bik(g_creditor_id);
    IF (v_mfo IS NULL) THEN
      -- Creditor not found
      protocol_write_message_by_code('NOBIK', 0, 0, 0, 1);
      RETURN;
    END IF;
    v_date := stat_f_pr_period(g_report_date);
    DELETE FROM r_stat_temp;
    IF is_brk <> 0 THEN
      BEGIN
        INSERT INTO r_stat_temp
          (id, znac, code, id_pokaz)
          SELECT rownum, od.znac, od.code_pokaz, od.id_pokaz
            FROM v_statistic_detail od
           WHERE od.mfo = v_mfo
             AND od.pr_period = v_date
            and od.dat_beg <= g_report_date
             AND (od.dat_end >= g_report_date OR od.dat_end IS NULL);
      END;
    ELSE
      BEGIN
        INSERT INTO r_stat_temp
          (id, znac, code, id_pokaz)
          SELECT rownum, od.znac, od.code_pokaz, od.id_pokaz
            FROM v_statistic_operdata od
           WHERE od.mfo = v_mfo
             AND od.pr_period = v_date
             and od.dat_beg <= g_report_date
             AND (od.dat_end >= g_report_date OR od.dat_end IS NULL);
      END;
    END IF;
    IF is_brk = 0 AND g_report_date >= to_date('01.11.2014', 'dd.mm.yyyy') THEN
      check_fs_zpd_msfo();
    END IF;

    IF is_brk = 0 and g_report_date >=to_date('01.07.2015', 'dd.mm.yyyy') then
      check_fs_zpd_pastdue();
      check_fs_pzo_msfo();
    END IF;

    stat_sum_bal_act := 0;
    cred_sum_bal_act := 0;
    next_bal_act_no  := '';
    v_3316           := 0;
    FOR ba IN (SELECT vba.no no,
                      NVL(st.znac, 0) st_sum,
                      NVL(cr.cr_sum, 0) cr_sum,
                      nvl(puo.co, 0) + nvl(pmsfo.co, 0) as port_co
                 FROM v_bal_act vba,
                      r_stat_temp st,
                      (select pf.balance_account_id ba_id, count(1) as co
                         from showcase.r_core_portfolio_flow_kfn pf
                        where pf.portfolio_data_creditor_id = g_creditor_id
                          and pf.rep_date = g_report_date
                        group by pf.balance_account_id) puo,
                      (select pf.balance_account_id ba_id, count(1) as co
                         from showcase.r_core_portfolio_flow_msfo pf
                        where pf.portfolio_data_creditor_id = g_creditor_id
                          and pf.rep_date = g_report_date
                        group by pf.balance_account_id) pmsfo,
                      (SELECT bal.id,
                              ROUND(SUM(NVL(d.value, 0)) / 1000) cr_sum
                         FROM showcase.r_cust_remains_vert d,
                              v_credit_his   vch,
                              v_bal_act      bal
                        WHERE vch.creditor_id = g_creditor_id
                          and d.creditor_id = g_creditor_id
                          AND d.credit_id = vch.credit_id(+)
                          AND d.rep_date = g_report_date
                          AND bal.id = d.account_id(+)
                          AND (vch.maturity_date >= prev_report_date OR
                              vch.maturity_date IS NULL)
                          AND (vch.close_date > g_report_date OR
                              vch.close_date IS NULL)
                          AND vch.open_date <= g_report_date
                          AND vch.primary_contract_date < g_report_date
                        GROUP BY bal.id) cr
                WHERE vba.no = st.code(+)
                  AND vba.id = cr.id(+)
                  and vba.id = puo.ba_id(+)
                  and vba.id = pmsfo.ba_id(+)
                  and (vba.open_date is null or
                  vba.open_date <= g_report_date)
                  and (vba.close_date is null or
                  vba.close_date >= g_report_date)
                ORDER BY vba.no) LOOP
      IF (ba.no > '3316' AND v_3316 = 0) THEN
        check_33(0, '3316');
        v_3316 := 1;
      END IF;
      is_balance_account_controlled := 1;
      IF g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') AND
         (SUBSTR(ba.no, 1, 4) IN (1310,
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
                                  1435)) THEN
        IF (LENGTH(ba.no) = 4) THEN
          stat_sum_bal_act := stat_sum_bal_act + NVL(ba.st_sum, 0);
        END IF;
        cred_sum_bal_act              := cred_sum_bal_act +
                                         NVL(ba.cr_sum, 0);
        is_balance_account_controlled := 0;
        cur_no                        := SUBSTR(ba.no, 1, 4);
        SELECT no
          INTO next_bal_act_no
          FROM (SELECT vba.no
                  FROM v_bal_act vba
                 WHERE vba.no > ba.no
                 ORDER BY vba.no)
         WHERE rownum = 1;
        IF SUBSTR(next_bal_act_no, 1, 4) <> SUBSTR(ba.no, 1, 4) THEN
          is_balance_account_controlled := 1;
          stat_val                      := stat_sum_bal_act;
          cred_val                      := cred_sum_bal_act;
          stat_sum_bal_act              := 0;
          cred_sum_bal_act              := 0;
        END IF;
      ELSE
        stat_val := NVL(ba.st_sum, 0);
        cred_val := NVL(ba.cr_sum, 0);
        cur_no   := ba.no;
      END IF;
      IF (NVL(stat_val, 0) <> 0 OR NVL(cred_val, 0) <> 0 OR ba.port_co <> 0) THEN
        IF SUBSTR(cur_no, 1, 4) IN ('1319', '1329', '1428', '1463') THEN
          n                            := n + 1;
          stat_msfo_credits_value      := stat_msfo_credits_value +
                                          stat_val;
          stat_provision_credits_value := stat_provision_credits_value +
                                          stat_val;
          SELECT SUM(NVL(tp.provision_value, 0))
            INTO provision_portfolio_value
            FROM temp_provisions tp
           WHERE tp.account_no = cur_no
             AND tp.type = 2;
          cred_val := ROUND(NVL(provision_portfolio_value, 0) / 1000);
          IF g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') THEN
            SELECT SUM(NVL(tp.provision_value, 0))
              INTO provision_portfolio_value
              FROM temp_provisions tp
             WHERE tp.account_no = cur_no
               AND tp.type = 1;
            cred_val := cred_val +
                        ROUND(NVL(provision_portfolio_value, 0) / 1000);
          END IF;
          cred_val := ABS(cred_val);
          stat_val := ABS(stat_val);
          IF g_report_date < to_date('01.09.2013', 'dd.mm.yyyy') THEN
            v_is_error := 0;
          ELSE
            IF ABS(cred_val - stat_val) <= ADM_ERROR THEN
              v_is_error := 0;
            ELSE
              v_is_error := 1;
            END IF;
          END IF;
          IF v_is_error <> 0 THEN
            errors_count := errors_count + 1;
          END IF;
          protocol_write(cur_no,
                         cred_val,
                         stat_val,
                         cred_val - stat_val,
                         v_is_error);
        ELSE
          IF SUBSTR(cur_no, 1, 4) IN ('3303', '3304', '3305', '3307') THEN
            n := n + 1;
            IF LENGTH(cur_no) = 4 THEN
              stat_provision_credits_value := stat_provision_credits_value -
                                              stat_val;
              check_33(stat_val, ba.no);
            END IF;
          ELSE
            IF ba.no in ('7130', '6675', '8923') THEN
              n := n + 1;
              IF ba.no = '7130' THEN
                SELECT SUM(NVL(dr.value, 0))
                  INTO v_val_7130
                  FROM v_credit_his vch, showcase.r_cust_remains_vert dr
                 WHERE vch.creditor_id = g_creditor_id
                   AND dr.credit_id = vch.credit_id(+)
                   AND dr.rep_date = g_report_date
                   AND (vch.maturity_date >= prev_report_date OR
                       vch.maturity_date IS NULL)
                   AND (vch.close_date > g_report_date OR
                       vch.close_date IS NULL)
                   AND vch.open_date <= g_report_date
                   AND vch.primary_contract_date < g_report_date
                   AND dr.type_code = '6'
                   AND dr.account_id IS NULL;
                cred_val := cred_val + ROUND(NVL(v_val_7130, 0) / 1000);
              END IF;
              IF ba.no = '8923' THEN
                SELECT SUM(NVL(dr.value, 0))
                  INTO v_val_8923
                  FROM v_credit_his vch, showcase.r_cust_remains_vert dr
                 WHERE vch.creditor_id = g_creditor_id
                   AND dr.credit_id = vch.credit_id(+)
                   AND dr.rep_date = g_report_date
                   AND (vch.maturity_date >= prev_report_date OR
                       vch.maturity_date IS NULL)
                   AND (vch.close_date > g_report_date OR
                       vch.close_date IS NULL)
                   AND vch.open_date <= g_report_date
                   AND vch.primary_contract_date < g_report_date
                   AND dr.type_code = '6'
                   AND dr.account_id IS NULL;
                cred_val := cred_val + ROUND(NVL(v_val_8923, 0) / 1000);
                if g_report_date >= to_date('01.07.2015', 'dd.mm.yyyy') then
                  v_is_error := 2;

                else
                  if cred_val <= stat_val + ADM_ERROR then
                    v_is_error := 0;
                  else
                    v_is_error := 1;
                  end if;
                end if;
              else
                if cred_val <= stat_val + ADM_ERROR then
                  v_is_error := 0;
                else

                  v_is_error := 1;
                end if;
              END IF;
              protocol_write(cur_no,
                             cred_val,
                             stat_val,
                             cred_val - stat_val,
                             v_is_error);
              if v_is_error = 1 then
                errors_count := errors_count + 1;
              end if;
            ELSE
              IF SUBSTR(cur_no, 1, 4) IN
                 ('8708', '8709', '8917', '2875', '3315', '3316') THEN
                n := n + 1;
                IF LENGTH(cur_no) = 4 OR
                   (SUBSTR(cur_no, 1, 4) = '2875' AND LENGTH(ba.no) = 7) THEN
                  stat_provision_liab_value := stat_provision_liab_value +
                                               stat_val;
                  IF SUBSTR(cur_no, 1, 4) IN ('3315', '3316') THEN
                    IF (SUBSTR(cur_no, 1, 4) = '3316') THEN
                      v_3316 := 1;
                    END IF;
                    check_33(stat_val, cur_no);
                  END IF;
                  IF SUBSTR(cur_no, 1, 4) IN
                     ('8917', '2875', '8708', '8709') THEN
                    IF g_report_date >= to_date('01.09.2013', 'dd.mm.yyyy') THEN
                      n := n + 1;

                      if is_balance_account_controlled <> 0 then

                        select sum(nvl(tp.provision_value, 0))
                          into v_temp_value
                          from temp_provisions tp
                         where tp.account_no like cur_no || '%'
                           and tp.is_portfolio = 1;

                        cred_val := cred_val +
                                    round(nvl(v_temp_value, 0) / 1000);
                        if cred_val <> 0 OR stat_val <> 0 then
                          protocol_write(cur_no, cred_val, stat_val);
                        end if;

                      end if;
                    END IF;
                    stat_msfo_liab_value := stat_msfo_liab_value + stat_val;
                  END IF;
                END IF;
              ELSE
                IF cred_val <> 0 OR stat_val <> 0 THEN
                  n := n + 1;
                  IF is_balance_account_controlled <> 0 THEN
                    protocol_write(cur_no, cred_val, stat_val);
                  END IF;
                END IF;
              END IF;
            END IF;
          END IF;
        END IF;
      END IF;
    END LOOP;
    IF (v_3316 = 0) THEN
      check_33(0, '3316');
    END IF;
    stat_msfo_credits_value := ABS(NVL(stat_msfo_credits_value, 0));
    provisions_msfo_credits_check(stat_msfo_credits_value);
    stat_msfo_liab_value := ABS(NVL(stat_msfo_liab_value, 0));
    provisions_msfo_liab_check(stat_msfo_liab_value);
    stat_provision_credits_value := ABS(NVL(stat_provision_credits_value, 0));
    provisions_uo_credits_check(stat_provision_credits_value);
    stat_provision_liab_value := ABS(NVL(stat_provision_liab_value, 0));
    provisions_uo_liab_check(stat_provision_liab_value);
    IF n = 0 THEN
      protocol_write_message_by_code('NO_REMAINS', NULL, NULL, NULL, 1);
      errors_count := errors_count + 1;
    END IF;
  END;
  PROCEDURE ipoteque_check IS
    nokbdb_val                   NUMBER;
    cred_val                     NUMBER;
    n                            NUMBER;
    nokbdb_provision_credits_sum NUMBER;
    nokbdb_provision_liab_value  NUMBER;
    nokbdb_msfo_credits_value    NUMBER;
    nokbdb_msfo_liab_value       NUMBER;
  BEGIN
    check_sums_without_account_no();
    nokbdb_msfo_credits_value    := 0;
    nokbdb_msfo_liab_value       := 0;
    nokbdb_provision_credits_sum := 0;
    nokbdb_provision_liab_value  := 0;
    n                            := 0;
    INSERT INTO r_stat_temp
      (id, znac, code)
      SELECT rownum, ni.sum, ni.no
        FROM v_nokbdb_ipoteque ni
       WHERE ni.rep_date = g_report_date
         AND ni.creditor_id = g_creditor_id;
    FOR ba IN (SELECT SUBSTR(vba.no, 1, 4) no,
                      NVL(st.znac, 0) st_sum,
                      NVL(cr.cr_sum, 0) cr_sum
                 FROM (SELECT DISTINCT SUBSTR(no, 1, 4) no FROM v_bal_act) vba,
                      r_stat_temp st,
                      (SELECT SUBSTR(bal.no, 1, 4) no,
                              ROUND(SUM(NVL(d.value, 0)) / 1000) cr_sum
                         FROM showcase.r_cust_remains_vert d,
                              v_credit_his   vch,
                              v_bal_act      bal
                        WHERE vch.creditor_id = g_creditor_id
                          and d.creditor_id = g_creditor_id
                          AND d.credit_id = vch.credit_id(+)
                          AND d.rep_date = g_report_date
                          AND bal.id = d.account_id(+)
                          AND (vch.maturity_date >= prev_report_date OR
                              vch.maturity_date IS NULL)
                          AND (vch.close_date > g_report_date OR
                              vch.close_date IS NULL)
                          AND vch.open_date <= g_report_date
                          AND vch.primary_contract_date < g_report_date
                        GROUP BY SUBSTR(bal.no, 1, 4)) cr
                WHERE vba.no = st.code(+)
                  AND vba.no = cr.no(+)
                  AND (NVL(st.znac, 0) <> 0 OR NVL(cr.cr_sum, 0) <> 0)
                ORDER BY vba.no) LOOP
      nokbdb_val := NVL(ba.st_sum, 0);
      cred_val   := NVL(ba.cr_sum, 0);
      IF SUBSTR(ba.no, 1, 4) IN ('1319', '1329', '1428', '1463') THEN
        n                            := n + 1;
        nokbdb_msfo_credits_value    := nokbdb_msfo_credits_value +
                                        nokbdb_val;
        nokbdb_provision_credits_sum := nokbdb_provision_credits_sum +
                                        nokbdb_val;
      ELSE
        IF ba.no IN ('3303', '3304', '3305', '3307') THEN
          n                            := n + 1;
          nokbdb_provision_credits_sum := nokbdb_provision_credits_sum -
                                          nokbdb_val;
          check_33(nokbdb_val, ba.no);
        ELSE
          IF ba.no = '7130' OR ba.no = '6675' OR ba.no = '8923' THEN
            n := n + 1;
            IF cred_val <= nokbdb_val THEN
              protocol_write(ba.no,
                             cred_val,
                             nokbdb_val,
                             cred_val - nokbdb_val,
                             0);
            ELSE
              protocol_write(ba.no,
                             cred_val,
                             nokbdb_val,
                             cred_val - nokbdb_val,
                             1);
              errors_count := errors_count + 1;
            END IF;
          ELSE
            IF SUBSTR(ba.no, 1, 4) IN
               ('8708', '8709', '8917', '2875', '3315', '3316') THEN
              n                           := n + 1;
              nokbdb_provision_liab_value := nokbdb_provision_liab_value +
                                             nokbdb_val;
              IF SUBSTR(ba.no, 1, 4) IN ('3315', '3316') THEN
                check_33(nokbdb_val, ba.no);
              END IF;
              IF SUBSTR(ba.no, 1, 4) IN ('8917', '2875', '8708', '8709') THEN
                nokbdb_msfo_liab_value := nokbdb_msfo_liab_value +
                                          nokbdb_val;
              END IF;
            ELSE
              IF cred_val <> 0 OR nokbdb_val <> 0 THEN
                n := n + 1;
                protocol_write(ba.no, cred_val, nokbdb_val);
              END IF;
            END IF;
          END IF;
        END IF;
      END IF;
    END LOOP;
    nokbdb_msfo_credits_value := ABS(NVL(nokbdb_msfo_credits_value, 0));
    provisions_msfo_credits_check(nokbdb_msfo_credits_value);
    nokbdb_msfo_liab_value := ABS(NVL(nokbdb_msfo_liab_value, 0));
    provisions_msfo_liab_check(nokbdb_msfo_liab_value);
    nokbdb_provision_credits_sum := ABS(NVL(nokbdb_provision_credits_sum, 0));
    provisions_uo_credits_check(nokbdb_provision_credits_sum);
    nokbdb_provision_liab_value := ABS(NVL(nokbdb_provision_liab_value, 0));
    provisions_uo_liab_check(nokbdb_provision_liab_value);
    IF n = 0 THEN
      -- no data found
      protocol_write_message_by_code('NO_REMAINS');
      errors_count := errors_count + 1;
    END IF;
  END;
  PROCEDURE nokbdb_od_opd(p_creditor_code IN VARCHAR2) IS
    nokb_value NUMBER;
    cred_val   NUMBER;
  BEGIN
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000, 0)
      INTO cred_val
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND (dr.type_code = '1' OR dr.type_code = '2')
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id IN (14, 75) -- kind.code = 'CR'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    cred_val := NVL(cred_val, 0);
    SELECT SUM(NVL(vnr.main_dept, 0))
      INTO nokb_value
      FROM v_nokbdb_report vnr
     WHERE vnr.creditor_code = p_creditor_code
       AND vnr.report_date = g_report_date;
    nokb_value := NVL(nokb_value, 0);
    protocol_write_message_by_code('CC_CR_OD', cred_val, nokb_value);
  END;
  PROCEDURE nokbdb_ov_opv(p_creditor_code IN VARCHAR2) IS
    nokb_value NUMBER;
    cred_val   NUMBER;
  BEGIN
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000, 0)
      INTO cred_val
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND (dr.type_code = '4' OR dr.type_code = '5')
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id IN (14, 75) --kind.code = 'CR'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    cred_val := NVL(cred_val, 0);
    SELECT SUM(NVL(vnr.prem, 0))
      INTO nokb_value
      FROM v_nokbdb_report vnr
     WHERE vnr.creditor_code = p_creditor_code
       AND vnr.report_date = g_report_date;
    nokb_value := NVL(nokb_value, 0);
    protocol_write_message_by_code('CC_CR_OV', cred_val, nokb_value);
  END;
  PROCEDURE nokbdb_pnuo_pouo(p_creditor_code IN VARCHAR2) IS
    nokb_value      NUMBER;
    cred_flow_value NUMBER;
    portfolio_value NUMBER;
    cred_val        NUMBER;
  BEGIN
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000)
      INTO cred_flow_value
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND dr.type_code = '11'
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id IN (14, 75) --kind.code = 'CR'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    cred_flow_value := NVL(cred_flow_value, 0);
    SELECT ROUND(SUM(NVL(pf.value, 0)) / 1000)
      INTO portfolio_value
      FROM showcase.r_core_portfolio_flow_kfn pf
     WHERE pf.portfolio_data_creditor_id = g_creditor_id
       AND pf.rep_date = g_report_date
       AND EXISTS
     (SELECT vch.credit_id
              FROM v_credit_his vch
             WHERE
             (vch.portfolio_id = pf.portfolio_id OR
             vch.inner_portfolio_id = pf.portfolio_id)
          AND (vch.maturity_date >= prev_report_date OR
             vch.maturity_date IS NULL)
          AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
          AND vch.open_date <= g_report_date
          AND vch.primary_contract_date < g_report_date);
    portfolio_value := NVL(portfolio_value, 0);
    cred_val        := cred_flow_value + portfolio_value;
    SELECT SUM(NVL(vnr.fact_vict, 0))
      INTO nokb_value
      FROM v_nokbdb_report vnr
     WHERE vnr.creditor_code = p_creditor_code
       AND vnr.report_date = g_report_date;
    nokb_value := NVL(nokb_value, 0);
    protocol_write_message_by_code('CC_CR_PNUO', cred_val, nokb_value);
  END;
  PROCEDURE nokbdb_uo_check(p_creditor_code IN VARCHAR2) IS
    main_debt    NUMBER;
    prem         NUMBER;
    fact_vict    NUMBER;
    od_opd_value NUMBER;
    ov_opv_value NUMBER;
    pnuo_value   NUMBER;
  BEGIN
    BEGIN
      SELECT vnlc.main_dept, vnlc.prem, vnlc.fact_vict
        INTO main_debt, prem, fact_vict
        FROM v_nokbdb_liab_class vnlc
       WHERE vnlc.creditor_code = p_creditor_code
         AND vnlc.report_date = g_report_date;
    EXCEPTION
      WHEN no_data_found THEN
        main_debt := 0;
        prem      := 0;
        fact_vict := 0;
    END;
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000, 0)
      INTO od_opd_value
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND (dr.type_code = '1' OR dr.type_code = '2')
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id = 15 --kind.code = 'CL'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    od_opd_value := NVL(od_opd_value, 0);
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000, 0)
      INTO ov_opv_value
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND (dr.type_code = '4' OR dr.type_code = '5')
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id = 15 --kind.code = 'CL'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    ov_opv_value := NVL(ov_opv_value, 0);
    SELECT ROUND(SUM(NVL(dr.value, 0)) / 1000)
      INTO pnuo_value
      FROM v_credit_his   vch,
           showcase.r_cust_remains_vert dr,
           showcase.r_ref_credit_type ct
     WHERE vch.creditor_id = g_creditor_id
       AND vch.credit_id = dr.credit_id
       AND dr.rep_date = g_report_date
       AND dr.type_code = '11'
       AND vch.credit_type_id = ct.ref_credit_type_id
       AND ct.kind_id = 15 --kind.code = 'CL'
       AND (vch.maturity_date >= prev_report_date OR
           vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date;
    pnuo_value := NVL(pnuo_value, 0);
    protocol_write_message_by_code('CC_CL_OD', od_opd_value, main_debt);
    protocol_write_message_by_code('CC_CL_OV', ov_opv_value, prem);
    protocol_write_message_by_code('CC_CL_PNUO', pnuo_value, fact_vict);
  END;
  PROCEDURE nokbdb_check IS
    nokbdb_creditor_code VARCHAR2(50);
  BEGIN
    SELECT crn.nokbdb_code
      INTO nokbdb_creditor_code
      FROM showcase.r_ref_creditor crn
     WHERE crn.ref_creditor_id = g_creditor_id
       AND crn.nokbdb_code IS NOT NULL;
    nokbdb_od_opd(nokbdb_creditor_code);
    nokbdb_ov_opv(nokbdb_creditor_code);
    nokbdb_pnuo_pouo(nokbdb_creditor_code);
    nokbdb_uo_check(nokbdb_creditor_code);
  END;
  PROCEDURE build_temp_provisions IS
  BEGIN
    DELETE FROM temp_provisions;
    INSERT INTO temp_provisions
      (provision_value, account_no, type, is_portfolio)
      SELECT SUM(NVL(dr.value, 0)), ba.no_, 1, 0
        FROM v_credit_his   vch,
             showcase.r_cust_remains_vert dr,
             showcase.r_ref_balance_account ba
       WHERE
       (vch.maturity_date >= prev_report_date OR vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date
       AND vch.credit_id = dr.credit_id
       AND dr.account_id = ba.ref_balance_account_id
       AND dr.rep_date = g_report_date
       AND vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND dr.type_code = '11'
       GROUP BY ba.no_;
    INSERT INTO temp_provisions
      (provision_value, account_no, type, is_portfolio)
      SELECT SUM(NVL(dr.value, 0)), ba.no_, 2, 0
        FROM v_credit_his   vch,
             showcase.r_cust_remains_vert dr,
             showcase.r_ref_balance_account ba
       WHERE
       (vch.maturity_date >= prev_report_date OR vch.maturity_date IS NULL)
       AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
       AND vch.open_date <= g_report_date
       AND vch.primary_contract_date < g_report_date
       AND vch.credit_id = dr.credit_id
       AND dr.account_id = ba.ref_balance_account_id
       AND dr.rep_date = g_report_date
       AND vch.creditor_id = g_creditor_id
       and dr.creditor_id = g_creditor_id
       AND dr.type_code IN ('12', '13')
       GROUP BY ba.no_;
    INSERT INTO temp_provisions
      (provision_value, account_no, type, is_portfolio)
      SELECT SUM(NVL(pf.value, 0)), ba.no_, 1, 1
        FROM showcase.r_core_portfolio_flow_kfn pf,
             showcase.r_ref_balance_account     ba
       WHERE pf.rep_date = g_report_date
         AND pf.portfolio_creditor_id = g_creditor_id
         AND pf.balance_account_id = ba.ref_balance_account_id
         AND EXISTS
       (SELECT vch.credit_id
                FROM v_credit_his vch
               WHERE
               (vch.portfolio_id = pf.portfolio_id OR
               vch.inner_portfolio_id = pf.portfolio_id)
            AND (vch.maturity_date >= prev_report_date OR
               vch.maturity_date IS NULL)
            AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
            AND vch.open_date <= g_report_date
            AND vch.primary_contract_date < g_report_date
            AND vch.creditor_id = g_creditor_id)
       GROUP BY ba.no_;
    INSERT INTO temp_provisions
      (provision_value, account_no, type, is_portfolio)
      SELECT SUM(NVL(pf.value, 0)), ba.no_, 2, 1
        FROM showcase.r_core_portfolio_flow_msfo pf,
             showcase.r_ref_balance_account      ba
       WHERE pf.rep_date = g_report_date
         AND pf.portfolio_data_creditor_id = g_creditor_id
         AND pf.balance_account_id = ba.ref_balance_account_id
         AND EXISTS
       (SELECT vch.credit_id
                FROM v_credit_his vch
               WHERE
               vch.portfolio_msfo_id = pf.portfolio_id
            AND (vch.maturity_date >= prev_report_date OR
               vch.maturity_date IS NULL)
            AND (vch.close_date > g_report_date OR vch.close_date IS NULL)
            AND vch.open_date <= g_report_date
            AND vch.primary_contract_date < g_report_date
            AND vch.creditor_id = g_creditor_id)
       GROUP BY ba.no_;
    IF g_report_date < to_date('01.07.2013', 'dd.mm.yyyy') THEN
      FOR empty_port IN (SELECT p.code, SUM(NVL(pf.value, 0)) AS sm
                           FROM showcase.r_core_portfolio_flow_kfn pf,
                                showcase.r_ref_portfolio           p
                          WHERE pf.rep_date = g_report_date
                            AND pf.portfolio_data_creditor_id = g_creditor_id
                            AND pf.portfolio_id = p.ref_portfolio_id
                            AND NOT EXISTS
                          (SELECT vch.credit_id
                                   FROM v_credit_his vch
                                  WHERE
                                  (vch.portfolio_id = p.ref_portfolio_id OR
                                  vch.inner_portfolio_id =
                                  p.ref_portfolio_id)
                               AND (vch.maturity_date >= prev_report_date OR
                                  vch.maturity_date IS NULL)
                               AND (vch.close_date > g_report_date OR
                                  vch.close_date IS NULL)
                               AND vch.open_date <= g_report_date
                               AND vch.primary_contract_date < g_report_date
                               AND vch.creditor_id = g_creditor_id)
                          GROUP BY p.id, p.code) LOOP
        protocol_write_message_by_code('EMPPORTUO',
                                       'Нет договоров: [' ||
                                       empty_port.code || ']',
                                       '0',
                                       ROUND(NVL(empty_port.sm, 0) / 1000),
                                       1);
      END LOOP;
    END IF;
    FOR empty_port_msfo IN (SELECT p.code, SUM(NVL(pf.value, 0)) AS sm
                              FROM showcase.r_core_portfolio_flow_msfo pf,
                                   showcase.r_ref_portfolio            p
                             WHERE pf.rep_date = g_report_date
                               AND pf.portfolio_data_creditor_id = g_creditor_id
                               AND pf.portfolio_id = p.ref_portfolio_id
                               AND NOT EXISTS
                             (SELECT vch.credit_id
                                      FROM v_credit_his vch
                                     WHERE
                                     vch.portfolio_msfo_id =
                                     p.ref_portfolio_id
                                  AND (vch.maturity_date >= prev_report_date OR
                                     vch.maturity_date IS NULL)
                                  AND (vch.close_date > g_report_date OR
                                     vch.close_date IS NULL)
                                  AND vch.open_date <= g_report_date
                                  AND vch.primary_contract_date <
                                     g_report_date
                                  AND vch.creditor_id = g_creditor_id)
                             GROUP BY p.id, p.code
                            HAVING SUM(NVL(pf.value, 0)) <> 0) LOOP
      protocol_write_message_by_code('EMPPORTMSF',
                                     'Нет договоров: [' ||
                                     empty_port_msfo.code || ']',
                                     '0',
                                     ROUND(NVL(empty_port_msfo.sm, 0) / 1000),
                                     1);
    END LOOP;
  END;
  PROCEDURE run_cross_check(p_creditor IN NUMBER,
                            p_date     IN DATE,
                            Err_Code   OUT INTEGER,
                            Err_Msg    OUT VARCHAR2) IS
    ProcName CONSTANT VARCHAR2(30) := 'cross_check';
    report_type_failed_id   INTEGER;
    report_type_errors_id   INTEGER;
    report_type_success_id  INTEGER;
    report_type_finished_id INTEGER;
    with_error_status_id    INTEGER; --input info status: ""Executed with errors""
    without_error_status_id INTEGER; --input info status: ""Executed withour errors""
    subject_type_code       INTEGER;
    creditor_code           VARCHAR2(10);
    org_approved_status_id  INTEGER;
    org_approving_status_id INTEGER;
    v_report_period_months  NUMBER;
    v_now                   DATE;
  BEGIN
    SELECT st.report_period_duration_months
      INTO v_report_period_months
      FROM showcase.r_ref_creditor c, showcase.r_ref_subject_type st
     WHERE c.ref_creditor_id = p_creditor
       AND c.subject_type_id = st.ref_subject_type_id;
    v_report_period_months  := NVL(v_report_period_months, 1);
    g_report_date           := p_date;
    prev_report_date        := add_months(p_date, -v_report_period_months);
    g_creditor_id           := p_creditor;
    report_type_failed_id   := 4;
    report_type_success_id  := 2;
    report_type_errors_id   := 1;
    report_type_finished_id := 5;
    org_approved_status_id  := 7;
    org_approving_status_id :=6;
    --Variable initialization
    without_error_status_id      := 2;
    with_error_status_id         := 1;
    CROSS_CHECK_PROTOCOL_TYPE_ID := 94;
    CROSS_CHECK_MESSAGE_TYPE_ID  := 93;
    SELECT st.code
      INTO subject_type_code
      FROM showcase.r_ref_creditor c, showcase.r_ref_subject_type st
     WHERE c.ref_creditor_id = p_creditor
       AND c.subject_type_id = st.ref_subject_type_id;
    --Getting next input info id
    SELECT cross_check_seq.nextval INTO global_cross_check_id FROM dual;
    SELECT c.code
      INTO creditor_code
      FROM showcase.r_ref_creditor c
     WHERE c.ref_creditor_id = p_creditor;
    INSERT INTO cross_check
      (id, user_name, date_begin, creditor_id, report_date, status_id)
    VALUES
      (global_cross_check_id,
       user_name,
       sysdate,
       p_creditor,
       p_date,
       with_error_status_id);
    COMMIT;
    errors_count := 0;
    check_pledges();
    -- check_not_approved_contracts();
    check_wrong_dates();
    check_portfolio_provisions();
    build_temp_provisions();
    IF creditor_code = '907' THEN
      stat_check(1);
    ELSE
      IF subject_type_code = '0001' THEN
        stat_check(0);
      ELSE
        IF subject_type_code = '0002' THEN
          ipoteque_check();
        ELSE
          nokbdb_check();
        END IF;
      END IF;
    END IF;
    COMMIT;
    SELECT SYSDATE INTO v_now FROM DUAL;
    LOOP
      EXIT WHEN v_now +(60 / 86400) = SYSDATE;
    END LOOP;
    IF errors_count = 0 THEN
      UPDATE cross_check cc
         SET cc.status_id = without_error_status_id
       WHERE cc.id = global_cross_check_id;
    END IF;
    UPDATE cross_check cc
       SET cc.date_end = sysdate
     WHERE cc.id = global_cross_check_id;
    COMMIT;
    IF errors_count = 0 THEN
      UPDATE eav_report@core r
         SET r.status_id = report_type_success_id
       WHERE r.creditor_id = g_creditor_id
         AND r.report_date = g_report_date
         AND r.status_id <> report_type_finished_id
         AND r.status_id <> org_approved_status_id
         and r.status_id <>org_approving_status_id;
    ELSE
      UPDATE eav_report@core r
         SET r.status_id = report_type_errors_id
       WHERE r.creditor_id = g_creditor_id
         AND r.report_date = g_report_date
         AND r.status_id <> report_type_finished_id
         AND r.status_id <> org_approved_status_id
         and r.status_id <>org_approving_status_id;
      COMMIT;
    END IF;
    COMMIT;
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' - ' || '/E\' || SUBSTR(SQLERRM, 1, 255) ||

                  '/R\' || 'Ошибка в контроле';
      BEGIN
        protocol_write('ERROR ' || Err_Code || ': ' || Err_Msg);
        COMMIT;
      EXCEPTION
        WHEN OTHERS THEN
          RETURN;
      END;
  END;
  PROCEDURE cross_check(p_creditor IN NUMBER,
                        p_date     IN DATE,
                        p_user_id  IN NUMBER,
                        Err_Code   OUT INTEGER,
                        Err_Msg    OUT VARCHAR2) IS
  BEGIN
    SELECT pu.first_name || ' ' || pu.last_name, pu.email
      INTO user_name, user_email_address
      FROM eav_a_user@core pu
     WHERE pu.user_id = p_user_id;
    run_cross_check(p_creditor, p_date, Err_Code, Err_Msg);
  END;
  PROCEDURE cross_check_all(p_date    IN DATE,
                            p_user_id IN NUMBER,
                            Err_Code  OUT INTEGER,
                            Err_Msg   OUT VARCHAR2) IS
    ProcName CONSTANT VARCHAR2(30) := 'cross_check_all';
  BEGIN
    FOR c IN (SELECT rc.ref_creditor_id AS id
                FROM showcase.r_ref_creditor        rc,
                     showcase.r_ref_creditor_branch cb
               WHERE rc.code = cb.code
                 AND cb.main_office_id IS NULL
                 AND rc.subject_type_id = 1) LOOP
      cross_check(c.id, p_date, p_user_id, Err_Code, Err_Msg);
    END LOOP;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' - ' || '/E\' || SUBSTR(SQLERRM, 1, 255) ||

                  '/R\' || 'Procedure error';
  END;
END DATA_VALIDATION;

