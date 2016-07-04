create or replace PROCEDURE LX_AMOUNT_FIX as
  v_min_rd date;
BEGIN
  FOR cr IN
  (SELECT * FROM lx_amount)
  LOOP
    SELECT MIN(report_date)
    INTO v_min_rd
    FROM eav_be_complex_values
    WHERE entity_id = cr.entity_id;
    INSERT
    INTO eav_be_double_values (
        entity_id,attribute_id,creditor_id,report_date,value,is_closed,is_last)
      VALUES(
        cr.entity_id,155,cr.creditor_id,v_min_rd,cr.amount,0,1);
  END LOOP;
END LX_AMOUNT_FIX;