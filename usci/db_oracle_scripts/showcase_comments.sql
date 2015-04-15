comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT1_ID is 'limit';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT2_ID is 'interest.pastdue';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT3_ID is 'debt.current';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT4_ID is 'debt.pastdue';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT5_ID is 'debt.write_off';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT6_ID is 'discount';
comment on COLUMN R_CORE_REMAINS.BALANCE_ACCOUNT7_ID is 'correction';

comment on COLUMN R_CORE_REMAINS.DEBT_CURRENT_VALUE is 'Остаток основного долга -  непросроченный основной долг';
comment on COLUMN R_CORE_REMAINS.DEBT_PAS_VALUE is 'Остаток основного долга -  просроченный основной долг';
comment on COLUMN R_CORE_REMAINS.DEBT_WRT_VALUE is 'Остаток основного долга -  списанная с баланса задолженность';
comment on COLUMN R_CORE_REMAINS.INTEREST_CURRENT_VALUE is 'Остаток  начисленного вознаграждения  - непросроченное вознаграждение';
comment on COLUMN R_CORE_REMAINS.INTEREST_PASTDUE_VALUE is 'Остаток  начисленного вознаграждения  - просроченное вознаграждение';
comment on COLUMN R_CORE_REMAINS.INTEREST_WRT_VALUE is 'Остаток  начисленного вознаграждения  - списанная с баланса задолженность';
comment on COLUMN R_CORE_REMAINS.DISCOUNTED_VALUE is 'Дисконт/премия';
comment on COLUMN R_CORE_REMAINS.CORR_VALUE is 'Положительная/отрицательная корректировка';
comment on COLUMN R_CORE_REMAINS.DEBT_DIS_VALUE is 'Дисконтированная (приведенная) стоимость будущих денежных потоков';
comment on COLUMN R_CORE_REMAINS.LIMIT_VALUE is 'Остаток лимита кредитной карты/овердрафта';

comment on COLUMN R_CORE_CREDIT_FLOW.KFN_VALUE is 'Фактически сформированная сумма провизий (резервов), по неоднородным кредитам по требованиям уполномоченного органа';
comment on COLUMN R_CORE_CREDIT.MSFO_VALUE is 'Фактически сформированная сумма провизий (резервов), по неоднородным кредитам по требованиям МСФО';
comment on COLUMN R_CORE_CREDIT.MSFO_O_B_VALUE is 'Фактически сформированная сумма провизий (резервов), по неоднородным кредитам по требованиям МСФО (по лимиту кредитной карты/овердрафту)';

comment on COLUMN R_CORE_DEBTOR_INFO.CREDITOR1_ID is 'organizaiton creditor';
comment on COLUMN R_CORE_DEBTOR_INFO.CREDITOR2_ID is 'person creditor';
comment on COLUMN R_CORE_DEBTOR_INFO.BANK_RELATION1_ID is 'organization bank_relation';
comment on COLUMN R_CORE_DEBTOR_INFO.BANK_RELATION2_ID is 'person bank_relation';

comment on COLUMN SHOWCASE.R_CORE_CREDIT.PORTFOLIO1_ID is 'portfolio_essp';
comment on COLUMN SHOWCASE.R_CORE_CREDIT.PORTFOLIO2_ID is 'portfolio';
comment on COLUMN SHOWCASE.R_CORE_CREDIT.PORTFOLIO_MSFO_ID  is 'portfolio_msfo';
