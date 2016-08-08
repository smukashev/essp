create or replace PROCEDURE LX_ORGANIZATION_HEAD_FILL
AS
BEGIN

  delete from lx_organization_head;

  for prbl in (

      select be.id headId,
              midname.value midname,
              lastname.value lastname,
              firstname.value firstname,
              lang.value lang,
              set_values.report_date  report_date

      from eav_be_entities be

        left outer join eav_be_entity_complex_sets name_set
        on name_set.entity_id=be.id and name_set.attribute_id=3

        left outer join eav_be_complex_set_values set_values
        on set_values.set_id=name_set.id

        left outer join eav_be_string_values midname
        on midname.entity_id=set_values.entity_value_id and midname.attribute_id=33

        left outer join eav_be_string_values lastname
        on lastname.entity_id=set_values.entity_value_id and lastname.attribute_id=32


        left outer join eav_be_string_values firstname
        on firstname.entity_id=set_values.entity_value_id and firstname.attribute_id=30

        left outer join eav_be_string_values lang
        on lang.entity_id=set_values.entity_value_id and lang.attribute_id=31

      where

        be.class_id = 12

      group by be.id, midname.value , lastname.value, firstname.value, lang.value, set_values.report_date
      having count(1) > 1

  )
  loop

  insert into LX_ORGANIZATION_HEAD(HEAD_ID, MIDNAME, LASTNAME ,FIRSTNAME , LANG, REPORT_DATE)
  values(prbl.headId, prbl.midname, prbl.lastname, prbl.firstname, prbl.lang, prbl.report_date);

  end loop;


  --fill organization_info_id
  for orginfo in (
    select clx.entity_id orginfo_id, lx.head_id head_id
      from eav_be_complex_values clx
    join lx_organization_head lx
      on clx.entity_value_id = lx.head_id
      and attribute_id = 30
  )
  loop
    update lx_organization_head set
      orginfo_id=orginfo.orginfo_id
    where head_id = orginfo.head_id;
  end loop;


  --fill subject_id
  for subject in (
    select clx.entity_id subject_id, lx.orginfo_id orginfo_id
      from eav_be_complex_values clx
    join lx_organization_head lx
      on clx.entity_value_id = lx.orginfo_id
      and attribute_id = 36
  )
  loop
    update lx_organization_head set subject_id=subject.subject_id where orginfo_id = subject.orginfo_id;
  end loop;

END LX_ORGANIZATION_HEAD_FILL;