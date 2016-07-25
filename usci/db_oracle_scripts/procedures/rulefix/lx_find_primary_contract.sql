CREATE OR REPLACE PROCEDURE LX_FIND_PRIMARY_CONTRACT (
  p_entity_id in number,
  p_no out varchar,
  p_date out date,
  p_creditor_id out number,
  p_cid out number,
  p_error out VARCHAR
)
AS
  v_primary_contract_id number;
  BEGIN
    p_error := null;

    select regexp_substr(key_string, '\d+',1,1), regexp_substr(key_string, '\d+',1,2) into
           v_primary_contract_id,p_creditor_id
      from eav_optimizer
     where entity_id = p_entity_id;


    select value into p_no from eav_be_string_values
      where entity_id = v_primary_contract_id;

    select value into p_date from eav_be_date_values
       where entity_id = v_primary_contract_id;

    select creditor_id into p_cid
     from v_creditor_map
    where ref_creditor_id = p_creditor_id;

    EXCEPTION
      when OTHERS THEN
        p_error := 'entity_id = ' || p_entity_id || ', ' || SQLERRM;



  END LX_FIND_PRIMARY_CONTRACT;

