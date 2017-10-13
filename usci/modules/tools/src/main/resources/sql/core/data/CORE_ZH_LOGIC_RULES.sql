INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (5, 'function boolean isExclusiveIIN(String iin) {
    try{
        //  поиск исключительного иин
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("06"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}

function boolean isExclusiveRNN(String iin) {
    try{
        //  поиск исключительного рнн
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("11"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}
', 'функция исключительный документ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (6, '
function String isBVUNODoc(List docs, IBaseEntityProcessorDao processorDao,
                           IMetaClassRepository metaClassRepository,
                           Map creditorCache){
    Set<Long> uniqueCreditorIds = new HashSet<Long>();

    for(Object docObject: docs) {
        BaseEntity doc = (BaseEntity) docObject;
        if(creditorCache.size() < 1) {
            List<BaseEntity> creditorsDbList = processorDao.getEntityByMetaClass(
                    metaClassRepository.getMetaClass("ref_creditor"));

            for (BaseEntity creditor : creditorsDbList) {
                BaseSet creditorDocs = (BaseSet) ((BaseValue) creditor.getBaseValue("docs")).getValue();
                for (IBaseValue creditorDocValue : creditorDocs.get()) {
                    BaseEntity creditorDoc = (BaseEntity) creditorDocValue.getValue();
                    String docKey = creditorDoc.getEl("no") + " | " + creditorDoc.getEl("doc_type.code");
                    creditorCache.put(docKey, creditor);
                }
            }
        }

        if(creditorCache.size() < 1)
            throw new RuntimeException("Справочник кредиторов пуст.");


        String docKey = doc.getEl("no") + " | " + doc.getEl("doc_type.code");
        if(!creditorCache.containsKey(docKey))
            return "Не существующий документ из справочника кредиторов " + docKey;

        uniqueCreditorIds.add(((BaseEntity) creditorCache.get(docKey)).getId());
    }

    if(uniqueCreditorIds.size() > 1) {
        return "Документы из разных кредиторов";
    }

    return "";
}', 'работа со справочником кредиторов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (7, '
function List getInvalidIINs(List iins){
    List ret = new ArrayList();
    for(Object iin : iins) {
        if(!iinCorrect((String) iin ) && !isExclusiveIIN((String) iin))
            ret.add(iin);
    }
    return ret;
}

function  boolean iinCorrect(String iin){

    int[] weights = new int[]{1,2,3,4,5,6,7,8,9,10,11,1,2};

    int sum = 0;
    if(iin.length() != 12)
        return false;

    if(!isDateValid(iin.substring(0,6),"yyMMdd"))
        return false;

    if(iin.charAt(6) < ''0'' || iin.charAt(6) > ''6'')
        return false;

    for(int i=0;i<11;i++)
        sum += (iin.charAt(i) - ''0'' ) * weights[i];
    sum %= 11;
    int last = iin.charAt(11) - ''0'';
    if(sum ==  10) {
        sum = 0;
        for(int i=0;i<11;i++)
            sum+=(iin.charAt(i) - ''0'') * weights[i+2];
        sum %= 11;
    }
    return sum == last;
}', 'функций для проверки иин', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (8, 'rule "Неправильный ИИН у субъекта предпринимательства ИП"
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                && getEls("{setString(02,05,08)}subject.organization_info.enterprise_type.code") > 0
                && $r : getInvalidIINs((List) getEls("{get}subject.docs[doc_type.code=07]no"))
                && $r.size() > 0
)
then
 $entity.addValidationError("Неправильный ИИН у субъекта предпринимательства ИП " + $r);
end', 'Неправильный ИИН у субъекта предпринимательства ИП', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (9, 'rule "иин физ лицо"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r: getInvalidIINs((List)getEls("{get}subject[is_person=true]docs[doc_type.code=06]no"))
			&& $r.size() > 0 )
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин физ лицо', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (10, 'rule "иин юр. лицо"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}subject[is_organization=true]docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин юр. лицо', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (11, 'rule "иин кредитора"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}creditor.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин кредитора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (12, 'rule "иин филиал банка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}creditor_branch.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин филиал банка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (13, 'rule "иин головной банк"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}creditor_branch.main_office.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин головной банк', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (39, 'rule "документ  головной банк должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  головной банк должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (40, 'rule "документ  кредитора физ лица с особыми отношениями должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (41, 'rule "документ  руководитель организации должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  руководитель организации должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (42, 'rule "документ  кредитора юр лица с особыми отношениями должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (43, 'rule "документ  кредитор портфель должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (44, 'rule "документ  кредитор портфель МСФО должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель МСФО должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (45, 'rule "документ  физ лица должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  физ лица должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (46, 'rule "документ  юр. лица должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  юр. лица должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (47, 'rule "документ  кредитора должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (48, 'rule "документ  филиал банка должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  филиал банка должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (49, 'rule "документ  головной банк должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  головной банк должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (50, 'rule "документ  кредитора физ лица с особыми отношениями должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (14, 'rule "иин кредтиора физ лица с особыми отношениями"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}subject[is_person=true]person_info.bank_relations.creditor.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин кредтиора физ лица с особыми отношениями', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (15, 'rule "иин руководитель организации"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}subject[is_organization=true]organization_info.head.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин руководитель организации', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (16, 'rule "иин кредтиора юр лица с особыми отношениями"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}subject[is_organization=true]organization_info.bank_relations.creditor.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин кредтиора юр лица с особыми отношениями', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (17, 'rule "иин кредитор портфель"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин кредитор портфель', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (18, 'rule "иин кредитор портфель МСФО"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин кредитор портфель МСФО', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (19, 'rule "иин субъект кредитора"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ( $r : getInvalidIINs((List)getEls("{get}subject[is_creditor=true]docs[doc_type.code=06]no")) && $r.size() > 0))
then
	$entity.addValidationError("обнаружен некорректный иин: " + $r);
end', 'иин субъект кредитора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (20, '
function List getInvalidRNNs(List rnns, Boolean isPerson) {
    List ret = new ArrayList();
    for(Object rnn : rnns) {
        try {
            rnnCorrect((String) rnn, isPerson);
        } catch(Exception e) {
            if(!isExclusiveRNN((String)rnn))
                ret.add(rnn + ": " + e.getMessage());
        }
    }
    return ret;
}

function void rnnCorrect(String rnn, Boolean isPerson) {
    int [][] WEIGHTS = new int[][]{
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1},
            {2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2},
            {3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3},
            {4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4},
            {5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5},
            {6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6},
            {7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7},
            {8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8},
            {9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
        };

    Set<String> rnnRegions = new HashSet<String> (Arrays.asList("03", "06", "09", "15", "18", "21", "24", "27", "30", "33", "36", "39", "43", "45", "48", "51", "53", "55", "58", "60", "62", "63", "67"));

    if(rnn.length() != 12)
        throw new RuntimeException("Должен содержать 12 символов");

    if(rnn.charAt(4) == ''0'' && isPerson)
       throw new RuntimeException("Пятый символ РНН физ. лица должен принимать значения от 1 до 9");

    if (rnn.charAt(4) != ''0'' && !isPerson)
       throw new RuntimeException("Пятый символ РНН юр. лица должен принимать значение 0");

    if(!rnnRegions.contains(rnn.substring(0,2)))
       throw new RuntimeException("Неправильный код области СОАТО по РНН субъекта");

    char c = rnn.charAt(0);
    for(int i=1;i<12;i++)
        if(rnn.charAt(i) != rnn.charAt(0))
           c = rnn.charAt(i);

    if(c == rnn.charAt(0))
        throw new RuntimeException("Значение всех разрядов РНН не должно быть одинаковым");

    int sum = 10;
    for(int i=0;i<WEIGHTS.length;i++) {
       int[] w = WEIGHTS[i];
       sum = 0;

       for(int j=0;j<w.length;j++)
         sum+= (rnn.charAt(j) - ''0'' ) * w[j];

       sum %= 11;

       if(sum != 10)
         break;
    }

    if(sum != rnn.charAt(11) - ''0'')
       throw new RuntimeException("Не совпадает контрольный символ");
}', 'функций для проверки рнн', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (21, 'rule "документ физ лица РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ($r : getInvalidRNNs((List)getEls("{get}subject[is_person=true]docs[doc_type.code=11]no"), true) && $r.size() > 0))
then
	$entity.addValidationError("" + $r);
end', 'документ физ лица РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (22, 'rule "документ юр лица РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ($r : getInvalidRNNs((List)getEls("{get}subject[is_organization=true][organization_info.is_se=false]docs[doc_type.code=11]no"), false) && $r.size() > 0))
then
	$entity.addValidationError("" + $r);
end', 'документ юр лица РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (23, 'rule "документ юр лица РНН ИП"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ($r : getInvalidRNNs((List)getEls("{get}subject[is_organization=true][organization_info.is_se=true]docs[doc_type.code=11]no"), true) && $r.size() > 0))
then
	$entity.addValidationError("" + $r);
end', 'документ юр лица РНН ИП', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (24, 'rule "документ рук юр лица РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ($r : getInvalidRNNs((List)getEls("{get}subject.organization_info.head.docs[doc_type.code=11]no"), true) && $r.size() > 0))
then
	$entity.addValidationError("" + $r);
end', 'документ рук юр лица РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (25, 'rule "документ  физ лица должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  физ лица должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (26, 'rule "документ  юр. лица должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  юр. лица должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (27, 'rule "документ  кредитора должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (28, 'rule "документ  филиал банка должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  филиал банка должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (29, 'rule "документ  головной банк должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  головной банк должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (30, 'rule "документ  кредитора физ лица с особыми отношениями должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (31, 'rule "документ  руководитель организации должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  руководитель организации должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (32, 'rule "документ  кредитора юр лица с особыми отношениями должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (33, 'rule "документ  кредитор портфель должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (34, 'rule "документ  кредитор портфель МСФО должен содержать 12 символов ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель МСФО должен содержать 12 символов ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (35, 'rule "документ  физ лица должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  физ лица должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (36, 'rule "документ  юр. лица должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  юр. лица должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (37, 'rule "документ  кредитора должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (38, 'rule "документ  филиал банка должен содержать 12 символов БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН должен содержать 12 цифр: " + $r);
end', 'документ  филиал банка должен содержать 12 символов БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (1, 'import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;

global kz.bsbnb.usci.eav.repository.IMetaClassRepository metaClassRepository;
global kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao baseEntityProcessorDao;
global java.util.Map creditorCache;

', 'Нужные импорты для работы функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (2, '
function boolean isDateValid(String date,String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
}

function boolean isDigit(char c){
    return ''0'' <= c && c <= ''9'';
}

function int getFirstDay(Date date){
    java.util.Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
}

function List getNot12DigitStrings(List docs){
    List ret = new ArrayList();
        for(Object doc : docs) {
            if(!((String) doc ).matches("\\d{12}"))
                ret.add(doc);
        }
    return ret;
}

function List get12ZeroStrings(List docs){
    List ret = new ArrayList();
        for(Object doc: docs) {
            if(((String) doc ).equals("000000000000"))
                ret.add(doc);
        }
    return ret;
}

function List getNegativesFromDoubles(List values){
    List ret = new ArrayList();
        for(Object value : values) {
            if( (Double) value < 0)
               ret.add(value);
        }
    return ret;
}

function String getStringDateV(Date d){

   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

   return sdf.format(d);

}

', 'Общие функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (3, '
function boolean hasBACT(String baNo, String creditCode, BaseEntity entity){
    try {
        // fixme!
        if (baNo == null || creditCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;

        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_ct"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity creditType = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        creditType.put("code", new BaseValue(reportDate, creditCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("credit_type",  new BaseValue(reportDate, creditType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}
', 'функция сверки балансовый счет - тип кредита', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (4, 'function boolean hasBADRT(String baNo, String drtCode, BaseEntity entity){
    try{
        // fixme!
        if (baNo == null || drtCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_drt"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity debtRemainsType = new BaseEntity(metaClassRepository.getMetaClass("ref_debt_remains_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        debtRemainsType.put("code", new BaseValue(reportDate, drtCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("debt_remains_type",  new BaseValue(reportDate, debtRemainsType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}

', 'функция сверки балансовый счет - тип остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (88, '
function boolean isCurrencyConvertible(IBaseEntityProcessorDao baseEntityProcessorDao, IMetaClassRepository metaClassRepository, String currencyCode, Date reportDate){
    try{
        //Инициализавать сущность, тип <валюта> - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_currency"), reportDate);

        //заполнять параметры поиска
        be.put("short_name",new BaseValue(currencyCode));

        //находить все иденификаторы сущности (проставлять идшники)
        IBaseEntity preparedBe  = baseEntityProcessorDao.prepare(be, be.getBaseEntityReportDate().getCreditorId());

        //подгружать остальные поля сущности
        IBaseEntity loadedBe = baseEntityProcessorDao.getBaseEntityLoadDao().loadByMaxReportDate(preparedBe.getId(), preparedBe.getReportDate());

        //возратить признак
        return loadedBe.getEl("is_convertible").equals(true);

    } catch(Exception e){
        return false;
    }
}

function boolean isBA5thSymbolCorrect(String ba, BaseEntity entity){
    if(ba.length() < 5) return true;
    if(ba.length() == 7 && ba.endsWith("000")) return true;

    List subjects = (List) entity.getEls("{get}subject.organization_info");
    Boolean isResident = null;

    if(subjects.size() > 0) {
        BaseEntity subject = (BaseEntity) subjects.get(0);
        isResident = (Integer)subject.getEls("{count}country[code_numeric=398]") > 0;
    }

    subjects = (List) entity.getEls("{get}subject.person_info");

    if(subjects.size() > 0) {
        BaseEntity subject = (BaseEntity) subjects.get(0);
        isResident = (Integer)subject.getEls("{count}country[code_numeric=398]") > 0;
    }

    if(isResident == null)
       return true;
    if(isResident && ba.charAt(4) == ''1'')
        return true;
    if(!isResident && ba.charAt(4) == ''2'')
        return true;

    return false;
}

function boolean isBA7thSymbolCorrect(IBaseEntityProcessorDao baseEntityProcessorDao, IMetaClassRepository metaClassRepository, String ba,String currencyCode, Date reportDate){
    if(ba.length() < 7) return true;
    if(ba.length() == 7 && ba.endsWith("000")) return true;

    if(currencyCode.equals("KZT"))
       return ba.charAt(6) == ''1'';

    boolean isConvertible = isCurrencyConvertible(baseEntityProcessorDao, metaClassRepository, currencyCode, reportDate);

    if(isConvertible)
        return ba.charAt(6) == ''2'';

    return ba.charAt(6) == ''3'';

}
', 'функций проверки балансовых счетов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (89, 'rule "проверка 7 символа  бс основного долга текущего остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.current.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс основного долга текущего остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (90, 'rule "проверка 7 символа  бс основного долга просроченного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.pastdue.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс основного долга просроченного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (91, 'rule "проверка 7 символа  бс основного долга списанного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.write_off.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс основного долга списанного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (92, 'rule "проверка 7 символа  бс вознаграждения текущего остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.current.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс вознаграждения текущего остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (93, 'rule "проверка 7 символа  бс вознаграждения просроченного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.pastdue.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс вознаграждения просроченного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (94, 'rule "проверка 7 символа  бс провизий"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_kfn.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс провизий', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (95, 'rule "проверка 7 символа  бс провизий МСФО"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс провизий МСФО', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (96, 'rule "проверка 7 символа  бс провизий МСФО над балансом"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс провизий МСФО над балансом', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (97, 'rule "проверка 7 символа  бс корректировки остатка "
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.correction.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс корректировки остатка ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (98, 'rule "проверка 7 символа  бс лимита"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.limit.balance_account.no_")
			&& $r != null
			&& $c : getEl("currency.short_name")
			&& $c != null
			&& !isBA7thSymbolCorrect(baseEntityProcessorDao, metaClassRepository, (String)$r, (String)$c, getReportDate()))
then
	$entity.addValidationError("7й символ балансового счета  неккоректный: " + $r);
end', 'проверка 7 символа  бс лимита', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (99, 'rule "проверка соответсвия бс основного долга текущего остатка по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.current.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс основного долга текущего остатка по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (100, 'rule "проверка соответсвия бс основного долга просроченного остатка по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.pastdue.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс основного долга просроченного остатка по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (101, 'rule "проверка соответсвия бс основного долга списанного остатка по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.write_off.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс основного долга списанного остатка по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (102, 'rule "проверка соответсвия бс вознаграждения текущего остатка по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.current.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс вознаграждения текущего остатка по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (103, 'rule "проверка соответсвия бс вознаграждения просроченного остатка по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.pastdue.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс вознаграждения просроченного остатка по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (104, 'rule "проверка соответсвия бс провизий по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_kfn.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс провизий по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (105, 'rule "проверка соответсвия бс провизий МСФО по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс провизий МСФО по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (106, 'rule "проверка соответсвия бс провизий МСФО над балансом по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс провизий МСФО над балансом по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (134, 'rule "remains_interest_current_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.current.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте остатка начисленного вознаграждения договора");
end', 'сумма в валюте остатка начисленного вознаграждения договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (135, 'rule "remains_limit_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.limit.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма остатка лимита договора");
end', 'сумма остатка лимита договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (136, 'rule "remains_limit_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.limit.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте остатка лимита договора");
end', 'сумма в валюте остатка лимита договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (137, 'rule "remains_dept_write_off_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.debt.write_off.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в остатке долга списанной задолженности");
end', 'сумма в остатке долга списанной задолженности', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (138, 'rule "remains_dept_write_off_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.debt.write_off.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте остатка долга списанной задолженности");
end', 'сумма в валюте остатка долга списанной задолженности', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (139, 'rule "remains_interest_write_off_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.write_off.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в остатке начисленного вознаграждения договора");
end', 'сумма в остатке начисленного вознаграждения договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (140, 'rule "remains_interest_write_off_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.write_off.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте остатка начисленного вознаграждения договора");
end', 'сумма в валюте остатка начисленного вознаграждения договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (141, 'rule "remains_discounted_value_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.discounted_value.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма дисконтированной стоимости договора");
end', 'сумма дисконтированной стоимости договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (142, 'rule "remains_interest_pastdue_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.pastdue.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в просроченной задолженности по начисленному вознаграждению договора");
end', 'сумма в просроченной задолженности по начисленному вознаграждению договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (143, 'rule "remains_interest_pastdue_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.pastdue.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте в просроченной задолженности по начисленному вознаграждению договора");
end', 'сумма в валюте в просроченной задолженности по начисленному вознаграждению договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (144, 'rule "remains_debt_pastdue_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.debt.pastdue.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в просроченной задолженности по основному долгу договора");
end', 'сумма в просроченной задолженности по основному долгу договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (145, 'rule "remains_debt_pastdue_value_currency"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.debt.pastdue.value_currency"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в валюте в просроченной задолженности по основному долгу договора");
end', 'сумма в валюте впросроченной задолженности по основному долгу договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (107, 'rule "проверка соответсвия бс корректировки остатка  по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.correction.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс корректировки остатка  по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (108, 'rule "проверка соответсвия бс дисконта по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.discount.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс дисконта по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (109, 'rule "проверка соответсвия бс лимита по кредиту"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.limit.balance_account.no_")
			&& $r != null
			&& !hasBACT((String)$r, (String)getEl("credit_type.code"), $entity))
then
	$entity.addValidationError("балансовый счет не соответствует кредиту: " + $r);
end', 'проверка соответсвия бс лимита по кредиту', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (110, 'rule "проверка соответсвия бс основного долга текущего остатка и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.current.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "1", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс основного долга текущего остатка и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (111, 'rule "проверка соответсвия бс основного долга просроченного остатка и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.pastdue.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "2", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс основного долга просроченного остатка и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (112, 'rule "проверка соответсвия бс основного долга списанного остатка и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.write_off.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "3", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс основного долга списанного остатка и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (113, 'rule "проверка соответсвия бс вознаграждения текущего остатка и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.current.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "4", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс вознаграждения текущего остатка и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (114, 'rule "проверка соответсвия бс вознаграждения просроченного остатка и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.pastdue.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "5", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс вознаграждения просроченного остатка и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (115, 'rule "проверка соответсвия бс провизий и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_kfn.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "11", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс провизий и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (116, 'rule "проверка соответсвия бс провизий МСФО и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "12", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс провизий МСФО и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (117, 'rule "проверка соответсвия бс провизий МСФО над балансом и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "13", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс провизий МСФО над балансом и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (118, 'rule "проверка соответсвия бс корректировки остатка  и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.correction.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "8", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс корректировки остатка  и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (119, 'rule "проверка соответсвия бс дисконта и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.discount.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "7", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс дисконта и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (120, 'rule "проверка соответсвия бс лимита и типа остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.limit.balance_account.no_")
			&& $r != null
			&& !hasBADRT((String)$r, "10", $entity))
then
	$entity.addValidationError("балансовый счет не соответствует типу остатка: " + $r);
end', 'проверка соответсвия бс лимита и типа остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (121, 'rule "rd_first_day"
//MES401
when
  $entity: BaseEntity(getFirstDay($entity.getReportDate()) !=1)
then
  $entity.addValidationError("Неверная отчетная дата");
end', 'rd_first_day', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (122, 'rule "проверка кредитора"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getBaseEntityReportDate() != null
       && getEl("creditor") != null
       && getBaseEntityReportDate().getCreditorId() != ((BaseEntity) getEl("creditor")).getId()
    )
then
   $entity.addValidationError("Неправильно установлен кредитор");
end', 'проверка кредитора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (123, 'rule "не заполнен язык руководителя"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{count}subject.organization_info.head.names[lang=null]") > 0
    )
then
   $entity.addValidationError("Не заполнен язык при наименовании руководителя");
end', 'не заполнен язык руководителя', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (124, 'rule "не заполнен язык из списка для руководителя"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{setString(EN,KZ,RU)}subject.organization_info.head.names.lang") !=
          getEls("{count}subject.organization_info.head.names.lang")
    )
then
   $entity.addValidationError("Не заполнен язык из списка EN, KZ, RU для руководителя");
end', 'не заполнен язык из списка для руководителя', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (125, 'rule "дупликат языка руководителя"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{hasDuplicates(subject.organization_info.head.names)}lang") == true
    )
then
   $entity.addValidationError("дупликат языка руководителя");
end', 'дупликат языка руководителя', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (126, 'rule "не заполнен язык наименования для организации"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{count}subject.organization_info.names[lang=null]") > 0
    )
then
   $entity.addValidationError("Не заполнен язык наименования для организации");
end', 'не заполнен язык наименования для организации', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (127, 'rule "не заполнен язык из списка для организации"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{setString(EN,KZ,RU)}subject.organization_info.names.lang") !=
          getEls("{count}subject.organization_info.names.lang")
    )
then
   $entity.addValidationError("Не заполнен язык из списка EN, KZ, RU для организации");
end', 'не заполнен язык из списка для организации', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (128, 'rule "дупликат языка наименования для организации"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{hasDuplicates(subject.organization_info.names)}lang") == true
    )
then
   $entity.addValidationError("дупликат языка наименования для организации");
end', 'дупликат языка наименования для организации', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (129, 'rule "не заполнен язык наименования для физ лица"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{count}subject.person_info.names[lang=null]") > 0
    )
then
   $entity.addValidationError("Не заполнен язык наименования для физ лица");
end', 'не заполнен язык наименования для физ лица', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (130, 'rule "не заполнен язык из списка для физ лица"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{setString(EN,KZ,RU)}subject.person_info.names.lang") !=
          getEls("{count}subject.person_info.names.lang")
    )
then
   $entity.addValidationError("Не заполнен язык из списка EN, KZ, RU для физ лица");
end', 'не заполнен язык из списка для физ лица', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (131, 'rule "дупликат языка наименования для физ лица"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{hasDuplicates(subject.person_info.names)}lang") == true
    )
then
   $entity.addValidationError("дупликат языка наименования для физ лица");
end', 'дупликат языка наименования для физ лица', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (132, 'rule "remains_pledges_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ( $r : getNegativesFromDoubles((List)getEls("{get}pledges.value")) && $r.size() > 0))
then
  $entity.addValidationError("Не правильная сумма в обеспечение договора");
end', 'сумма в обеспечение договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (133, 'rule "remains_interest_current_value"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && ($r : getEl("change.remains.interest.current.value"))
                && $r != null
                && $r < 0)
then
  $entity.addValidationError("Не правильная сумма в остатке начисленного вознаграждения договора");
end', 'сумма в остатке начисленного вознаграждения договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (51, 'rule "документ  руководитель организации должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  руководитель организации должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (52, 'rule "документ  кредитора юр лица с особыми отношениями должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (53, 'rule "документ  кредитор портфель должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (54, 'rule "документ  кредитор портфель МСФО должен содержать 12 символов РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!matches("\\d{12}")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН должен содержать 12 цифр: " + $r);
end', 'документ  кредитор портфель МСФО должен содержать 12 символов РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (55, 'rule "документ  физ лица не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  физ лица не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (56, 'rule "документ  юр. лица не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  юр. лица не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (57, 'rule "документ  кредитора не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (58, 'rule "документ  филиал банка не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  филиал банка не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (59, 'rule "документ  головной банк не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  головной банк не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (60, 'rule "документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (61, 'rule "документ  руководитель организации не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  руководитель организации не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (62, 'rule "документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (63, 'rule "документ  кредитор портфель не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (64, 'rule "документ  кредитор портфель МСФО не должен содержать 12 нулей ИИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=06]no")
then
	$entity.addValidationError("ИИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель МСФО не должен содержать 12 нулей ИИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (65, 'rule "документ  физ лица не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  физ лица не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (66, 'rule "документ  юр. лица не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  юр. лица не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (67, 'rule "документ  кредитора не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (68, 'rule "документ  филиал банка не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  филиал банка не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (69, 'rule "документ  головной банк не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  головной банк не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (70, 'rule "документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (71, 'rule "документ  руководитель организации не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  руководитель организации не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (72, 'rule "документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (73, 'rule "документ  кредитор портфель не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (74, 'rule "документ  кредитор портфель МСФО не должен содержать 12 нулей БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=07]no")
then
	$entity.addValidationError("БИН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель МСФО не должен содержать 12 нулей БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (75, 'rule "документ  физ лица не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  физ лица не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (76, 'rule "документ  юр. лица не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  юр. лица не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (77, 'rule "документ  кредитора не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (78, 'rule "документ  филиал банка не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  филиал банка не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (79, 'rule "документ  головной банк не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}creditor_branch.main_office.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  головной банк не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (80, 'rule "документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.person_info.bank_relations.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора физ лица с особыми отношениями не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (81, 'rule "документ  руководитель организации не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.head.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  руководитель организации не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (82, 'rule "документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}subject.organization_info.bank_relations.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитора юр лица с особыми отношениями не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (83, 'rule "документ  кредитор портфель не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (84, 'rule "документ  кредитор портфель МСФО не должен содержать 12 нулей РНН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(equals("000000000000")) from (List) $entity.getEls("{get}portfolio.portfolio_msfo.creditor.docs[doc_type.code=11]no")
then
	$entity.addValidationError("РНН не должен содержать 12 нулей: " + $r);
end', 'документ  кредитор портфель МСФО не должен содержать 12 нулей РНН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (85, '
function boolean isAinCorrect(String ain, boolean isPerson){
    if(ain.length() != 12)
        return false;
    if(!ain.substring(0,2).matches("[A-Z]{2}"))
        return false;
    if(!ain.substring(2,4).matches("[\\p{javaUpperCase}]{2}"))
        return false;
    if(isPerson) {
        if(!isDateValid(ain.substring(4,10), "yyMMdd"))
            return false;
        if(!ain.substring(10,12).matches("\\d{2}"))
            return false;
    } else {
        if(ain.charAt(4) != ''0'')
            return false;
        if(!isDateValid(ain.substring(5,11), "yyMMdd"))
            return false;
        if(!isDigit(ain.charAt(11)))
            return false;
    }

    return true;
}
', 'Функций провекри аин', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (86, 'rule "аин физ лицо"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
    $r: String(!isAinCorrect($r, true)) from (List) $entity.getEls("{get}subject[is_person=true]docs[doc_type.code=17]no")
then
	$entity.addValidationError("Некорректный аин: " + $r);
end', 'аин физ лицо', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (87, 'rule "аин юр. лицо"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit")
	$r: String(!isAinCorrect($r, false)) from (List) $entity.getEls("{get}subject[is_organization=true]docs[doc_type.code=17]no")
then
	$entity.addValidationError("Некорректный аин: " + $r);
end', 'аин юр. лицо', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (163, 'rule "gen_remains_interest_pastdue"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.interest.pastdue.value") != null
                && getEl("change.remains.interest.pastdue.value") > 0
                && getEl("change.remains.interest.pastdue.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток начисленного вознаграждения в валюте договора: просроченного");
end', 'Остаток - Вознаграждение - Просроченная  задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (164, 'rule "gen_remains_interest_writeoff"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.interest.write_off.value") != null
                && getEl("change.remains.interest.write_off.value") > 0
                && getEl("change.remains.interest.write_off.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток начисленного вознаграждения в валюте договора: списанного с баланса");
end', 'Остаток - Вознаграждение - Списанная с баланса задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (165, 'rule "gen_remains_correction"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.correction.value") != null
                && getEl("change.remains.correction.value") > 0
                && getEl("change.remains.correction.value_currency") == null )
then
  $entity.addValidationError("Не заполнена положительная (отрицательная) корректировка в валюте договора");
end', 'Остаток - Положительная/отрицательная корректировка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (166, 'rule "gen_remains_discount"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.discount.value") != null
                && getEl("change.remains.discount.value") > 0
                && getEl("change.remains.discount.value_currency") == null )
then
  $entity.addValidationError("Не заполнен дисконт (премия) в валюте договора");
end', 'Остаток - Дисконт/премия', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (167, 'rule "head juridical"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(12,17,18,19,24)}credit_type.code") == 0
            && getEls("{count}subject[is_organization=true]organization_info[is_se!=true][head=null]") > 0)
then
  $entity.addValidationError("Не заполнен руководитель юридического лица");
end', 'Руководитель юридического лица', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (168, 'rule "amount"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEl("credit_type.code") != "12"
            && getEl("amount") == null)
then
  $entity.addValidationError("Не указана сумма займа (условного обязательства) в валюте договора");
end', 'cумма займа', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (169, 'rule "credit subject"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(17,18)}credit_type.code") == 0
            && getEls("{count}subject") == 0)
then
  $entity.addValidationError("Отсутствует информация о субъекте кредитной истории");
end', 'Субъект кредитной истории', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (170, 'rule "currency by credit"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(15,25)}credit_type.code") == 0
            && getEl("currency.short_name") == null)
then
  $entity.addValidationError("Не указан вид валюты по договору");
end', 'Вид валюты по договору', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (171, 'rule "legal_form in jur"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(17,18,19,24)}credit_type.code") == 0
            && getEls("{count}subject[is_organization=true]organization_info[is_se!=true][country.code_numeric=398][legal_form=null]") > 0)
then
  $entity.addValidationError("Не заполнена организационно-правовая форма");
end', 'Организационно-правовая форма', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (172, 'rule "contract_maturity_date"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(02,10,12,13,14,15,25,26)}credit_type.code") == 0
            && getEl("contract_maturity_date") == null)
then
  $entity.addValidationError("Не заполнена дата погашения по условиям договора");
end', 'Дата погашения по условиям договора', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (173, 'rule "finance source"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(02,03,10,12,13,14,15,17,18,24,25,26)}credit_type.code") == 0
            && getEl("finance_source.code") == null)
then
  $entity.addValidationError("Не заполнен источник финансирования банка (организации), выдавшей заем");
end', 'Источник финансирования банка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (174, 'rule "credit purpose"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(02,03,10,12,13,14,15,16,17,18,24,25,26)}credit_type.code") == 0
            && getEl("credit_purpose.code") == null)
then
  $entity.addValidationError("Не заполнена цель кредитования");
end', 'Цель кредитования', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (175, 'rule "credit object"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{setString(02,03,10,12,13,14,15,16,17,18,24,25,26)}credit_type.code") == 0
            && getEl("credit_object.code") == null)
then
  $entity.addValidationError("Не заполнен объект кредитования");
end', 'Объект кредитования', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (176, 'rule "econ trade KZ"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
            && getEls("{count}subject[is_organization=true]organization_info[country.code_numeric=398][econ_trade.code=null]") > 0)
then
  $entity.addValidationError("Не указан вид экономической деятельности");
end', 'Вид экономической деятельности для КЗ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (177, '
function boolean isCurrencyConvertible(IBaseEntityProcessorDao baseEntityProcessorDao, IMetaClassRepository metaClassRepository, String currencyCode, Date reportDate){
    try{
        //Инициализавать сущность, тип <валюта> - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_currency"), reportDate);

        //заполнять параметры поиска
        be.put("short_name",new BaseValue(currencyCode));

        //находить все иденификаторы сущности (проставлять идшники)
        IBaseEntity preparedBe  = baseEntityProcessorDao.prepare(be, be.getBaseEntityReportDate().getCreditorId());

        //подгружать остальные поля сущности
        IBaseEntity loadedBe = baseEntityProcessorDao.getBaseEntityLoadDao().loadByMaxReportDate(preparedBe.getId(), preparedBe.getReportDate());

        //возратить признак
        return loadedBe.getEl("is_convertible").equals(true);

    } catch(Exception e){
        return false;
    }
}

function boolean isBA5thSymbolCorrect(String ba, BaseEntity entity){
    if(ba.length() < 5) return true;
    if(ba.length() == 7 && ba.endsWith("000")) return true;

    List subjects = (List) entity.getEls("{get}subject.organization_info");
    Boolean isResident = null;

    if(subjects.size() > 0) {
        BaseEntity subject = (BaseEntity) subjects.get(0);
        isResident = (Integer)subject.getEls("{count}country[code_numeric=398]") > 0;
    }

    subjects = (List) entity.getEls("{get}subject.person_info");

    if(subjects.size() > 0) {
        BaseEntity subject = (BaseEntity) subjects.get(0);
        isResident = (Integer)subject.getEls("{count}country[code_numeric=398]") > 0;
    }

    if(isResident == null)
       return true;
    if(isResident && ba.charAt(4) == ''1'')
        return true;
    if(!isResident && ba.charAt(4) == ''2'')
        return true;

    return false;
}

function boolean isBA7thSymbolCorrect(IBaseEntityProcessorDao baseEntityProcessorDao, IMetaClassRepository metaClassRepository, String ba,String currencyCode, Date reportDate){
    if(ba.length() < 7) return true;
    if(ba.length() == 7 && ba.endsWith("000")) return true;

    if(currencyCode.equals("KZT"))
       return ba.charAt(6) == ''1'';

    boolean isConvertible = isCurrencyConvertible(baseEntityProcessorDao, metaClassRepository, currencyCode, reportDate);

    if(isConvertible)
        return ba.charAt(6) == ''2'';

    return ba.charAt(6) == ''3'';

}
', 'функций проверки балансовых счетов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (178, 'function String getSubjectTypeCodeOfCredit(IBaseEntityProcessorDao baseEntityProcessorDao, BaseEntity credit){
  try{
  		if(credit == null)
  		    return "";
        return (String) credit.getEl("creditor.subject_type.code");
    } catch (Exception e) {
        credit.addValidationError(e.getMessage());
    }
    return null;
}', 'Функция для работы c типом субъекта', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (204, 'rule "INCORRECT_PROVISION_MSFO"
//MES1600
when
  $entity: BaseEntity(getMeta().getClassName == "credit"
      && getEl("portfolio.portfolio_msfo.code") != null
      && getEl("change.credit_flow.provision.provision_msfo.value") != null
      && getEl("change.credit_flow.provision.provision_msfo.value") > 0)
then
  $entity.addValidationError("Если сумма провизий по неоднородным кредитам по требованиям МСФО больше 0, то код однородного портфеля не заполняется");
end', 'INCORRECT_PROVISION_MSFO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (205, 'rule "INCORRECT_PROVISION_KFN"
//MES1600
when
  $entity: BaseEntity(getMeta().getClassName == "credit"
      && getEl("portfolio.portfolio.code") != null
      && getEl("change.credit_flow.provision.provision_kfn.value") != null
      && getEl("change.credit_flow.provision.provision_kfn.value") > 0)
then
  $entity.addValidationError("Если сумма провизий по неоднородным кредитам по требованиям УО больше 0, то код однородного портфеля не заполняется");
end', 'INCORRECT_PROVISION_KFN', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (206, 'rule "credit Type check"
when:
 $entity: BaseEntity( getMeta().getClassName == "credit" && getEl("credit_type.code") == null )
then
 $entity.addValidationError("Не указан код вида займа (условного обязательства)");
end', 'credit_type', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (207, 'rule "документ иной"
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                && getEls("{count}subject.docs[doc_type.code=16][name=null]") > 0 )
then
 $entity.addValidationError("При указании иного документа наименование документа не должен быть пустым");
end', 'документ иной', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (208, 'rule "название документа"
//MES917
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                && getEls("{count}subject.docs[doc_type.code!=16][name!=null]") > 0 )
then
 $entity.addValidationError("Присутствует излишнее наименование документа");
end', 'название документа', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (209, 'rule "признак ИП - тип юр лица"
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                && (getEls("{setString(01,04,07)}subject[is_organization=true]organization_info[is_se=true]enterprise_type.code") > 0
                   ||
                    getEls("{setString(02,03,05,06,08,09)}subject[is_organization=true]organization_info[is_se=false]enterprise_type.code") > 0
                   ||
                    (getEls("{count}subject[is_organization=true]organization_info[enterprise_type.code=null][country.code_numeric=398]") > 0

                       && getEls("{setString(08)}subject.organization_info.legal_form.code")==0 )                  )

                   )
then
 $entity.addValidationError("Код субъекта частного предпринимательства указан некорректно");
end', 'признак ИП - тип юр лица', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (210, 'rule "идентификацонный документ для КЗ"
//17 и 99 убраны
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                 &&   getEls("{setString(06,07,11)}subject.docs.doc_type.code") == 0
                 && ( getEls("{count}subject[is_person=true]person_info[country.code_numeric=398]") > 0
                  || getEls("{count}subject[is_organization=true]organization_info[country.code_numeric=398]") > 0)
                  )

then
 $entity.addValidationError("У резидента отсутствует обязательный идентификационный документ");
end', 'идентификацонный документ для КЗ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (179, 'rule "DEBTREMAINS_DEBT_PASDUE_OPEN_DATE_MORE_THAN_OR_EQUAL_TO_REPORT_DATE"
//MES825
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEl("change.remains.debt.pastdue.open_date") != null
        && getEl("change.remains.debt.pastdue.open_date") >= reportDate)
then
  $entity.addValidationError("Дата вынесения на счет просроченной задолженности по основному долгу не должна быть равна или позднее отчетной даты");
end', 'debt_pastdue_vs_report_date', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (180, 'rule "DEBT_REMAINS_DEBT_PASTDUE_CLOSE_DATE_LESS_THAN_OPEN_DATE"
//MES827
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
    && getEl("change.remains.debt.pastdue.open_date")!=null
    && getEl("change.remains.debt.pastdue.close_date")!=null
    && getEl("change.remains.debt.pastdue.open_date") > getEl("change.remains.debt.pastdue.close_date") )
then
  $entity.addValidationError("Дата погашения просроченной задолженности по основному долгу не должна быть ранее даты вынесения на счет просроченной задолженности по основному долгу");
end', 'debt_pastdue_od_vs_cd', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (181, 'rule "DEBT_REMAINS_DEBT_PASTDUE_CLOSE_DATE_MORE_THAN_REPORT_DATE"
//MES828
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && getEl("change.remains.debt.pastdue.close_date") != null
                && getEl("change.remains.debt.pastdue.close_date") >= getReportDate())
then
   $entity.addValidationError("Дата погашения просроченной задолженности по основному долгу не должна быть позднее отчетной даты")
end', 'debt_pastdue_cd_rd', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (182, 'rule "DEBT_REMAINS_DEBT_PASTDUE_VALUE_NULL_WHEN_CLOSE_DATE_IS_NOT_SET"
//MES829
when
  $entity: BaseEntity( getMeta().getClassName() == "credit"
                        && (getEl("change.remains.debt.pastdue.value") == null
                            || getEl("change.remains.debt.pastdue.value") == 0.0 )
                        &&    getEl("change.remains.debt.pastdue.close_date") == null
                        &&    getEl("change.remains.debt.pastdue.open_date") != null)
then
   $entity.addValidationError("При наличии даты вынесения на счет просроченной задолженности по основному долгу и отсутствии даты погашения просроченной задолженности по основному долгу» значение остатка не может быть пустым или равным нулю.")
end', 'debt_pastdue_val1', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (183, 'rule "DEBT_REMAINS_DEBT_PASTDUE_OPEN_DATE_IS_NULL_WHEN_VALUE_IS_NON_ZERO"
//MES833
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && getEl("change.remains.debt.pastdue.open_date") == null
                && getEl("change.remains.debt.pastdue.value")!=null
                && getEl("change.remains.debt.pastdue.value")!= 0.0 )
then
   $entity.addValidationError("Отсутствует дата вынесения на счет просроченной задолженности по основному долгу")
end', 'debt_pastdue_val2', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (184, 'rule "DEBTREMAINS_INTEREST_PASDUE_OPEN_DATE_MORE_THAN_OR_EQUAL_TO_REPORT_DATE"
//MES826
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEl("change.remains.interest.pastdue.open_date") != null
        && getEl("change.remains.interest.pastdue.open_date") >= reportDate)
then
  $entity.addValidationError("Дата вынесения на счет просроченной задолженности по вознаграждению не должна быть равна или позднее отчетной даты");
end', 'interest_pastdue_vs_rd', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (185, 'rule "DEBT_REMAINS_INTEREST_PASTDUE_CLOSE_DATE_LESS_THAN_OPEN_DATE"
//MES830
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
    && getEl("change.remains.interest.pastdue.open_date")!=null
    && getEl("change.remains.interest.pastdue.close_date")!=null
    && getEl("change.remains.interest.pastdue.open_date") > getEl("change.remains.interest.pastdue.close_date") )
then
  $entity.addValidationError("Дата погашения просроченной задолженности по начисленному вознаграждению не может быть ранее даты вынесения на счет");
end', 'interest_pastdue_od_vs_cd', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (186, 'rule "DEBT_REMAINS_INTEREST_PASTDUE_CLOSE_DATE_MORE_THAN_REPORT_DATE"
//MES831
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
            && getEl("change.remains.interest.pastdue.close_date") != null
            && getEl("change.remains.interest.pastdue.close_date") >= getReportDate())
then
   $entity.addValidationError("Дата погашения просроченной задолженности по начисленному вознаграждению не может быть равна или позднее отчетной даты")
end', 'interest_pastdue_cd_rd', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (187, 'rule "DEBT_REMAINS_INTEREST_PASTDUE_VALUE_NULL_WHEN_CLOSE_DATE_IS_NOT_SET"
//MES832
when
  $entity: BaseEntity( getMeta().getClassName() == "credit"
                        && (getEl("change.remains.interest.pastdue.value") == null
                          || getEl("change.remains.interest.pastdue.value") == 0.0 )
                        &&    getEl("change.remains.interest.pastdue.close_date") == null
                        &&    getEl("change.remains.interest.pastdue.open_date") != null)
then
   $entity.addValidationError("При наличии даты вынесения на счет просроченной задолженности по начисленному вознаграждению и отсутствии даты погашения просроченной задолженности по начисленному вознаграждению значение остатка не может быть пустым или равным нулю.")
end', 'interest_pastdue_val1', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (188, 'rule "DEBT_REMAINS_INTEREST_PASTDUE_OPEN_DATE_IS_NULL_WHEN_VALUE_IS_NON_ZERO"
//MES834
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
                && getEl("change.remains.interest.pastdue.open_date") == null
                && getEl("change.remains.interest.pastdue.value")!=null
                && getEl("change.remains.interest.pastdue.value")!= 0.0 )
then
   $entity.addValidationError("Отсутствует дата вынесения на счет просроченной задолженности по вознаграждению")
end', 'interest_pastdue_val2', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (189, 'rule "ACTUAL_ISSUE_DATE_MISMATCH"
//MES817
when
    $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEl("actual_issue_date")!=null && getEl("primary_contract.date")!=null
        && getEl("actual_issue_date") < getEl("primary_contract.date"))
then
    $entity.addValidationError("Ошибка по фактической дате выдачи: фактическая дата выдачи должна быть позднее или равна дате договора");
end', 'ACTUAL_ISSUE_DATE_MISMATCH', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (190, 'rule "CONTRACT_MATURITY_DATE_MISMATCH"
//MES818
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEl("contract_maturity_date") != null
        && getEl("contract_maturity_date") < getEl("primary_contract.date"))
then
  $entity.addValidationError("Ошибка по дате погашения по условиям договора: дата погашения по договору должна быть позднее или равна дате договора")
end
', 'CONTRACT_MATURITY_DATE_MISMATCH', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (191, 'rule "CONTRACT_MATURITY_DATE_MISMATCH_ACTUAL_ID"
//MES818
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEl("contract_maturity_date") != null
        && getEl("actual_issue_date") != null
        && getEl("contract_maturity_date") < getEl("actual_issue_date"))
then
  $entity.addValidationError("Ошибка по дате погашения по условиям договора: дата погашения по договору должна быть позднее или равна фактической дате выдачи")
end
', 'CONTRACT_MATURITY_DATE_MISMATCH_ACTUAL_ID', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (192, 'rule "MATURITY_DATE_MISMATCH"
//MES819
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && mDate : getEl("maturity_date")
        && pDate : getEl("primary_contract.date")
        && mDate!=null && pDate !=null
        && mDate < pDate)
then
  $entity.addValidationError("Ошибка по дате фактического погашения: дата фактического погашения должна быть позднее или равна дате договора");
end
', 'MATURITY_DATE_MISMATCH', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (193, 'rule "PROLONGATION_DATE_MISMATCH_820"
//MES820
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && pDate: getEl("prolongation_date") !=null
            && mDate: getEl("contract_maturity_date")!=null
        && pDate < mDate)
then
  $entity.addValidationError("Неверная дата окончания пролонгации");
end
', 'PROLONGATION_DATE_MISMATCH', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (194, 'rule "PLEDGE_ABSENSE"
//MES810
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEls("{count}pledges") == 0)
then
  $entity.addValidationError("Отсутствует блок обеспечения");
end
', 'PLEDGE_ABSENSE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (195, 'rule "PLEDGE_NO_ABSENSE"
//MES814
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEls("{count}pledges[pledge_type.code!=47][contract=null]") > 0)
then
  $entity.addValidationError("Отсутствует номер договора залога");
end
', 'PLEDGE_NO_ABSENSE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (196, 'rule "PLEDGE_SUM_ABSENSE"
//MES814
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEls("{count}pledges[pledge_type.code!=47][value=null]") > 0)
then
  $entity.addValidationError("Отсутствует залоговая сумма");
end
', 'PLEDGE_SUM_ABSENSE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (197, 'rule "PLEDGE_BLANK_WITH_NON_BLANK"
//MES837
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
    && getEls("{count}pledges") > 1
    && getEls("{count}pledges[pledge_type.code=47]") > 0)
then
  $entity.addValidationError("Ошибка по обеспечению: Значение «Без залога» не может использоваться с иными значениями");
end
', 'PLEDGE_BLANK_WITH_NON_BLANK', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (198, 'rule "DEBTOR_DOC_ABSENSE"
//MES903
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEls("{count}subject") > 0
        && getEls("{count}subject.docs") == 0)
then
  $entity.addValidationError("Отсутствует документ субъекта кредитной истории");
end', 'DEBTOR_DOC_ABSENSE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (199, 'rule "SUBJECT_DOC_NO_EMPTY"
//MES914
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && getEls("{count}subject.docs[no=null]") > 0)
then
  $entity.addValidationError("Не указан номер документа субъекта");
end', 'SUBJECT_DOC_NO_EMPTY', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (200, 'rule "BANK_RELATION_BLOCK_INVALID"
//MES921
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
        && (
            (getEls("{count}subject[is_person=true]person_info.bank_relations.bank_relation") > 1
            && getEls("{count}subject[is_person=true]person_info.bank_relations[bank_relation.code=50]") > 0)
            ||
            (getEls("{count}subject[is_organization=true]organization_info.bank_relations.bank_relation") > 1
            && getEls("{count}subject[is_organization=true]organization_info.bank_relations[bank_relation.code=50]") > 0)
        ))
then
  $entity.addValidationError("Ошибка по признаку связанности: Значение \"Не связан\" не может использоваться с иными значениями");
end
', 'BANK_RELATION_BLOCK_INVALID', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (201, 'rule "BANK_RELATION_DUPLICATE"
//MES921
//beef dobavit creditora
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
  && (
        getEls("{hasDuplicates(subject[is_person=true]person_info.bank_relations)}bank_relation.code") == true ||
        getEls("{hasDuplicates(subject[is_organization=true]organization_info.bank_relations)}bank_relation.code") == true
     ))
then
  $entity.addValidationError("Ошибка по признаку связанности: Дублируется значение");
end
', 'BANK_RELATION_DUPLICATE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (202, 'rule "BANK_RELATION_EMPTY"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
  && (
        (getEls("{count}subject[is_person=true]person_info") > 0 &&
        getEls("{count}subject[is_person=true]person_info.bank_relations.bank_relation.code") == 0)
        ||
        (getEls("{count}subject[is_organization=true]organization_info") > 0 &&
        getEls("{count}subject[is_organization=true]organization_info.bank_relations.bank_relation.code") == 0)
     ))
then
  $entity.addValidationError("Не заполнен блок связанности");
end
', 'BANK_RELATION_EMPTY', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (203, 'rule "PLEDGE_DUPLICATE"
//MES836
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
  && getEls("{hasDuplicates(pledges)}pledge_type.code,contract") == true)
then
  $entity.addValidationError("Ошибка по обеспечению: Дублируются записи \"Номер договора залога\", \"Вид обеспечения\"");
end', 'PLEDGE_DUPLICATE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (146, 'rule "BVUNODOC"
when
  $entity: BaseEntity(getMeta().getClassName == "credit"
      && getEl("subject.is_creditor") == true)
  $r: String(length > 0) from isBVUNODoc((List) $entity.getEls("{get}subject.docs"),
                                          baseEntityProcessorDao, metaClassRepository, creditorCache)
then
  $entity.addValidationError($r);
end', 'BVUNODOC', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (147, 'rule "MATURITY_DATE_2100_LISTENER"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEl("maturity_date") !=null
       && $md: getStringDateV((Date) getEl("maturity_date")) == "01.01.2100"
       && $creditorId: getBaseEntityReportDate().getCreditorId()
       && $rd: reportDate)
then
  $entity.put("maturity_date",new BaseEntityDateValue(0, $creditorId, $rd, null, false, true));
end', 'MATURITY_DATE_2100_LISTENER', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (148, 'rule "MATURITY_DATE_MISMATCH_REPORT_DATE"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && $md: getEl("maturity_date") !=null
       && getStringDateV((Date) getEl("maturity_date")) != "01.01.2100"
       && $rd: reportDate !=null
       && $md >= $rd)
then
  $entity.addValidationError("Ошибка по дате фактического погашения: дата фактического погашения должна быть раньше чем отчетная дата");
end', 'MATURITY_DATE_MISMATCH_REPORT_DATE', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (149, 'rule "PLEDGE_47_CONTRACT"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
       && getEls("{count}pledges[pledge_type.code=47][contract!=null]") > 0)
then
  $entity.addValidationError("Присутствует излишняя информация(контракт) по обеспечению с типом «Без залога»");
end', 'PLEDGE_47_CONTRACT', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (150, 'rule "проверка документов руководителя"
when:
    $entity: BaseEntity(getMeta().getClassName == "credit"
      && (getEls("{setString(07,10,15,16,18)}subject[is_organization=true][organization_info.is_se=false]organization_info.head.docs.doc_type.code") > 0 ))
then
    $entity.addValidationError("Вид документа не соответствует руководителю юридического лица");
end', 'проверка документов руководителя', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (151, '
function String getBinError(List bins, String entCode) {
    if(bins.size() < 1)
        return "";

    String bin = (String)bins.get(0);
    try {
         binCorrect(bin, entCode);
    } catch(Exception e) {
         return bin + ": " + e.getMessage();
    }
    return "";
}

function void binCorrect(String bin,String entCode) {

    int[][] WEIGHTS = new int[][]{
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            {3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2}
        };

    Set<String> SE_CODES = new HashSet<String>(Arrays.asList("03","06","09"));
    if("02".equals(entCode)  || "05".equals(entCode) || "08".equals(entCode)) {
       if(getInvalidIINs(Arrays.asList(bin)).size() > 0)
           throw new RuntimeException("Бин заполнен не верно для СЧП 02, 05, 08 (проверка как ИИН)");

       return;
    }

    if(bin.length() != 12)
        throw new RuntimeException("Должен содержать 12 символов");

    if(!isDateValid(bin.substring(0,4), "yyMM"))
        throw new RuntimeException("Первые 4 разряда должны соответствовать году и месяцу регистрации в формате ГГММ");

    //проверка 5 го символа
    if(bin.charAt(4) < ''4'' || bin.charAt(4) > ''6'')
        throw new RuntimeException("Пятый разряд должен быть равен 4, 5, или 6");

    if(SE_CODES.contains(entCode)) {
        if(bin.charAt(4) != ''6'')
            throw new RuntimeException("Пятый разряд должен быть равен 6 для субъектов с кодом СЧП 03, 06 или 09");
    } else if (bin.charAt(4) != ''4'' && bin.charAt(4) != ''5'')
            throw new RuntimeException("Пятый разряд должен быть равен 4 или 5 для субъектов с кодом СЧП 01, 04 или 07");

    //проверка 6 го символа
    if(bin.charAt(5) < ''0'' || bin.charAt(5) > ''4'')
        throw new RuntimeException("Шестой разряд должен быть равен 0, 1, 2, 3 или 4");

    if(!SE_CODES.contains(entCode) && bin.charAt(5) == ''3'')
        throw new RuntimeException("Шестой разряд должен быть равен 0, 1, или 2 для субъектов с кодом СЧП 01, 04 или 07");


    int sum = 10;
    for(int i=0;i<WEIGHTS.length;i++) {
       int[] w = WEIGHTS[i];
       sum = 0;

       for(int j=0;j<w.length;j++)
         sum+= (bin.charAt(j) - ''0'' ) * w[j];

       sum %= 11;

       if(sum != 10)
         break;
    }

    if(sum != bin.charAt(11) - ''0'')
       throw new RuntimeException("Не совпадает контрольный символ");
}', 'функций для проверки бин', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (152, 'rule "документ юр лица БИН"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& ($r : getBinError((List)getEls("{get}subject[is_organization=true]docs[doc_type.code=07]no"),
			                 (String)getEl("subject.organization_info.enterprise_type.code")) && $r.length() > 0))
then
	$entity.addValidationError("" + $r);
end', 'документ юр лица БИН', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (153, 'import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;

global kz.bsbnb.usci.eav.repository.IMetaClassRepository metaClassRepository;
global kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao baseEntityProcessorDao;
global java.util.Map creditorCache;

', 'Нужные импорты для работы функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (154, '
function boolean isDateValid(String date,String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
}

function boolean isDigit(char c){
    return ''0'' <= c && c <= ''9'';
}

function int getFirstDay(Date date){
    java.util.Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
}

function List getNot12DigitStrings(List docs){
    List ret = new ArrayList();
        for(Object doc : docs) {
            if(!((String) doc ).matches("\\d{12}"))
                ret.add(doc);
        }
    return ret;
}

function List get12ZeroStrings(List docs){
    List ret = new ArrayList();
        for(Object doc: docs) {
            if(((String) doc ).equals("000000000000"))
                ret.add(doc);
        }
    return ret;
}

function List getNegativesFromDoubles(List values){
    List ret = new ArrayList();
        for(Object value : values) {
            if( (Double) value < 0)
               ret.add(value);
        }
    return ret;
}

function String getStringDateV(Date d){

   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

   return sdf.format(d);

}

', 'Общие функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (155, '
function boolean hasBACT(String baNo, String creditCode, BaseEntity entity){
    try {
        // fixme!
        if (baNo == null || creditCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;

        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_ct"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity creditType = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        creditType.put("code", new BaseValue(reportDate, creditCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("credit_type",  new BaseValue(reportDate, creditType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}
', 'функция сверки балансовый счет - тип кредита', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (156, 'function boolean hasBADRT(String baNo, String drtCode, BaseEntity entity){
    try{
        // fixme!
        if (baNo == null || drtCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_drt"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity debtRemainsType = new BaseEntity(metaClassRepository.getMetaClass("ref_debt_remains_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        debtRemainsType.put("code", new BaseValue(reportDate, drtCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("debt_remains_type",  new BaseValue(reportDate, debtRemainsType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}

', 'функция сверки балансовый счет - тип остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (157, 'function boolean isExclusiveIIN(String iin) {
    try{
        //  поиск исключительного иин
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("06"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}

function boolean isExclusiveRNN(String iin) {
    try{
        //  поиск исключительного рнн
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("11"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}
', 'функция исключительный документ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (158, '
function String isBVUNODoc(List docs, IBaseEntityProcessorDao processorDao,
                           IMetaClassRepository metaClassRepository,
                           Map creditorCache){
    Set<Long> uniqueCreditorIds = new HashSet<Long>();

    for(Object docObject: docs) {
        BaseEntity doc = (BaseEntity) docObject;
        if(creditorCache.size() < 1) {
            List<BaseEntity> creditorsDbList = processorDao.getEntityByMetaClass(
                    metaClassRepository.getMetaClass("ref_creditor"));

            for (BaseEntity creditor : creditorsDbList) {
                BaseSet creditorDocs = (BaseSet) ((BaseValue) creditor.getBaseValue("docs")).getValue();
                for (IBaseValue creditorDocValue : creditorDocs.get()) {
                    BaseEntity creditorDoc = (BaseEntity) creditorDocValue.getValue();
                    String docKey = creditorDoc.getEl("no") + " | " + creditorDoc.getEl("doc_type.code");
                    creditorCache.put(docKey, creditor);
                }
            }
        }

        if(creditorCache.size() < 1)
            throw new RuntimeException("Справочник кредиторов пуст.");


        String docKey = doc.getEl("no") + " | " + doc.getEl("doc_type.code");
        if(!creditorCache.containsKey(docKey))
            return "Не существующий документ из справочника кредиторов " + docKey;

        uniqueCreditorIds.add(((BaseEntity) creditorCache.get(docKey)).getId());
    }

    if(uniqueCreditorIds.size() > 1) {
        return "Документы из разных кредиторов";
    }

    return "";
}', 'работа со справочником кредиторов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (159, 'rule "gen_remains_debt_current"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.debt.current.value") != null
                && getEl("change.remains.debt.current.value") > 0
                && getEl("change.remains.debt.current.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток основного долга в валюте договора: непросроченная задолженность");
end', 'Остаток - Основной долг - Непросроченная  задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (160, 'rule "gen_remains_debt_pastdue"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.debt.pastdue.value") != null
                && getEl("change.remains.debt.pastdue.value") > 0
                && getEl("change.remains.debt.pastdue.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток основного долга в валюте договора: просроченная задолженность");
end', 'Остаток - Основной долг - Просроченная задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (161, 'rule "credit_gen_remains_debt_writeoff"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.debt.write_off.value") != null
                && getEl("change.remains.debt.write_off.value") > 0
                && getEl("change.remains.debt.write_off.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток основного долга в валюте договора: списанная с баланса задолженность");
end', 'Остаток - Основной долг - Списанная с баланса задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (162, 'rule "gen_remains_interest_current"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit" 
                && getEls("{setString(KZT)}currency.short_name") == 0
                && getEl("change.remains.interest.current.value") != null
                && getEl("change.remains.interest.current.value") > 0
                && getEl("change.remains.interest.current.value_currency") == null )
then
  $entity.addValidationError("Не заполнен остаток начисленного вознаграждения в валюте договора: непогашенного");
end', 'Остаток - Вознаграждение - Непросроченная  задолженность', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (246, 'rule "документ РНН должен содержать 12 символов"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor"
			&& ( $r : getNot12DigitStrings((List)getEls("{get}docs[doc_type.code=11]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ должен содержать 12 цифр: " + $r);
end', 'документ РНН должен содержать 12 символов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (247, 'rule "документ БИН головной банк должен содержать 12 символов"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor"
			&& ( $r : getNot12DigitStrings((List)getEls("{get}docs[doc_type.code=07]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ должен содержать 12 цифр: " + $r);
end', 'документ БИН должен содержать 12 символов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (248, 'rule "документ РНН из 12 нулей"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor"
			&& ( $r : get12ZeroStrings((List)getEls("{get}docs[doc_type.code=11]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ содержит 12 нулей: " + $r);
end', 'документ РНН из 12 нулей', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (249, 'rule "документ БИН из 12 нулей"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor"
			&& ( $r : get12ZeroStrings((List)getEls("{get}docs[doc_type.code=07]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ содержит 12 нулей: " + $r);
end', 'документ БИН из 12 нулей', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (250, 'import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;

global kz.bsbnb.usci.eav.repository.IMetaClassRepository metaClassRepository;
global kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao baseEntityProcessorDao;
global java.util.Map creditorCache;

', 'Нужные импорты для работы функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (251, '
function boolean isDateValid(String date,String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
}

function boolean isDigit(char c){
    return ''0'' <= c && c <= ''9'';
}

function int getFirstDay(Date date){
    java.util.Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
}

function List getNot12DigitStrings(List docs){
    List ret = new ArrayList();
        for(Object doc : docs) {
            if(!((String) doc ).matches("\\d{12}"))
                ret.add(doc);
        }
    return ret;
}

function List get12ZeroStrings(List docs){
    List ret = new ArrayList();
        for(Object doc: docs) {
            if(((String) doc ).equals("000000000000"))
                ret.add(doc);
        }
    return ret;
}

function List getNegativesFromDoubles(List values){
    List ret = new ArrayList();
        for(Object value : values) {
            if( (Double) value < 0)
               ret.add(value);
        }
    return ret;
}

function String getStringDateV(Date d){

   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

   return sdf.format(d);

}

', 'Общие функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (252, 'rule "документ РНН должен содержать 12 символов"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor_branch"
			&& ( $r : getNot12DigitStrings((List)getEls("{get}docs[doc_type.code=11]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ должен содержать 12 цифр: " + $r);
end', 'документ РНН должен содержать 12 символов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (253, 'rule "документ БИН головной банк должен содержать 12 символов"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor_branch"
			&& ( $r : getNot12DigitStrings((List)getEls("{get}docs[doc_type.code=07]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ должен содержать 12 цифр: " + $r);
end', 'документ БИН должен содержать 12 символов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (254, 'rule "документ РНН из 12 нулей"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor_branch"
			&& ( $r : get12ZeroStrings((List)getEls("{get}docs[doc_type.code=11]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ содержит 12 нулей: " + $r);
end', 'документ РНН из 12 нулей', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (255, 'rule "документ БИН из 12 нулей"
when
	$entity: BaseEntity(getMeta().getClassName() == "ref_creditor_branch"
			&& ( $r : get12ZeroStrings((List)getEls("{get}docs[doc_type.code=07]no")) && $r.size() > 0))
then
	$entity.addValidationError("документ содержит 12 нулей: " + $r);
end', 'документ БИН из 12 нулей', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (256, 'import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;

global kz.bsbnb.usci.eav.repository.IMetaClassRepository metaClassRepository;
global kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao baseEntityProcessorDao;
global java.util.Map creditorCache;

', 'Нужные импорты для работы функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (257, '
function boolean isDateValid(String date,String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
}

function boolean isDigit(char c){
    return ''0'' <= c && c <= ''9'';
}

function int getFirstDay(Date date){
    java.util.Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
}

function List getNot12DigitStrings(List docs){
    List ret = new ArrayList();
        for(Object doc : docs) {
            if(!((String) doc ).matches("\\d{12}"))
                ret.add(doc);
        }
    return ret;
}

function List get12ZeroStrings(List docs){
    List ret = new ArrayList();
        for(Object doc: docs) {
            if(((String) doc ).equals("000000000000"))
                ret.add(doc);
        }
    return ret;
}

function List getNegativesFromDoubles(List values){
    List ret = new ArrayList();
        for(Object value : values) {
            if( (Double) value < 0)
               ret.add(value);
        }
    return ret;
}

function String getStringDateV(Date d){

   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

   return sdf.format(d);

}

', 'Общие функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (258, '
function boolean hasBACT(String baNo, String creditCode, BaseEntity entity){
    try {
        // fixme!
        if (baNo == null || creditCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;

        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_ct"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity creditType = new BaseEntity(metaClassRepository.getMetaClass("ref_credit_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        creditType.put("code", new BaseValue(reportDate, creditCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("credit_type",  new BaseValue(reportDate, creditType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}
', 'функция сверки балансовый счет - тип кредита', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (259, 'function boolean hasBADRT(String baNo, String drtCode, BaseEntity entity){
    try{
        // fixme!
        if (baNo == null || drtCode == null) {
            return true;
        }

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //получить отчетную дату
        Date reportDate = entity.getReportDate();

        //Инициализация сущности - для поиска
        //  поиск соответсвия из справочника бал счетов и типа кредита
        //  все классы с префиксом ref - справочники
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_ba_drt"), reportDate);

        //Создание сущности балансовый счет (пустой)
        IBaseEntity beAccount = new BaseEntity(metaClassRepository.getMetaClass("ref_balance_account"), reportDate);

        //Создание сущности тип кредита (пустой)
        IBaseEntity debtRemainsType = new BaseEntity(metaClassRepository.getMetaClass("ref_debt_remains_type"), reportDate);

        //заполняем параметры поиска
        beAccount.put("no_", new BaseValue(reportDate, baNo));
        debtRemainsType.put("code", new BaseValue(reportDate, drtCode));

        //заполняем параметры поиска для родительской сущности
        be.put("balance_account",  new BaseValue(reportDate, beAccount));
        be.put("debt_remains_type",  new BaseValue(reportDate, debtRemainsType));

        //Поиск сущности (entityService - это сервис ЕССП)
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        entity.addValidationError(e.getMessage());
    }

    return false;
}

', 'функция сверки балансовый счет - тип остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (260, 'function boolean isExclusiveIIN(String iin) {
    try{
        //  поиск исключительного иин
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("06"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}

function boolean isExclusiveRNN(String iin) {
    try{
        //  поиск исключительного рнн
        //  все классы с префиксом ref - справочники

        IBaseEntityProcessorDao baseEntityProcessorDao = BRMSHelper.rulesLoadDao;
        IMetaClassRepository metaClassRepository = BRMSHelper.rulesMetaDao;


        //Инициализация сущности - для поиска
        BaseEntity be = new BaseEntity(metaClassRepository.getMetaClass("ref_exclusive_doc"), new Date());

        BaseEntity docType = new BaseEntity(metaClassRepository.getMetaClass("ref_doc_type"), new Date());

        docType.put("code", new BaseValue("11"));

        //заполняем параметры поиска
        be.put("code",new BaseValue(iin));
        be.put("doc_type", new BaseValue(docType));

        //Поиск сущности
        IBaseEntity res = baseEntityProcessorDao.prepare(be, 0L);

        //если идентификатор больше нуля - данное соответвие присутсвтует в базе
        return res.getId() > 0;
    } catch (Exception e) {
        return false;
    }
}
', 'функция исключительный документ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (261, '
function String isBVUNODoc(List docs, IBaseEntityProcessorDao processorDao,
                           IMetaClassRepository metaClassRepository,
                           Map creditorCache){
    Set<Long> uniqueCreditorIds = new HashSet<Long>();

    for(Object docObject: docs) {
        BaseEntity doc = (BaseEntity) docObject;
        if(creditorCache.size() < 1) {
            List<BaseEntity> creditorsDbList = processorDao.getEntityByMetaClass(
                    metaClassRepository.getMetaClass("ref_creditor"));

            for (BaseEntity creditor : creditorsDbList) {
                BaseSet creditorDocs = (BaseSet) ((BaseValue) creditor.getBaseValue("docs")).getValue();
                for (IBaseValue creditorDocValue : creditorDocs.get()) {
                    BaseEntity creditorDoc = (BaseEntity) creditorDocValue.getValue();
                    String docKey = creditorDoc.getEl("no") + " | " + creditorDoc.getEl("doc_type.code");
                    creditorCache.put(docKey, creditor);
                }
            }
        }

        if(creditorCache.size() < 1)
            throw new RuntimeException("Справочник кредиторов пуст.");


        String docKey = doc.getEl("no") + " | " + doc.getEl("doc_type.code");
        if(!creditorCache.containsKey(docKey))
            return "Не существующий документ из справочника кредиторов " + docKey;

        uniqueCreditorIds.add(((BaseEntity) creditorCache.get(docKey)).getId());
    }

    if(uniqueCreditorIds.size() > 1) {
        return "Документы из разных кредиторов";
    }

    return "";
}', 'работа со справочником кредиторов', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (262, '
function List getInvalidIINs(List iins){
    List ret = new ArrayList();
    for(Object iin : iins) {
        if(!iinCorrect((String) iin ) && !isExclusiveIIN((String) iin))
            ret.add(iin);
    }
    return ret;
}

function  boolean iinCorrect(String iin){

    int[] weights = new int[]{1,2,3,4,5,6,7,8,9,10,11,1,2};

    int sum = 0;
    if(iin.length() != 12)
        return false;

    if(!isDateValid(iin.substring(0,6),"yyMMdd"))
        return false;

    if(iin.charAt(6) < ''0'' || iin.charAt(6) > ''6'')
        return false;

    for(int i=0;i<11;i++)
        sum += (iin.charAt(i) - ''0'' ) * weights[i];
    sum %= 11;
    int last = iin.charAt(11) - ''0'';
    if(sum ==  10) {
        sum = 0;
        for(int i=0;i<11;i++)
            sum+=(iin.charAt(i) - ''0'') * weights[i+2];
        sum %= 11;
    }
    return sum == last;
}', 'функций для проверки иин', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (263, 'rule "exclusive_doc"
when
  $entity: BaseEntity(getMeta().getClassName() == "ref_exclusive_doc"
                && getEl("doc_type.code") == "06"
                && $r : getInvalidIINs((List)getEls("{get}code"))
                && $r.size() == 0)
then
  $entity.addValidationError("Исключительный ИИН заполнен не верно");
end', 'Исключительный иин', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (211, 'rule "проверка документов по признаку IS_PERSON_DOC/IS_ORG_DOC"
when:
 $entity: BaseEntity( getMeta().getClassName == "credit"
                 && (getEls("{setString(07,10,15,16,18)}subject[is_person=true]docs.doc_type.code") > 0
                  || getEls("{setString(01,02,03,04,05,06,08,12,13)}subject[is_organization=true][organization_info.is_se=false]docs.doc_type.code") > 0
                  ))
then
 $entity.addValidationError("Вид документа не соответствует типу субъекта кредитной истории");
end', 'проверка документов по признаку IS_PERSON_DOC/IS_ORG_DOC', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (212, '
function boolean creditorBranchIsGood(BaseEntity entity) {
   try {
      //Извлечь филиал
      BaseEntity creditorBranch = (BaseEntity) entity.getEl("creditor_branch");

      //Не обязательно для заполнения
      if(creditorBranch == null)
        return true;

      //Извлечь кредитор
      BaseEntity creditor = (BaseEntity) entity.getEl("creditor");

      //Извлечь головной банк филиала
      BaseEntity mainOffice = (BaseEntity) creditorBranch.getEl("main_office");

      //Не обязательно для заполнения
      if(mainOffice == null)
         return true;

      //Проверить на соответсвие
      return creditor.getId() == mainOffice.getId();
   } catch (Exception e){
     //В случае ошибок
     return false;
   }
}', 'функций проверки филиала', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (213, 'rule "проверка филиала"
//Проверка принадлежности филиала кредитору
when:
 $entity: BaseEntity( getMeta().getClassName == "credit" &&  !creditorBranchIsGood($entity) )
then
 $entity.addValidationError("филиал заполнен не верно");
end', 'проверка филиала', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (214, 'rule "Дата договора < Отчетная дата"
//MES838
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
    && pDate: getEl("primary_contract.date")
    && pDate!=null
    && pDate >= getReportDate())
then
  $entity.addValidationError("Дата договора должна быть раньше отчетной даты");
end
', 'Дата договора < Отчетная дата', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (215, 'rule "HAS_CURRENCY_EARN"
when
    $entity: BaseEntity(getMeta().getClassName() == "credit"
             && getEl("currency.short_name") != null
             && getEls("{setString(KZT)}currency.short_name") == 0
             && getEl("has_currency_earn") == null
             && getEls("{setString(02,03,10,12,13,14,15,17,18,19,24,25,26)}credit_type.code") == 0)
then
    $entity.addValidationError("Не заполнен показатель «Наличие валютной выручки и (или) инструментов хеджирования у заемщика» по валютному займу (условному обязательству)");
end', 'HAS_CURRENCY_EARN', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (216, 'rule "NOT_HIMSELF_BIN"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
     && ((getEl("subject.is_creditor") != true) || ((getEl("subject.is_creditor") == true) && getEls("{setString(12,13)}credit_type.code") == 0)))
     $r: String() from $entity.getEls("{get}creditor.docs[doc_type.code=07]no")
     $a: String(equals($r)) from $entity.getEls("{get}subject.docs[doc_type.code=07]no")
then
	$entity.addValidationError("Нельзя указывать себя в качестве субъекта кредитной истории");
end', 'NOT_HIMSELF_BIN', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (217, 'rule "NOT_HIMSELF_RNN"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
     && ((getEl("subject.is_creditor") != true) || ((getEl("subject.is_creditor") == true) && getEls("{setString(12,13)}credit_type.code") == 0)))
     $r: String() from $entity.getEls("{get}creditor.docs[doc_type.code=11]no")
     $a: String(equals($r)) from $entity.getEls("{get}subject.docs[doc_type.code=11]no")
then
	$entity.addValidationError("Нельзя указывать себя в качестве субъекта кредитной истории");
end', 'NOT_HIMSELF_RNN', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (218, 'rule "NO_DEBT_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
		      && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.debt.current.value") != null && getEl("change.remains.debt.current.value") != 0.0)
			  && getEl("change.remains.debt.current.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по основному долгу: непросроченная задолженность");
end', 'NO_DEBT_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (219, 'rule "NO_DEBT_PASTDUE_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.debt.pastdue.value") != null && getEl("change.remains.debt.pastdue.value") != 0.0 )
			  && getEl("change.remains.debt.pastdue.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по основному долгу: просроченная задолженность");
end', 'NO_DEBT_PASTDUE_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (220, 'rule "NO_DEBT_WRITE_OFF_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.debt.write_off.value") != null && getEl("change.remains.debt.write_off.value") != 0.0)
			  && getEl("change.remains.debt.write_off.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по основному долгу: списанная с баланса задолженность");
end', 'NO_DEBT_WRITE_OFF_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (221, 'rule "NO_INTEREST_CURRENT_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.interest.current.value") != null && getEl("change.remains.interest.current.value") != 0.0 )
			  && getEl("change.remains.interest.current.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по вознаграждению: непросроченная задолженность");
end', 'NO_INTEREST_CURRENT_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (222, 'rule "NO_INTEREST_PASTDUE_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
	  		  && (getEl("change.remains.interest.pastdue.value") != null && getEl("change.remains.interest.pastdue.value") != 0.0)
			  && getEl("change.remains.interest.pastdue.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по вознаграждению: просроченная задолженность");
end', 'NO_INTEREST_PASTDUE_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (223, 'rule "NO_PROVISION_KFN_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.credit_flow.provision.provision_kfn.value") != null && getEl("change.credit_flow.provision.provision_kfn.value") != 0.0)
			  && getEl("change.credit_flow.provision.provision_kfn.balance_account.no_") == null)
then
		$entity.addValidationError("Не заполнен номер балансового счета по резервам (провизиям) по требованиям уполномоченного органа");
end', 'NO_PROVISION_KFN_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (224, 'rule "NO_PROVISION_MSFO_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.credit_flow.provision.provision_msfo.value") != null && getEl("change.credit_flow.provision.provision_msfo.value") != 0.0)
			  && getEl("change.credit_flow.provision.provision_msfo.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по резервам (провизиям) по требованиям МСФО");
end', 'NO_PROVISION_MSFO_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (225, 'rule "NO_PROVISION_MSFO_OVER_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.credit_flow.provision.provision_msfo_over_balance.value") != null && getEl("change.credit_flow.provision.provision_msfo_over_balance.value") != 0.0)
			  && getEl("change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по резервам (провизиям) по требованиям МСФО по лимиту кредитной карты/овердрафта");
end', 'NO_PROVISION_MSFO_OVER_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (226, 'rule "NO_CORRECTION_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.correction.value") != null && getEl("change.remains.correction.value") != 0.0)
			  && getEl("change.remains.correction.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по положительной (отрицательной) корректировке");
end', 'NO_CORRECTION_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (227, 'rule "NO_DISCOUNT_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.discount.value") != null && getEl("change.remains.discount.value") != 0.0)
			  && getEl("change.remains.discount.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по дисконту (премии)");
end', 'NO_DISCOUNT_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (228, 'rule "NO_LIMIT_BALANCE_ACCOUNT_NO"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
	          && getSubjectTypeCodeOfCredit(baseEntityProcessorDao, $entity) != "0003"
			  && (getEl("change.remains.limit.value") != null && getEl("change.remains.limit.value") != 0.0)
			  && getEl("change.remains.limit.balance_account.no_") == null)
then
	$entity.addValidationError("Не заполнен номер балансового счета по остатку лимита кредитной карты/овердрафта");
end', 'NO_LIMIT_BALANCE_ACCOUNT_NO', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (229, 'rule "actual_issue_date"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
            && getEls("{setString(12,15,25)}credit_type.code") == 0
            && getEl("actual_issue_date") == null)
then
  $entity.addValidationError("Не заполнена фактическая дата выдачи");
end', 'actual_issue_date', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (230, 'rule "interest_rate_yearly"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
            && getEls("{setString(12,13,14,15,24,25)}credit_type.code") == 0
            && getEl("interest_rate_yearly") == null)
then
  $entity.addValidationError("Не заполнена годовая ставка вознаграждения по договору");
end', 'interest_rate_yearly', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (231, 'rule "region_of_organization"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
            && getEls("{count}subject.organization_info[country.code_numeric=398]addresses[region.code=null]") > 0)
then
  $entity.addValidationError("Не заполнена область в адресе субъекта");
end', 'region_of_organization', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (232, 'rule "region_of_person"
when
  $entity: BaseEntity(getMeta().getClassName() == "credit"
            && getEls("{count}subject.person_info[country.code_numeric=398]addresses[region.code=null]") > 0)
then
  $entity.addValidationError("Не заполнена область в адресе физического лица");
end', 'region_of_person', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (233, 'rule "проверка 5 символа  бс основного долга текущего остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.current.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс основного долга текущего остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (234, 'rule "проверка 5 символа  бс основного долга просроченного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.pastdue.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс основного долга просроченного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (235, 'rule "проверка 5 символа  бс основного долга списанного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.debt.write_off.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс основного долга списанного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (236, 'rule "проверка 5 символа  бс вознаграждения текущего остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.current.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс вознаграждения текущего остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (237, 'rule "проверка 5 символа  бс вознаграждения просроченного остатка"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.interest.pastdue.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс вознаграждения просроченного остатка', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (238, 'rule "проверка 5 символа  бс провизий"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_kfn.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс провизий', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (239, 'rule "проверка 5 символа  бс провизий МСФО"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс провизий МСФО', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (240, 'rule "проверка 5 символа  бс провизий МСФО над балансом"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.credit_flow.provision.provision_msfo_over_balance.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс провизий МСФО над балансом', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (241, 'rule "проверка 5 символа  бс корректировки остатка "
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.correction.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс корректировки остатка ', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (242, 'rule "проверка 5 символа  бс дисконта"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.discount.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс дисконта', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (243, 'rule "проверка 5 символа  бс лимита"
when
	$entity: BaseEntity(getMeta().getClassName() == "credit"
			&& $r : getEl("change.remains.limit.balance_account.no_")
			&& $r != null
			&& !isBA5thSymbolCorrect((String)$r, $entity))
then
	$entity.addValidationError("5й символ балансового счета заполнен не верно: " + $r);
end', 'проверка 5 символа  бс лимита', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (244, 'import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityDateValue;

global kz.bsbnb.usci.eav.repository.IMetaClassRepository metaClassRepository;
global kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao baseEntityProcessorDao;
global java.util.Map creditorCache;

', 'Нужные импорты для работы функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);
INSERT INTO LOGIC_RULES (ID, RULE, TITLE, TITLE_RU, TITLE_KZ, OPEN_DATE, CLOSE_DATE) VALUES (245, '
function boolean isDateValid(String date,String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException pe) {
            return false;
        }
}

function boolean isDigit(char c){
    return ''0'' <= c && c <= ''9'';
}

function int getFirstDay(Date date){
    java.util.Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_MONTH);
}

function List getNot12DigitStrings(List docs){
    List ret = new ArrayList();
        for(Object doc : docs) {
            if(!((String) doc ).matches("\\d{12}"))
                ret.add(doc);
        }
    return ret;
}

function List get12ZeroStrings(List docs){
    List ret = new ArrayList();
        for(Object doc: docs) {
            if(((String) doc ).equals("000000000000"))
                ret.add(doc);
        }
    return ret;
}

function List getNegativesFromDoubles(List values){
    List ret = new ArrayList();
        for(Object value : values) {
            if( (Double) value < 0)
               ret.add(value);
        }
    return ret;
}

function String getStringDateV(Date d){

   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

   return sdf.format(d);

}

', 'Общие функций', null, null, TO_DATE('2001-04-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), null);