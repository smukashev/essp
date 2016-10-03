select creditor_id, count(1) from
(select distinct t.creditor_id  from report@credits t where t.report_date = date '2016-09-01' and t.status_id in ( 92, 128)
union all
select distinct creditor_id from v_xml_file@credits where report_date = date '2016-09-01') group by creditor_id having count(1) = 1