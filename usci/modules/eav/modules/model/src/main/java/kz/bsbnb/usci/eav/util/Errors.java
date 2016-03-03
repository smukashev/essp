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
    E180, E181, E182, E183, E184, E185, E186, E187, E188, E189, E190, E191, E192, E193, E194, E195, E196, E197, E198, E199;

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
        errors.put("E62", "Запись класса #metaclass не найдена; #baseEntity");
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
        errors.put("E76", "Ошибка при вставке #e.getMessage() #insertedObject");
        errors.put("E77", "Ошибка при обновлений #e.getMessage() #updatedObject");
        errors.put("E78", "Ошибка при удалений #e.getMessage() #deletedObject");
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
        errors.put("E103", "Запись #id не действует до отчётного периода #reportDate");
        errors.put("E104", "Нельзя обьединять сущности разных банков");
        errors.put("E105", "Для слияние двух объектов BaseEntity необходимо существование обоих объектов в БД");//Merging two BaseEntity objects requires for both objects to exits in DB
        errors.put("E106", "Невозможно обработать sets после операции слияния");//Can't process sets after MERGE operation
        errors.put("E107", "Два объекта BaseValue может быть в паре только один раз");//Two BaseValue objects can be paired only once
        errors.put("E108", "Неверная структура MergeManager-а");//Invalid structure of MergeManager
        errors.put("E109", "Невозможно удалить сущность #metaclass (id: #id ) используется в классах: #sbUsages");

        errors.put("E110", "Невозмозжно удалить кредитор у которго есть связки с пользователями (id: #id )");
        errors.put("E111", "Кредитор не найден #creditor");
        errors.put("E112", "Сущность для удаления не найдена #baseEntity");
        errors.put("E113", "Справочник с историей не может быть удалена #baseEntity");
        errors.put("E114", "Сущность для закрытия не найдена #baseEntity");
        errors.put("E115", "Дата закрытия не может быть одинаковой с датой открытия #baseEntity");
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
        errors.put("E175", "Найдено более одного документа #entity");
        errors.put("E176", "Метакласс не может быть NULL");
        errors.put("E177", "Ключевой атрибут( #name ) не может быть пустым. Родитель: #metaclass ;");
        errors.put("E178", "Массив должен содержать элементы( #metaclass );");
        errors.put("E179", "Простой массив не может быть ключевым( #metaclass );");

        errors.put("E180", "Неудается найти конфигурационный файл БД #schema");
        errors.put("E181", "Проблемы с очередью: #e.message()");
        errors.put("E182", "ОС не поддерживается");//OS is not support
        errors.put("E183", "Мета класс для оптимизаций не найден; #iBaseEntity");
        errors.put("E184", "Документ не содержит обязательные поля; #iBaseEntity ");
        errors.put("E185", "Кредитор не найден в справочнике; #iBaseEntity");
        errors.put("E186", "Тип документа не найден; #iBaseEntity");
        errors.put("E187", "Договор не содержит обязательные поля; #iBaseEntity");
        errors.put("E188", "Ключевое поле docs пустое; #iBaseEntity");
        errors.put("E189", "Субъект должен иметь идентификационные документы; #iBaseEntity");

        errors.put("E190", "Тип данных не определён #dataTypes");
        errors.put("E191", "ZIP-файл не содержит каких-либо файлов");//ZIP file does not contain any files
        errors.put("E192", "Sync тайм-аут в reader-е");//Sync timeout in reader
        errors.put("E193", "Ошибка при проверки XML");//XML validation error
        errors.put("E194", "Ошибка преобразования класса: #localName , текст исключении :  #e.getMessage()");//Cast error: #localName , exception text:  #e.getMessage()

        errors.put("E195", "Ошибка бизнес правил #e.getMessage");
        errors.put("E196", "Запись найдена в базе( #id ). Вставка не произведена;");
        errors.put("E197", "Кредитор установлен не правильно;");
        errors.put("E198", "Запись не найдена в базе. Обновление не выполнено;");
        errors.put("E199", "Ошибка при обработке описания протокола;");
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
        String[] paramArr = message.split(Errors.SEPARATOR);
        String error = Errors.getError(paramArr[0]);
        List<String> params = Arrays.asList(Arrays.copyOfRange(paramArr, 1, paramArr.length));

        Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(error);
        List<String> matches = new ArrayList<String>();
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
}
