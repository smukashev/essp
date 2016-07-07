CREATE OR REPLACE PROCEDURE LX_DOCTYPECODE16_FIX AS
  v_min_rd DATE;
  BEGIN
    FOR cr IN
    (SELECT *
     FROM LX_DOCTYPECODE16)
    LOOP
      SELECT MIN(report_date)
      INTO v_min_rd
      FROM eav_be_complex_values
      WHERE entity_id = cr.entity_id;
      INSERT
      INTO EAV_BE_STRING_VALUES (
        entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
      VALUES (
        cr.entity_id, 15, cr.creditor_id, v_min_rd, cr.name_ru, 0, 1);
    END LOOP;
  END LX_DOCTYPECODE16_FIX;