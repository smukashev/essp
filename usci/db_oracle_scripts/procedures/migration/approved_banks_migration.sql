create or replace PROCEDURE "APPROVED_BANKS_MIGRATION" IS
   v_user_name varchar(255);
BEGIN

   for report in (
     select rp.id id,rp.report report_date, cr.name creditor_name from eav_report rp
      left outer join r_ref_creditor@showcase cr
      on rp.creditor_id=ref_creditor_id
      where
      rp.STATUS_ID=3 and rp.report_date = date '2016-09-01'
      --and rp.CREDITOR_ID=3436242
   )
   loop
      update eav_report set status_id=5 where id=report.id;

       for bank_user in (
         select u.screen_name username from eav_a_creditor_user cu
          join eav_a_user  u
          on cu.user_id = u.user_id
          where cu.creditor_id=bank.creditor_id
       )
       loop
          PKG_NOTIFICATION.SEND_APPROVAL_NOTIFICATION(report.creditor_id, report.creditor_name, bank_user.username, report.report_date);
       end loop;

   end loop;

END APPROVED_BANKS_MIGRATION;