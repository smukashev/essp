create or replace PROCEDURE "CREDITOR_ACTUAL_COUNT"(
    p_cur OUT utility.TCursor,
    p_maturity_date IN DATE,
    p_open_date     IN DATE)
IS
BEGIN
  OPEN p_cur FOR

  SELECT t1.REF_CREDITOR_ID, t2.actual_count FROM showcase.r_ref_creditor t1,
    (SELECT creditor_id,
      COUNT(DISTINCT credit_id) AS actual_count
    FROM
      (SELECT * FROM showcase.r_core_credit
      UNION ALL
      SELECT * FROM showcase.r_core_credit_his
      )
    WHERE
    (maturity_date IS NULL
    OR maturity_date     >= p_maturity_date)
    AND
    (close_date IS NULL
    OR close_date     > p_open_date)
    AND open_date        <= p_open_date
    GROUP BY creditor_id
    ) t2 WHERE t1.ref_creditor_id = t2.creditor_id order by t1.name;


END CREDITOR_ACTUAL_COUNT;


