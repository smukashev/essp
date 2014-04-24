select xf.file_name,
       c.primary_contract_no,
       c.primary_contract_date,
       cd_rnn.no_ as rnn,
       cd_bin.no_ as bin,
       cd_bik.no_ as bik
  from core.xml_file@credits xf,
       core.xml_credit_id@credits xcid,
       core.credit@credits c,
       ref.creditor_doc@credits cd_rnn,
       ref.creditor_doc@credits cd_bin,
       ref.creditor_doc@credits cd_bik
 where xf.id = xcid.xml_file_id
   and xf.report_date = to_date('01.05.2013', 'dd.MM.yyyy') --  Report date
   and xcid.credit_id = c.id

   and cd_rnn.type_id = 11
   and c.creditor_id = cd_rnn.creditor_id
   and cd_bin.type_id = 7
   and c.creditor_id = cd_bin.creditor_id
   and cd_bik.type_id = 15
   and c.creditor_id = cd_bik.creditor_id
   and not exists (select ct.credit_id
                     from eav_be_temp_credit ct,
                          eav_be_temp_creditor cr
                    where ct.creditor_id = cr.creditor_id
                      and ct.primary_contract_no = c.primary_contract_no
                      and ct.primary_contract_date = c.primary_contract_date
                      and (cr.rnn = cd_rnn.no_ or cr.bin = cd_bin.no_ or cr.bik = cd_bik.no_))