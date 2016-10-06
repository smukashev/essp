select tm.creditor_id, (select value from v_creditor_map where creditor_id = tm.creditor_id), count(1) from
  (select distinct t.creditor_id  from report@credits t where t.report_date = date '2016-09-01' and t.status_id in ( 92, 128)
   union all
   select distinct creditor_id from v_xml_file@credits where report_date = date '2016-09-01'
   union all
   select distinct (select creditor_id from v_creditor_map where id = r.creditor_id) creditor_id from eav_report r
     where r.report_date = date '2016-09-01') tm
group by creditor_id having count(1) != 3