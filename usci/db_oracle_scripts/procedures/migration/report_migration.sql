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
      eav_message_attachment_id number;
    begin

          begin
            select id
                into eav_report_id
            from EAV_REPORT
                where creditor_id = rp.creditor_id
                and report_date = rp.report_date;
            EXCEPTION
              WHEN no_data_found THEN
              if rp.creditor_id is not null then


                --insert new report
                insert into eav_report(creditor_id, total_count, actual_count, beg_date, end_date, report_date, status_id, username, last_manual_edit_date)
                values(rp.creditor_id, rp.total_count, rp.actual_count, rp.beg_date, rp.end_date, rp.report_date, rp.status_id, rp.username, rp.last_manual_edit_date)
                RETURNING id INTO eav_report_id;


                --find messages
                for messages in (
                    select * from report_message@credits
                    where report_id=rp.report_id
                    order by send_date
                )
                loop

                    declare
                      eavMessage varchar(1000 byte);
                    begin

                        select text
                          into eavMessage
                        from eav_report_message
                        where report_id=eav_report_id and
                          text=messages.text and
                          send_date=messages.send_date and
                          username=messages.username;

                      exception
                        when too_many_rows then
                          null;
                        when no_data_found then
                          insert into eav_report_message(report_id, username, send_date, text)
                          values(eav_report_id, messages.username, messages.send_date, messages.text)
                          RETURNING id INTO eav_report_message_id;

                          --find attachemtns
                          for attach in (
                              select id, report_message_id, filename from report_message_attachment@credits
                              where report_message_id=messages.id
                          )
                          loop
                              declare
                                  eavFilename varchar2(2000 byte);
                                begin

                                  select filename
                                    into eavFilename
                                  from eav_report_message_attachment
                                  where report_message_id=eav_report_message_id and
                                    filename=attach.filename;

                                exception
                                when too_many_rows then
                                  null;
                                when no_data_found then
                                    insert into eav_report_message_attachment(report_message_id, filename)
                                    values(eav_report_message_id, attach.filename)
                                    returning id into eav_message_attachment_id;

                                    update eav_report_message_attachment
                                    set CONTENT = (select content from report_message_attachment@credits where id=attach.id)
                                    where id=eav_message_attachment_id;
                                end;

                          end loop;

                      end;


                end loop;

              end if;
          end;

    end;
  end loop;


END REPORT_MIGRATION;