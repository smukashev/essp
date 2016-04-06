package kz.bsbnb.usci.eav.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Errors {

    E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E20, E21, E22, E23, E24, E25,
    E26, E27, E28, E29, E30, E31, E32, E33, E34, E35, E36, E37, E38, E39, E41, E43, E44, E45,
    E46, E47, E48, E49, E50, E51, E52, E53, E54, E55, E56, E57, E58, E59, E60, E61, E62, E63, E64, E65,
    E66, E67, E68, E69, E70, E71, E72, E73, E74, E75, E76, E77, E78, E79, E80, E81, E82, E83, E84, E85,
    E86, E87, E88, E89, E90, E91, E92, E93, E94, E95, E96, E97, E98, E99, E100, E101, E102, E103, E104, E105,
    E106, E107, E108, E109, E110, E111, E112, E113, E114, E115, E116, E117, E118, E119, E120, E121, E122, E123,
    E124, E125, E126, E127, E128, E129, E130, E131, E132, E133, E134, E135, E136, E137, E138, E139, E140, E141,
    E142, E143, E144, E145, E146, E147, E148, E149, E150, E151, E152, E153, E154, E155, E156, E157, E158, E159, E160,
    E161, E162, E163, E164, E165, E166, E167, E168, E169, E170, E171, E172, E173, E174, E175, E176, E177, E178, E179,
    E180, E181, E182, E183, E184, E185, E186, E187, E188, E189, E190, E191, E192, E193, E194, E195, E196, E197, E198, E199,
    E200, E201, E202, E203, E204, E205, E206, E207, E208, E209, E210, E211, E212, E213, E214, E215, E216, E217, E218, E219,
    E220, E221, E222, E223, E224, E225, E226, E227, E228, E229, E230, E231, E232, E233, E234, E235, E236, E237, E238, E239,
    E240, E241, E242, E243, E244, E245, E246, E247, E248, E249, E250, E251, E252, E253, E254, E255, E256, E257, E258, E259,
    E260, E261, E262, E263, E264, E265, E266, E267, E268, E269, E270, E271, E272, E273, E274, E275, E276, E277, E278, E279,
    E280, E281, E282, E283, E284, E285, E286, E287, E288, E289, E290, E291, E292, E293, E294, E295, E296, E297, E298, E299,
    E300, E301, E302, E303, E304, E305, E306, E307, E308, E309, E310, E311, E312, E313, E314, E315, E316, E317, E318, E319,
    E320, E321, E322, E323, E324, E325, E326, E327, E328, E329, E330, E331, E332, E333, E334, E335, E336, E337, E338, E339,
    E340, E341, E342, E343, E344, E345, E346, E347, E348, E349, E350, E351, E352, E353, E354, E355, E356, E357, E358, E359,
    ;

    public static final String SEPARATOR = "\\|~~~\\|";
    private static final String LOCALE = "RU";
    private static HashMap<String, String> errors = new HashMap<>();

    static {
        errors.put("E1_RU", "Ключевой атрибут ( #attr ) не может быть пустым");
        errors.put("E1_KZ", "Ключевой атрибут ( #attr ) не может быть пустым");
        errors.put("E1_EN", "Ключевой атрибут ( #attr ) не может быть пустым");

        errors.put("E2", "Не реализован");
        errors.put("E3", "Документ является NULL");
        errors.put("E4", "Тип документка является NULL");
        errors.put("E5", "Вес документа является NULL");
        errors.put("E6", "Сущность не содержит данные из EAV_BE_ENTITY_REPORT_DATES");
        errors.put("E7", "Неизвестный тип данных");
        errors.put("E8", "BaseEntity класс не реализует интерфейс Cloneable");
        errors.put("E9", "Нет такого поля: #fieldname");
        errors.put("E10", "Не можете работать с массивами: #set");
        errors.put("E11", "BaseEntityReportDate является null. Проверить правильность создания объекта.");
        errors.put("E12", "Мета класс #mc не содержит атрибут #att");
        errors.put("E13", "Путь не может иметь промежуточные простые значения"); //Path can't have intermediate simple values
        errors.put("E14", "Функция должна быть указана");
        errors.put("E15", "Нет функций");
        errors.put("E16", "Функциональные дубликаты не правильные: пример {hasDuplicates(subjects)}doc_type.code,date");
        errors.put("E17", "Бизнес правила пока не реализована");
        errors.put("E18", "Не правильная открывающая скобка");//opening bracket not correct
        errors.put("E20", "Только один знак равенства в фильтре и только в фильтре"); //only exactly one equal sign in filter and only in filter
        errors.put("E21", "Знак равенства должна присутствовать после '!'");//equal sign must be present after exlaim
        errors.put("E22", "Не правильные скобки");
        errors.put("E23", "Простые наборы не поддерживается");//Simple sets not supported
        errors.put("E24", "Набор множеств не поддерживается");//Set of sets not supported
        errors.put("E25", "Тип #attribute , не найден в классе: #metaclass");
        errors.put("E26", "Значение не может быть равно null");//Value not be equal to null
        errors.put("E27", "Несоответствие типов в классе: #metaclass . Нужный #expValueClass , получен: #valueClass");
        errors.put("E28", "Не возможно создать BaseEntityReportDate с null датой");
        errors.put("E29", "Отчетная не можеть быть NULL");
        errors.put("E30", "BaseEntityReportDate класс не реализует интерфейс Cloneable");
        errors.put("E31", "BaseSet класс не реализует интерфейс Cloneable");
        errors.put("E32", "Элемент множества не может быть равен null");//Element of the set can not be equal to null
        errors.put("E33", "Комплексный метод был вызван для простого атрибута или массива"); //Get complex attribute method called for simple attribute or array
        errors.put("E34", "Ожидалось значение поля"); //Field value expected


        errors.put("E35", "Простой метод был вызван для комплексного атрибута или массива"); //Get simple attribute method called for complex attribute or array
        errors.put("E36", "reportDate является null. Инициализация BaseValue невозможна.");//reportDate is null. Initialization of the BaseValue is not possible
        errors.put("E37", "BaseValue класс не реализует интерфейс Cloneable");
        errors.put("E38", "Сравнение значений двух объектов BaseValue без метаданных невозможно");//Comparison values of two instances of BaseValue without meta data is not possible
        errors.put("E39", "Сравнение значений двух объектов BaseValue с значениями null невозможно");//Comparison values of two instances of BaseValue with null values is not possible
        errors.put("E41", "Дата не поддерживается");//DATE is not supported
        errors.put("E43", "Невозможно создать объект BaseValue");//Can not create instance of BaseValue
        errors.put("E44", "Путь не может иметь простые элементы");
        errors.put("E45", "Атрибут: #attribute не найден в мета классе: #metaclass");


        errors.put("E46", "MetaType не может быть null");//MetaType can not be null
        errors.put("E47", "Нет пагинации");//no pagination
        errors.put("E48", "Не поддерживается");//not supported
        errors.put("E49", "Неизвестный тип. Не может быть возвращен соответствующий класс.");//Unknown type. Can not be returned an appropriate class
        errors.put("E50", "customMeta не может быть null");//customMeta can't be null
        errors.put("E51", "#name: Невозможно получить атрибут: #attribute");//#name: Can't get attribute: #attribute
        errors.put("E52", "customMeta не установлен для витрины");//customMeta not set for showcase
        errors.put("E53", "Обьект для удаления не может быть NULL");
        errors.put("E54", "Обьект для вставки не может быть NULL");
        errors.put("E55", "Обьект для обновления не может быть NULL");
        errors.put("E56", "Найденный объект #baseEntityId не имеет отчетный даты");
        errors.put("E57", "Запись с ID #baseEntityId является закрытой с даты #reportDate. Обновление после закрытия сущностей не является возможным");
        errors.put("E58", "Атрибут должен иметь мета данные");
        errors.put("E59", "Родитель атрибута #baseValueSaving.getMetaAttribute().getName() должна быть сущность");
        errors.put("E60", "Атрибут должен содержать мета данные");
        errors.put("E61", "Поддержка массив массивов не реализовано");
        errors.put("E62", "Справочник #metaclass не найдена;");
        errors.put("E63", "В базе нет данных для записи #baseEntityId до отчетной даты(включительно): #reportDate");
        errors.put("E64", "Комплексный элелемент не содержит внутренних элементов #metaclass");
        errors.put("E65", "Оперативные атрибуты могут сожержать только оперативные данные. Мета: #metaclass , атрибут: #attribute");
        errors.put("E66", "Оперативные данные могут быть закрыты только за существующий отчетный период #metaAttribute");
        errors.put("E67", "Оперативные данные выгружены неправильно #metaAttribute");
        errors.put("E68", "Предыдущая запись не была найдена #metaAttribute");
        errors.put("E69", "Оперативные данные #metaAttribute могут изменятся только за существующие периоды");

        errors.put("E70", "Предыдущая запись не найдена #metaAttribute");
        errors.put("E71", "Запись класса #metaAttribute не найдена");
        errors.put("E72", "Дата закрытия атрибута #metaAttribute должна быть больше или равна дате открытия атрибута");
        errors.put("E73", "Запись открытия не была найдена #metaAttribute");
        errors.put("E74", "Last значение выгружено неправильно");
        errors.put("E75", "Закрытие атрибута за прошлый период не является возможным");
        errors.put("E76", "Ошибка при вставке #e_message");
        errors.put("E77", "Ошибка при обновлений #e_message");
        errors.put("E78", "Ошибка при удалений #e_message");
        errors.put("E79", "Удаление затронуло #count записей #id , EAV_BE_BOOLEAN_VALUES");

        errors.put("E80", "Мета данные атрибута не могут быть NULL");
        errors.put("E81", "Мета данные атрибута должны иметь ID больше 0");
        errors.put("E82", "Родитель записи #metaAttribute является NULL");
        errors.put("E83", "Найдено больше одной записи #metaAttribute");

        errors.put("E84", "Обновление затронуло #count записей #id , EAV_BE_BOOLEAN_VALUES");
        errors.put("E85", "Удаление затронуло #count записей #id , EAV_BE_ENTITY_COMPLEX_SETS");
        errors.put("E86", "Обновление затронуло #count записей #id , EAV_BE_ENTITY_COMPLEX_SETS");
        errors.put("E87", "Удаление затронуло #count записей #id , EAV_BE_COMPLEX_VALUES");
        errors.put("E88", "Обновление затронуло #count записей #id , EAV_BE_COMPLEX_VALUES");
        errors.put("E89", "Попытка удалений более 1 записи #id");

        errors.put("E90", "Удаление не произошло #id");
        errors.put("E91", "Найдено более одной записи #id");
        errors.put("E92", "Запись не была найдена #id");
        errors.put("E93", "Запись должна иметь идентификатор");
        errors.put("E94", "Запись должна иметь отчётную дату");
        errors.put("E95", "Удаление затронуло #count записей #id , EAV_BE_DATE_VALUES");
        errors.put("E96", "Обновление затронуло #count записей #id , EAV_BE_DATE_VALUES");
        errors.put("E97", "Удаление затронуло #count записей #id , EAV_BE_DOUBLE_VALUES");
        errors.put("E98", "Обновление затронуло #count записей #id , EAV_BE_DOUBLE_VALUES");
        errors.put("E99", "Удаление затронуло #count записей #id , EAV_BE_INTEGER_VALUES");

        errors.put("E100", "Обновление затронуло #count записей #id , EAV_BE_INTEGER_VALUES");
        errors.put("E101", "В базе отсутсвует отчетная дата на #id");
        errors.put("E102", "Необходимо предоставить ID записи и отчётную дату");
        errors.put("E103", "Справочник #id не доступен на отчётный период #reportDate");
        errors.put("E104", "Нельзя обьединять сущности разных банков");
        errors.put("E105", "Для слияние двух объектов BaseEntity необходимо существование обоих объектов в БД");//Merging two BaseEntity objects requires for both objects to exits in DB
        errors.put("E106", "Невозможно обработать sets после операции слияния");//Can't process sets after MERGE operation
        errors.put("E107", "Два объекта BaseValue может быть в паре только один раз");//Two BaseValue objects can be paired only once
        errors.put("E108", "Неверная структура MergeManager-а");//Invalid structure of MergeManager
        errors.put("E109", "Невозможно удалить сущность #metaclass (id: #id ) используется в классах: #sbUsages");

        errors.put("E110", "Невозмозжно удалить кредитор у которго есть связки с пользователями (id: #id )");
        errors.put("E111", "Кредитор не найден #creditor");
        errors.put("E112", "Сущность для удаления не найдена");
        errors.put("E113", "Справочник с историей не может быть удалена");
        errors.put("E114", "Сущность для закрытия не найдена");
        errors.put("E115", "Дата закрытия не может быть одинаковой с датой открытия");
        errors.put("E116", "Запись была найдена в базе ( #baseEntityId ). Вставка не произведена");
        errors.put("E117", "Запись не была найдена в базе. Обновление не выполнено; ");
        errors.put("E118", "Операция не поддерживается #operation");
        errors.put("E119", "Удаление должно было затронуть одну запись");

        errors.put("E120", "Отсутствует ID. Необходимо указать ID сущности;");
        errors.put("E121", "Отсутствует отчетная дата. Необходимо указать отчетную дату");
        errors.put("E122", "Найдено больше одной записи на одну отчетную дату");
        errors.put("E123", "Отсутствует запись с сущностью( #baseEntityId ) на отчетную дату( #reportDate )");
        errors.put("E124", "Для обновления необходимо предоставить ID");
        errors.put("E125", "Обновление должно было затронуть одну запись");
        errors.put("E126", "Удаление затронуло #count записей #id , EAV_BE_ENTITY_SIMPLE_SETS");
        errors.put("E127", "Неизвестный тип");//Unknown type
        errors.put("E128", "Обновление затронуло #count записей #id , EAV_BE_ENTITY_SIMPLE_SETS");
        errors.put("E129", "Удаление затронуло #count записей #id , EAV_BE_STRING_VALUES");

        errors.put("E130", "Обновление затронуло #count записей #id , EAV_BE_STRING_VALUES");
        errors.put("E131", "Удаление затронуло #count записей #id , EAV_BE_BOOLEAN_SET_VALUES");
        errors.put("E132", "Обновление затронуло #count записей #id , EAV_BE_BOOLEAN_SET_VALUES");
        errors.put("E133", "Удаление затронуло #count записей #id , EAV_BE_COMPLEX_SET_VALUES");
        errors.put("E134", "Невозможно найти закрытый объект BaseValue без контейнера или контейнер ID");//Can not find closed instance of BaseValue without container or container ID
        errors.put("E135", "Найдено более одной закрытой записи массива #id , #metaclass");
        errors.put("E136", "Найдено более одной следующей записи массива #id , #metaclass");
        errors.put("E137", "Найдено более одной предыдущей записи массива #id , #metaclass");
        errors.put("E138", "Обновление затронуло #count записей #id , EAV_BE_COMPLEX_SET_VALUES");
        errors.put("E139", "Операция Удаление должна удалять только один запись");//DELETE operation should be delete only one record

        errors.put("E140", "Операция Обновление должна обновлять только один запись");//UPDATE operation should be update only one record
        errors.put("E141", "Удаление затронуло #count записей #id , EAV_BE_DATE_SET_VALUES");
        errors.put("E142", "Обновление затронуло #count записей #id , EAV_BE_DATE_SET_VALUES");
        errors.put("E143", "Удаление затронуло #count записей #id , EAV_BE_DOUBLE_SET_VALUES");
        errors.put("E144", "Обновление затронуло #count записей #id , EAV_BE_DOUBLE_SET_VALUES");
        errors.put("E145", "Удаление затронуло #count записей #id , EAV_BE_INTEGER_SET_VALUES");
        errors.put("E146", "Обновление затронуло #count записей #id , EAV_BE_INTEGER_SET_VALUES");
        errors.put("E147", "Удаление затронуло #count записей #id , EAV_BE_STRING_SET_VALUES");
        errors.put("E148", "Обновление затронуло #count записей #id , EAV_BE_STRING_SET_VALUES");
        errors.put("E149", "Найдено более одного пакета. Не удается загрузить.");//More than one batch found. Can't load.

        errors.put("E150", "Пакет не найден. Не удается загрузить.");//Batch not found. Can't load.
        errors.put("E151", "Более одного BatchEntry найдены");//More then one BatchEntry found
        errors.put("E152", "BatchEntry с идентификатором #id не найден");//BatchEntry with identifier #id was not found
        errors.put("E153", "Без идентификатора невозможно удалить BatchEntry");//Can't remove BatchEntry without id
        errors.put("E154", "Операция должна была удалить 1 запись. Было удалено #count  записей");
        errors.put("E155", "Значение не найдено");//value not found
        errors.put("E156", "Операция должна была обновить 1 запись. Былог обновлено #count записей;");
        errors.put("E157", "Обновление затронуло #count записей #id , EAV_OPTIMIZER");
        errors.put("E158", "Мета класс не был создан");
        errors.put("E159", "MetaClass должен иметь идентификатор до удаление обекъекта с БД");//MetaClass must have an id filled before attributes deletion to DB

        errors.put("E160", "Классы не найдены");//Classes not found
        errors.put("E161", "MetaClass должен иметь идентификатор до вставки в БД");//MetaClass must have an id filled before attributes insertion to DB
        errors.put("E162", "Мета класс не имеет имя или идентификатор.Не удается загрузить.");//Meta class does not have name or id. Can't load
        errors.put("E163", "Класс не найден. Невозможно загрузить класс #metaclass");//Class not found. Can't load class #metaclass
        errors.put("E164", "Невозможно загрузить аттрибуты метакласса без ID");//Can't load atributes of metaclass without id
        errors.put("E165", "Невозможно удалить метакласса без ID");//Can't remove MetaClass without id
        errors.put("E166", "Невозможно определить id мета класса");//Can't determine meta id
        errors.put("E167", "#attributeName является массивом,ожидалось одно значение.");//#attributeName is an array, single value expected.
        errors.put("E168", "#attributeName не является массивом");//#attributeName is not an array
        errors.put("E169", "MetaClass должен иметь идентификатор до обновление объекта в БД");//MetaClass must have an id filled before attributes update in DB

        errors.put("E170", "MetaClass должен иметь ID до обновление");//MetaClass must have id to be updated
        errors.put("E171", "Повторяющиеся идентификаторы в report_message или в report_message_attachment");//Duplicate ids in report_message or report_message_attachment
        errors.put("E172", "Persistable класс не может быть null");//Persistable class can not be null
        errors.put("E173", "Не найдено соответствующий интерфейс для persistable класса #metaclass");//Not found appropriate interface for persistable class #metaclass
        errors.put("E174", "Найдено более одного договора");
        errors.put("E175", "Найдено более одного документа");
        errors.put("E176", "Метакласс не может быть NULL");
        errors.put("E177", "Ключевой атрибут( #name ) не может быть пустым. Родитель: #metaclass ;");
        errors.put("E178", "Массив должен содержать элементы( #metaclass );");
        errors.put("E179", "Простой массив не может быть ключевым( #metaclass );");

        errors.put("E180", "Неудается найти конфигурационный файл БД #schema");
        errors.put("E181", "Проблемы с очередью: #e_message");
        errors.put("E182", "ОС не поддерживается");//OS is not support
        errors.put("E183", "Мета класс для оптимизаций не найден;");
        errors.put("E184", "Документ не содержит обязательные поля; ");
        errors.put("E185", "Кредитор не найден в справочнике;");
        errors.put("E186", "Тип документа не найден;");
        errors.put("E187", "Договор не содержит обязательные поля;");
        errors.put("E188", "Ключевое поле docs пустое;");
        errors.put("E189", "Субъект должен иметь идентификационные документы;");

        errors.put("E190", "Тип данных не определён #dataTypes");
        errors.put("E191", "ZIP-файл не содержит каких-либо файлов");//ZIP file does not contain any files
        errors.put("E192", "Sync тайм-аут в reader-е");//Sync timeout in reader
        errors.put("E193", "Ошибка при проверки XML");//XML validation error
        errors.put("E194", "Ошибка преобразования класса: #localName , текст исключении :  #e_message");//Cast error: #localName , exception text:  #e_message

        errors.put("E195", "Ошибка бизнес правил #e_message");
        errors.put("E196", "Запись найдена в базе( #id ). Вставка не произведена;");
        errors.put("E197", "Кредитор установлен не правильно;");
        errors.put("E198", "Запись не найдена в базе. Обновление не выполнено;");
        errors.put("E199", "Ошибка при обработке описания протокола;");
        errors.put("E200", "Параметр <Liferay пользователя> не может быть null;");//Parameter <liferayUser> can not be null;
        errors.put("E201", "Не удалось получить ответ. #e_message"); //Failed to consume response. #e_message
        errors.put("E202", "Ошибка : HTTP код ошибки : #statusCode : #reasonPhrase");//Failed : HTTP error code : #statusCode : #reasonPhrase
        errors.put("E203", "Возможно Bonita не запущен, или URL является недействительным. Пожалуйста, проверьте имя хоста и номер порта. Используемый URL : #BONITA_URI , #e_message"); //Bonita bundle may not have been started, or the URL is invalid. Please verify hostname and port number. URL used is: #BONITA_URI , #e_message
        errors.put("E204", "Первичная отчетнная дата неправильно отформатирована");//Initial report date is incorrectly formatted
        errors.put("E205", "Количество Отчет > 1");//Reports size > 1
        errors.put("E206", "Пока не поддерживается.");//Not supported yet.
        errors.put("E207", "Поток схемы не может быть пустым");//Schema stream can't be null
        errors.put("E208", "Невозможно открыть схему");//Can't open schema
        errors.put("E209", "Не удается преобразовать #attribute в Meta Value: неизвестный simple type"); //Can't convert #attribute to MetaValue: unknown simple type
        errors.put("E210", "Тип не сложный или простой"); //Type is not complex or simple
        errors.put("E211", "Не удалось проверить последнюю ошибку"); //Failed last exception check
        errors.put("E212", "Разрешенные операции рефов [import] [filename]"); //Allowed operations refs [import] [filename]
        errors.put("E213", "Заглавие должно соответствовать формату: <name>");//Title must be specified format title: <name>
        errors.put("E214", "Правило не должно быть пустым");//Rule must not be empty
        errors.put("E215", "Набор витрин [meta,name,tableName,downPath] {value}");//showcase set [meta,name,tableName,downPath] {value}"
        errors.put("E216", "Не сушествунет путь для downPath: #path"); //No such path for downPath: #path
        errors.put("E217", "Путь аттрибута м имя столбца не может быть пустым");//AttributePath and columnName cannot be empty
        errors.put("E218", "Метакласс, путь аттрибута м имя столбца не может быть пустым");//MetaClass, attributePath and columnName cannot be empty
        errors.put("E219", "Аргументы: витрина [status, set]"); //Arguments: showcase [status, set]
        errors.put("E220", "Неподготовленный поток");//Unprepared thread
        errors.put("E221", "IMetaClassRepository не может быть null.");//Instance of IMetaClassRepository can not be null
        errors.put("E222", "Вызов процедуры тайм-аута исключения");//Вызов процедуры тайм-аута исключения
        errors.put("E223", "Должен быть каталог");//Must be directory
        errors.put("E224", "Начало формата не правильный");//Start format not correct
        errors.put("E225", "Конец формата не правильный");//End format not correct
        errors.put("E226", "Дан слишком длительный период");//Too long period given
        errors.put("E227", "Badrt not searchable!!!");//Badrt not searchable!!!
        errors.put("E228", "DocType с кодом #code не найдены");//DocType with code #code not found
        errors.put("E229", "Поиск не найден");//SearcherNotFound
        errors.put("E230", "Форма поиска не найдена");//Searcher form not found
        errors.put("E231", "Неправильное использование");//Incorrect use
        errors.put("E232", "RepDate, creditorId необходимы для пути кредитор!");//repDate, creditorId are required for creditor dir path!
        errors.put("E233", "RepDate, creditorId и хэш необходимы для пути к файлу!");//repDate, creditorId and hash are required for file path!
        errors.put("E234", "Запись не была найдена в базе");
        errors.put("E235", "Невозможно разобрать начальную дату отчета.");//Unable to parse the initial report date.
        errors.put("E236", "Нет кредитор");//No creditor
        errors.put("E237", "Первая дата должна быть меньше, чем вторая");//First date should be less than the second
        errors.put("E238", "Нет прав для просмотра");
        errors.put("E239", "Нет прав");//no.any.rights
        errors.put("E240", "Доступ к более одному банку");
        errors.put("E241", "Нет доступа к кредиторам");
        errors.put("E242", "Ошибка сериализации");
        errors.put("E243", "Мета является null");//Meta is null
        errors.put("E244", "Услуги является null");//Services are null
        errors.put("E245", "Тип является null");//Type is null
        errors.put("E246", "Невозможно разобрать значение конфигурации #LAST_MAIL_HANDLER_LAUNCH_TIME_CODE #e_message");//Couldn't parse #LAST_MAIL_HANDLER_LAUNCH_TIME_CODE configuration value #e_message
        errors.put("E247", "Shared не может быть null");//Shared can not be null
        errors.put("E248", "Не удалось найти кодировку #e_message");//Couldn't find encoding #e_message
        errors.put("E249", "Null неправильное значение для свойств массива"); //Null is illegal value for properties array
        errors.put("E250", "Null неправильное значение длины имен свойств");//Null is illegal value for property names length
        errors.put("E251", "Свойства и имена свойств должны содержать одинаковое количество элементов");//Properties and property names should contain equal number of elements
        errors.put("E252", "I/O exception #e_message");
        errors.put("E253", "Parse error #e_message");
        errors.put("E254", "Коллекции должны быть одинакового размера");//Collections should be of equal size
        errors.put("E255", "Null параметры");//null parameters
        errors.put("E256", "Нет больше записей");//No more records
        errors.put("E257", "Нет такого поля #filed");//No such field #filed
        errors.put("E258", "Сообщение об ошибке");//some error Message
        errors.put("E259", "Не имеет id. Не возможно загружать.");//Does not have id. Can't load.
        errors.put("E260", "Дата отчета должна быть присвоена до сохранени Batch-а в базу данных");//Report date must be set before instance of Batch saving to the DB.
        errors.put("E261", "Batch не имеет id. Невозможно создать batch версию");//Batch does not have id. Can't create batch version
        errors.put("E262", "Batch id не может быть null");//Пакетная идентификатор не может быть пустым
        errors.put("E263", "Версия пакета должна быть датой позднее");
        errors.put("E264", "Несколько правил с одимаковым id");//Several rules with same id
        errors.put("E265", "Batch версия не имеет id.");//Batchversion has no id
        errors.put("E266", "Правило не имеет id.");//Rule has no id.
        errors.put("E267", "Неправильный вызов метода");//Non proper method call
        errors.put("E268", "Нет такой пакет : #pkgName");//No such package : #pkgName
        errors.put("E269", "Пакет #pkgName не имеет информации версии!");//Package #pkgName has no versions information
        errors.put("E270", "поисковой ключ не задан");
        errors.put("E271", "Необходимо создать витрины;");
        errors.put("E272", "Произвольный массив не поддерживается!");
        errors.put("E273", "Ключи являются null!");//Keys are null
        errors.put("E274", "Комплексный элемент не может содержать комплексный массив");
        errors.put("E275", "Витрина не может содержать множество столбцов");//showCase can't contain set columns
        errors.put("E276", "Неизвестный simple тип кода");//Неизвестно простой код типа
        errors.put("E277", "Витрина не может содержать набор столбцов: #type");//showCase can't contain set columns:  #type
        errors.put("E278", "Запрос в витрины возвратил больше одной записи");
        errors.put("E279", "Витрина не найден.");//showCase not found.
        errors.put("E280", "ВАЖНЫЙ: Запись не найдена.");//CRITICAL: Entity not found.
        errors.put("E281", "Тип не поддерживается #type");
        errors.put("E282", "Класс не найден : #metaClassName");//No such class : #metaClassName
        errors.put("E283", "Метакласс не содержит атрибутов");//MetaClass with no members
        errors.put("E284", "Неизвестный тип;");
        errors.put("E285", "Ключевые простые массивы не поддерживются;");
        errors.put("E286", "Не получилось отправить в витрину;");
        errors.put("E287", "");
        errors.put("E288", "");
        errors.put("E289", "");
        errors.put("E290", "");
        errors.put("E291", "");
        errors.put("E292", "");
        errors.put("E293", "");
        errors.put("E294", "");
        errors.put("E295", "");
        errors.put("E296", "");
        errors.put("E297", "");
        errors.put("E298", "");
        errors.put("E299", "");
        errors.put("E300", "");
        errors.put("E301", "");
        errors.put("E302", "");
        errors.put("E303", "");
        errors.put("E304", "");
        errors.put("E305", "");
        errors.put("E306", "");
        errors.put("E307", "");
        errors.put("E308", "");
        errors.put("E309", "");
        errors.put("E310", "");
        errors.put("E311", "");
        errors.put("E312", "");
        errors.put("E313", "");
        errors.put("E314", "");
        errors.put("E315", "");
        errors.put("E316", "");
        errors.put("E317", "");
        errors.put("E318", "");
        errors.put("E319", "");
        errors.put("E320", "");
        errors.put("E321", "");
        errors.put("E322", "");
        errors.put("E323", "");
        errors.put("E324", "");
        errors.put("E325", "");
        errors.put("E326", "");
        errors.put("E327", "");
        errors.put("E328", "");
        errors.put("E329", "");
        errors.put("E330", "");
        errors.put("E331", "");
        errors.put("E332", "");
        errors.put("E333", "");
        errors.put("E334", "");
        errors.put("E335", "");
        errors.put("E336", "");
        errors.put("E337", "");
        errors.put("E338", "");
        errors.put("E339", "");
        errors.put("E300", "");
        errors.put("E340", "");
        errors.put("E341", "");
        errors.put("E342", "");
        errors.put("E343", "");
        errors.put("E344", "");
        errors.put("E345", "");
        errors.put("E346", "");
        errors.put("E347", "");
        errors.put("E348", "");
        errors.put("E349", "");
        errors.put("E350", "");



    }

    public static String getError(String code) {
        if (errors.get(code + "_" + LOCALE) == null)
            return errors.get(code + "");
        return errors.get(code + "_" + LOCALE);
    }

    public static String getMessage(Enum error, Object... params) {
        String message = String.valueOf(error);
        for (Object obj : params) {
            if (obj instanceof String && String.valueOf(obj).length() > 255) {
                obj = String.valueOf(obj).substring(0, 255);
            }
            message += "|~~~|" + obj;
        }
        return message;
    }

    public static String unmarshall(String message) {
        if (message == null) return null;

        String[] paramArr = message.split(Errors.SEPARATOR);
        String error = Errors.getError(paramArr[0]);
        List<String> params = Arrays.asList(Arrays.copyOfRange(paramArr, 1, paramArr.length));

        if (error == null) // DT:checkme!
            return message;

        Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(error);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add("#" + matcher.group(1));
        }

        for (int i = 0; i < params.size(); i++) {
            try {
                error = error.replaceFirst(matches.get(i), params.get(i));
            } catch (Exception ex) {
                throw new RuntimeException(getMessage(E199));
            }
        }
        return error;
    }

    public static String checkLength(String message){
        if(message != null && message.length()> 255){
            message = message.substring(0,255);
        }
        return message;
    }
}
