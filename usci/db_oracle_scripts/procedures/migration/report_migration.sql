create or replace PROCEDURE REPORT_MIGRATION
AS

BEGIN

  for rp in (
     select

       r.id report_id,
       r.total_count total_count,
       r.actual_count actual_count,
       r.beg_date beg_date,
       r.end_date end_date,
       r.username username,
       r.last_manual_edit_date last_manual_edit_date,

       map.REF_CREDITOR_ID creditor_id,
       r.report_date report_date,
       gl.id status_id

     from report@credits r

      left outer join v_creditor_map map
      on map.CREDITOR_ID = r.creditor_id

      left outer join eav_global gl
      on gl.value = to_char(r.status_id)

  )
  loop
    declare
      eav_report_id number;
      eav_report_message_id number;
      message_attachment_id number;
    begin

          begin
            select id
                into eav_report_id
            from EAV_REPORT
                where CREDITOR_ID = rp.creditor_id
                and report_date = rp.report_date;
            EXCEPTION
              WHEN no_data_found THEN
          end;


          --insert report
          if eav_report_id is null then
          null;
              insert into eav_report(creditor_id, total_count, actual_count, beg_date, end_date, report_date, status_id, username, last_manual_edit_date)
              values(rp.report_id, rp.total_count, rp.actual_count, rp.beg_date, rp.end_date, rp.report_date, rp.status_id, rp.username, rp.last_manual_edit_date)
              RETURNING id INTO eav_report_id;
          end if;

          --fill messages
          for messages in (
              select * from report_message@credits
              where report_id=rp.report_id
              order by send_date
          )
          loop

              insert into eav_report_message(report_id, username, send_date, text)
              values(eav_report_id, messages.username, messages.send_date, messages.text)
              RETURNING id INTO eav_report_message_id;


              --fill attachments
              for attach in (
                  select id, report_message_id, filename from report_message_attachment@credits
                  where report_message_id=messages.id
              )
              loop
                  insert into eav_report_message_attachment(report_message_id, filename)
                  values(eav_report_message_id, attach.filename)
                  returning id into message_attachment_id;

                  update eav_report_message_attachment
                  set CONTENT = (select content from report_message_attachment@credits where id=attach.id)
                  where id=message_attachment_id;

              end loop;

          end loop;
    end;
  end loop;


END REPORT_MIGRATION;