BEGIN
  for report in (
  select rp.creditor_id, rp.id id, rp.report_date report_date, cr.name creditor_name from eav_report rp
    left outer join r_ref_creditor@showcase cr
      on rp.creditor_id=ref_creditor_id
  where
    rp.STATUS_ID=3 and rp.report_date = date '2016-07-01'
    --and rp.CREDITOR_ID=3436242
  )
  loop
    update eav_report set status_id=5 where id=report.id;

    PKG_NOTIFICATION.SEND_APPROVAL_NOTIFICATION(report.creditor_id, report.creditor_name, 'Пользователь НБ', report.report_date);

  end loop;
END;