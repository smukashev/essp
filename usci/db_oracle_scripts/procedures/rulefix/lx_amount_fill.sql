CREATE OR REPLACE PROCEDURE LX_AMOUNT_FILL
AS
BEGIN

  execute immediate 'truncate table lx_amount#';
  execute immediate 'truncate table lx_amount';

  INSERT
  INTO lx_amount#
    (SELECT *
      FROM
        (SELECT ebe.id       AS entity_id,
          c1.entity_value_id AS type_id,
          c1.creditor_id,
          (SELECT creditor_id
          FROM v_creditor_map
          WHERE ref_creditor_id = c1.creditor_id
          ) cid,
          (SELECT primary_contract_no
          FROM r_core_credit@showcase
          WHERE credit_id = ebe.id
          ) pno,
          (SELECT primary_contract_date
          FROM r_core_credit@showcase
          WHERE credit_id = ebe.id
          ) pdate,
          c2.value AS amount
        FROM eav_be_entities ebe
        LEFT OUTER JOIN eav_be_complex_values c1
        ON c1.attribute_id = 64
        AND c1.entity_id   = ebe.id
        LEFT OUTER JOIN eav_be_double_values c2
        ON c2.attribute_id = 155
        AND c2.entity_id   = ebe.id
        WHERE ebe.class_id = 59
        )
      WHERE amount IS NULL
      AND type_id  != 2251
    );

  INSERT
  INTO lx_amount
    (SELECT lx.entity_id,
        lx.creditor_id,
        lx.cid,
        lx.pno,
        lx.pdate,
        NVL( c.amount,0 ) amount
      FROM lx_amount# lx
      LEFT OUTER JOIN credit@credits c
      ON (lx.cid   = c.creditor_id
      AND lx.pno   = c.primary_contract_no
      AND lx.pdate = c.primary_contract_date)
    );
END LX_AMOUNT_FILL;