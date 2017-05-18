package kz.bsbnb.usci.eav.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    E340, E341, E342, E343, E344, E345, E346, E347, E348, E349, E350, E351, E352, E353, E354, E355, E356, E357, E358, E359,;

    public static final String SEPARATOR = "\\|~~~\\|";
    private static final String LOCALE = "RU";
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private static HashMap<String, Error> errors = new HashMap<>();

    static {
        errors.put("E1", new Error("Атрибут не задан", "Ключевой атрибут (#attr) не может быть пустым"));
        errors.put("E2", new Error("Не реализован", "Не реализован"));
        errors.put("E3", new Error("Пустой документ", "Документ является NULL"));
        errors.put("E4", new Error("Неверный тип документа", "Тип документка является NULL"));
        errors.put("E5", new Error("Неверный вес документа", "Вес документа является NULL"));
        errors.put("E6", new Error("Сущность не найдена", "Сущность не содержит данные из EAV_BE_ENTITY_REPORT_DATES"));
        errors.put("E7", new Error("Тип не найден", "Неизвестный тип данных"));
        errors.put("E8", new Error("Ошибка наследования", "BaseEntity класс не реализует интерфейс Cloneable"));
        errors.put("E9", new Error("Поле не найдено", "Нет такого поля: #fieldname"));
        errors.put("E10", new Error("Инвалидный массив", "Не можете работать с массивами: #set"));
        errors.put("E11", new Error("Некоррекный обьект", "BaseEntityReportDate является null. Проверить правильность создания объекта."));
        errors.put("E12", new Error("Некоррекный метакласс", "Мета класс #mc не содержит атрибут #att"));
        errors.put("E13", new Error("Неправильный путь", "Путь не может иметь промежуточные простые значения"));
        errors.put("E14", new Error("Функция не задана", "Функция должна быть указана"));
        errors.put("E15", new Error("Нет функций", "Нет функций"));
        errors.put("E16", new Error("Функциональные дубликаты не правильные", "Функциональные дубликаты не правильные: пример {hasDuplicates(subjects)}doc_type.code,date"));
        errors.put("E17", new Error("Бизнес правило не реализовано", "Бизнес правила пока не реализована"));
        errors.put("E18", new Error("Не правильная скобка", "Не правильная открывающая скобка"));
        errors.put("E20", new Error("Не правильный знак равенства", "Только один знак равенства в фильтре и только в фильтре"));
        errors.put("E21", new Error("Нет знака равенства", "Знак равенства должна присутствовать после '!'"));
        errors.put("E22", new Error("Не правильные скобки", "Не правильные скобки"));
        errors.put("E23", new Error("Простые наборы не поддерживается", "Простые наборы не поддерживается"));
        errors.put("E24", new Error("Набор множеств не поддерживается", "Набор множеств не поддерживается"));
        errors.put("E25", new Error("Аттрибут не найден", "Тип #attribute , не найден в классе: #metaclass"));
        errors.put("E26", new Error("Значение не может быть пустым", "Значение не может быть равно null"));
        errors.put("E27", new Error("Несоответствие типов в классе", "Несоответствие типов в классе: #metaclass . Нужный #expValueClass , получен: #valueClass"));
        errors.put("E28", new Error("Не правильная дата", "Не возможно создать BaseEntityReportDate с null датой"));
        errors.put("E29", new Error("Не правильная отчетная", "Отчетная не можеть быть NULL"));
        errors.put("E30", new Error("Не правильная реализация класса", "BaseEntityReportDate класс не реализует интерфейс Cloneable"));
        errors.put("E31", new Error("Не правильная реализация класса", "BaseSet класс не реализует интерфейс Cloneable"));
        errors.put("E32", new Error("Не правильный элемент множества", "Элемент множества не может быть равен null"));
        errors.put("E33", new Error("Не правильный комплексный метод", "Комплексный метод был вызван для простого атрибута или массива"));
        errors.put("E34", new Error("Не правильный формат поля", "Ожидалось значение поля"));
        errors.put("E35", new Error("Не правльный вызов метода", "Простой метод был вызван для комплексного атрибута или массива"));
        errors.put("E36", new Error("Не верный отчетный период", "reportDate является null. Инициализация BaseValue невозможна."));
        errors.put("E37", new Error("Не правильная реализация класса", "BaseValue класс не реализует интерфейс Cloneable"));
        errors.put("E38", new Error("Сравнение объектов невозможно", "Сравнение значений двух объектов BaseValue без метаданных невозможно"));
        errors.put("E39", new Error("Сравнение объектов невозможно", "Сравнение значений двух объектов BaseValue с значениями null невозможно"));
        errors.put("E41", new Error("Некорректная дата", "Дата не поддерживается"));
        errors.put("E43", new Error("Невозможно создать объект", "Невозможно создать объект BaseValue"));
        errors.put("E44", new Error("Неправильный путь", "Путь не может иметь простые элементы"));
        errors.put("E45", new Error("Атрибут не найден", "Атрибут: #attribute не найден в мета классе: #metaclass"));
        errors.put("E46", new Error("Неправильный Метакласс", "MetaType не может быть null"));
        errors.put("E47", new Error("Нет пагинации", "Нет пагинации"));
        errors.put("E48", new Error("Функция не поддерживается", "Не поддерживается"));
        errors.put("E49", new Error("Неизвестный тип", "Неизвестный тип. Не может быть возвращен соответствующий класс."));
        errors.put("E50", new Error("Неправильный объект", "customMeta не может быть null"));
        errors.put("E51", new Error("Невозможно получить атрибут", "#name: Невозможно получить атрибут: #attribute"));
        errors.put("E52", new Error("Объект не установлен", "customMeta не установлен для витрины"));
        errors.put("E53", new Error("Невозможно удалить объект", "Обьект для удаления не может быть NULL"));
        errors.put("E54", new Error("Невозможно вставить объект", "Обьект для вставки не может быть NULL"));
        errors.put("E55", new Error("Невозможно обновить объект", "Обьект для обновления не может быть NULL"));
        errors.put("E56", new Error("Отчетная дата не задана", "Найденный объект #baseEntityId не имеет отчетный даты"));
        errors.put("E57", new Error("Обновление после закрытия сущностей не является возможным", "Запись с ID #baseEntityId является закрытой с даты #reportDate. Обновление после закрытия сущностей не является возможным"));
        errors.put("E58", new Error("Нет мета данных", "Атрибут должен иметь мета данные"));
        errors.put("E59", new Error("Неапрвильный родительский объект", "Родитель атрибута #baseValueSaving.getMetaAttribute().getName() должна быть сущность"));
        errors.put("E60", new Error("Нет мета данных", "Атрибут должен содержать мета данные"));
        errors.put("E61", new Error("Поддержка массив массивов не реализовано", "Поддержка массив массивов не реализовано"));
        errors.put("E62", new Error("Не найден метакласс", "не найден в справочнике #metaclass;"));
        errors.put("E63", new Error("Не найден объект", "В базе нет данных для записи #baseEntityId до отчетной даты(включительно): #reportDate"));
        errors.put("E64", new Error("Нет внутренних элементов", "Комплексный элелемент не содержит внутренних элементов #metaclass"));
        errors.put("E65", new Error("Оперативные атрибуты могут сожержать только оперативные данные.", "Оперативные атрибуты могут сожержать только оперативные данные. Мета: #metaclass , атрибут: #attribute"));
        errors.put("E66", new Error("Не правильное закрытие оперативных данных", "Оперативные данные могут быть закрыты только за существующий отчетный период #metaAttribute"));
        errors.put("E67", new Error("Оперативные данные выгружены неправильно", "Оперативные данные выгружены неправильно #metaAttribute"));
        errors.put("E68", new Error("Запись не найдена", "Предыдущая запись не была найдена #metaAttribute"));
        errors.put("E69", new Error("Не правильное изменение", "Оперативные данные #metaAttribute могут изменятся только за существующие периоды"));
        errors.put("E70", new Error("Запись не найдена", "Предыдущая запись не найдена #metaAttribute"));
        errors.put("E71", new Error("Запись не найдена", "Запись класса #metaAttribute не найдена"));
        errors.put("E72", new Error("Дата закрытия атрибута указана не корректно", "Дата закрытия атрибута #metaAttribute должна быть больше или равна дате открытия атрибута"));
        errors.put("E73", new Error("Запись открытия не была найдена", "Запись открытия не была найдена #metaAttribute"));
        errors.put("E74", new Error("Значение выгружено неправильно", "Last значение выгружено неправильно"));
        errors.put("E75", new Error("Закрытие атрибута не возможно", "Закрытие атрибута за прошлый период не является возможным #attribute"));
        errors.put("E76", new Error("Ошибка при вставке", "Ошибка при вставке #insertedObject, #e_message"));
        errors.put("E77", new Error("Ошибка при обновлений", "Ошибка при обновлений #updatedObject, #e_message"));
        errors.put("E78", new Error("Ошибка при удалений", "Ошибка при удалений #deletedObject, #e_message"));
        errors.put("E79", new Error("Не правльное удаление", "Удаление затронуло #count записей #id , EAV_BE_BOOLEAN_VALUES"));
        errors.put("E80", new Error("Не правильный атрибут", "Мета данные атрибута не могут быть NULL"));
        errors.put("E81", new Error("Не правильный атрибут", "Мета данные атрибута должны иметь ID больше 0"));
        errors.put("E82", new Error("Не правильная родительская запись", "Родитель записи #metaAttribute является NULL"));
        errors.put("E83", new Error("Найдено более одной записи", "Найдено больше одной записи #metaAttribute"));
        errors.put("E84", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_BOOLEAN_VALUES"));
        errors.put("E85", new Error("Не правильное количество удалении сущности", "Удаление затронуло #count записей #id , EAV_BE_ENTITY_COMPLEX_SETS"));
        errors.put("E86", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_ENTITY_COMPLEX_SETS"));
        errors.put("E87", new Error("Не правильное количество удалении сущности", "Удаление затронуло #count записей #id , EAV_BE_COMPLEX_VALUES"));
        errors.put("E88", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_COMPLEX_VALUES"));
        errors.put("E89", new Error("Попытка удалений более одной записи", "Попытка удалений более 1 записи #id"));
        errors.put("E90", new Error("Удаление не произошло", "Удаление не произошло #id"));
        errors.put("E91", new Error("Найдено более одной записи", "Найдено более одной записи #id"));
        errors.put("E92", new Error("Запись не найдена признак Е92", "Запись не была найдена #id признак Е92"));
        errors.put("E93", new Error("Запись должна иметь идентификатор", "Запись должна иметь идентификатор"));
        errors.put("E94", new Error("Запись должна иметь отчётную дату", "Запись должна иметь отчётную дату"));
        errors.put("E95", new Error("Не правильное количество удалении сущности", "Удаление затронуло #count записей #id , EAV_BE_DATE_VALUES"));
        errors.put("E96", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_DATE_VALUES"));
        errors.put("E97", new Error("Не правильное количество удалении сущности", "Удаление затронуло #count записей #id , EAV_BE_DOUBLE_VALUES"));
        errors.put("E98", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_DOUBLE_VALUES"));
        errors.put("E99", new Error("Не правильное количество удалении сущности", "Удаление затронуло #count записей #id , EAV_BE_INTEGER_VALUES"));
        errors.put("E100", new Error("Не правильное количество обновлении сущности", "Обновление затронуло #count записей #id , EAV_BE_INTEGER_VALUES"));
        errors.put("E101", new Error("Отсутсвует отчетная дата", "В базе отсутсвует отчетная дата на #id"));
        errors.put("E102", new Error("Необходимо предоставить ID записи и отчётную дату", "Необходимо предоставить ID записи и отчётную дату"));
        errors.put("E103", new Error("Запись за отчётный период не доступна", "Запись #id не доступен на отчётный период #reportDate"));
        errors.put("E104", new Error("Ошибка объединение", "Нельзя обьединять сущности разных банков"));
        errors.put("E105", new Error("Необходимо существование обоих объектов в БД", "Для слияние двух объектов BaseEntity необходимо существование обоих объектов в БД"));
        errors.put("E106", new Error("Обработка невозможна", "Невозможно обработать sets после операции слияния"));
        errors.put("E107", new Error("Два объекта могут быть в паре только один раз", "Два объекта BaseValue может быть в паре только один раз"));
        errors.put("E108", new Error("Неверная структура", "Неверная структура MergeManager-а"));
        errors.put("E109", new Error("Невозможно удалить сущность", "Невозможно удалить сущность #metaclass (id: #id ) используется в классах: #sbUsages"));
        errors.put("E110", new Error("Невозможно удалить кредитора", "Невозмозжно удалить кредитор у которго есть связки с пользователями (id: #id )"));
        errors.put("E111", new Error("Кредитор не найден", "Кредитор не найден #creditor"));
        errors.put("E112", new Error("Сущность для удаления не найдена", "Сущность для удаления не найдена"));
        errors.put("E113", new Error("Невозможно удалить справочник", "Справочник с историей не может быть удалена"));
        errors.put("E114", new Error("Сущность не найдена", "Сущность для закрытия не найдена"));
        errors.put("E115", new Error("Не правильная дата закрытия", "Дата закрытия не может быть одинаковой или раньше даты открытия"));
        errors.put("E116", new Error("Вставка не произведена", "Запись была найдена в базе ( #baseEntityId ). Вставка не произведена"));
        errors.put("E117", new Error("Обновление не выполнено", "Запись не была найдена в базе. Обновление не выполнено; "));
        errors.put("E118", new Error("Операция не поддерживается", "Операция не поддерживается #operation"));
        errors.put("E119", new Error("Некорректное удаление", "Удаление должно было затронуть одну запись"));
        errors.put("E120", new Error("Отсутствует идентификатор записи", "Отсутствует ID. Необходимо указать ID сущности;"));
        errors.put("E121", new Error("Отсутствует отчетная дата", "Отсутствует отчетная дата. Необходимо указать отчетную дату"));
        errors.put("E122", new Error("Найдено больше одной записи на одну отчетную дату", "Найдено больше одной записи на одну отчетную дату"));
        errors.put("E123", new Error("Отсутствует запись", "Отсутствует запись с сущностью( #baseEntityId ) на отчетную дату( #reportDate )"));
        errors.put("E124", new Error("Для обновления необходимо предоставить ID", "Для обновления необходимо предоставить ID"));
        errors.put("E125", new Error("Некорректное обновление", "Обновление должно было затронуть одну запись"));
        errors.put("E126", new Error("Не корректное удаление , EAV_BE_ENTITY_SIMPLE_SETS", "Удаление затронуло #count записей #id , EAV_BE_ENTITY_SIMPLE_SETS"));
        errors.put("E127", new Error("Неизвестный тип", "Неизвестный тип"));
        errors.put("E128", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_ENTITY_SIMPLE_SETS"));
        errors.put("E129", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_STRING_VALUES"));
        errors.put("E130", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_STRING_VALUES"));
        errors.put("E131", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_BOOLEAN_SET_VALUES"));
        errors.put("E132", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_BOOLEAN_SET_VALUES"));
        errors.put("E133", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_COMPLEX_SET_VALUES"));
        errors.put("E134", new Error("Невозможно найти закрытый объект", "Невозможно найти закрытый объект BaseValue без контейнера или контейнер ID"));
        errors.put("E135", new Error("Найдено более одной закрытой записи массива", "Найдено более одной закрытой записи массива #id , #metaclass"));
        errors.put("E136", new Error("Найдено более одной следующей записи массива", "Найдено более одной следующей записи массива #id , #metaclass"));
        errors.put("E137", new Error("Найдено более одной предыдущей записи массива", "Найдено более одной предыдущей записи массива #id , #metaclass"));
        errors.put("E138", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_COMPLEX_SET_VALUES"));
        errors.put("E139", new Error("Не правильное удаление", "Операция Удаление должна удалять только один запись"));
        errors.put("E140", new Error("Не правильное операция обновления", "Операция Обновление должна обновлять только один запись"));
        errors.put("E141", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_DATE_SET_VALUES"));
        errors.put("E142", new Error("Не корректное обновление", "Обновление затронуло #count записей #id , EAV_BE_DATE_SET_VALUES"));
        errors.put("E143", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_DOUBLE_SET_VALUES"));
        errors.put("E144", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_DOUBLE_SET_VALUES"));
        errors.put("E145", new Error("Не корректное удаление", "Удаление затронуло #count записей #id , EAV_BE_INTEGER_SET_VALUES"));
        errors.put("E146", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_INTEGER_SET_VALUES"));
        errors.put("E147", new Error("Не корректное удаление , EAV_BE_STRING_SET_VALUES", "Удаление затронуло #count записей #id , EAV_BE_STRING_SET_VALUES"));
        errors.put("E148", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_BE_STRING_SET_VALUES"));
        errors.put("E149", new Error("Не удается загрузить пакет", "Найдено более одного пакета. Не удается загрузить."));
        errors.put("E150", new Error("Пакет не найден", "Пакет не найден. Не удается загрузить."));
        errors.put("E151", new Error("Найдено более одного объекта", "Более одного BatchEntry найдены"));
        errors.put("E152", new Error("Объект не найден", "BatchEntry с идентификатором #id не найден"));
        errors.put("E153", new Error("Невозможно удалить объект", "Без идентификатора невозможно удалить BatchEntry"));
        errors.put("E154", new Error("Не корректное удаление", "Операция должна была удалить 1 запись. Было удалено #count  записей"));
        errors.put("E155", new Error("Значение не найдено", "Значение не найдено"));
        errors.put("E156", new Error("Не корректное обновление", "Операция должна была обновить 1 запись. Былог обновлено #count записей;"));
        errors.put("E157", new Error("Не правильное количество обновления", "Обновление затронуло #count записей #id , EAV_OPTIMIZER"));
        errors.put("E158", new Error("Метакласс не был создан", "Мета класс не был создан"));
        errors.put("E159", new Error("Не правильный метакласс", "MetaClass должен иметь идентификатор до удаление обекъекта с БД"));
        errors.put("E160", new Error("Классы не найдены", "Классы не найдены"));
        errors.put("E161", new Error("Не правильный метакласс", "MetaClass должен иметь идентификатор до вставки в БД"));
        errors.put("E162", new Error("Метакласс не имеет имя или идентификатор", "Мета класс не имеет имя или идентификатор.Не удается загрузить."));
        errors.put("E163", new Error("Класс не найден", "Класс не найден. Невозможно загрузить класс #metaclass"));
        errors.put("E164", new Error("Невозможно загрузить аттрибуты метакласса", "Невозможно загрузить аттрибуты метакласса без ID"));
        errors.put("E165", new Error("Невозможно удалить метакласс", "Невозможно удалить метакласса без ID"));
        errors.put("E166", new Error("Невозможно определить id мета класса", "Невозможно определить id мета класса"));
        errors.put("E167", new Error("Ожидалось одно значение объекта", "#attributeName является массивом,ожидалось одно значение."));
        errors.put("E168", new Error("Аттрибут не является массивом", "#attributeName не является массивом"));
        errors.put("E169", new Error("Не правильный метакласс", "MetaClass должен иметь идентификатор до обновление объекта в БД"));
        errors.put("E170", new Error("Не правильный метакласс", "MetaClass должен иметь ID до обновление"));
        errors.put("E171", new Error("Повторяющиеся идентификаторы", "Повторяющиеся идентификаторы в report_message или в report_message_attachment"));
        errors.put("E172", new Error("Объект не может быть пустым", "Persistable класс не может быть null"));
        errors.put("E173", new Error("Не найден соответствующий интерфейс", "Не найдено соответствующий интерфейс для persistable класса #metaclass"));
        errors.put("E174", new Error("Найдено более одного договора", "Найдено более одного договора"));
        errors.put("E175", new Error("Найдено более одного документа", "Найдено более одного документа"));
        errors.put("E176", new Error("Метакласс не может быть пустым", "Метакласс не может быть NULL"));
        errors.put("E177", new Error("Ключевой атрибут не может быть пустым", "Ключевой атрибут( #name ) не может быть пустым. Родитель: #metaclass ;"));
        errors.put("E178", new Error("Не правильный массив", "Массив должен содержать элементы( #metaclass );"));
        errors.put("E179", new Error("Простой массив не может быть ключевым", "Простой массив не может быть ключевым( #metaclass );"));
        errors.put("E180", new Error("Неудается найти конфигурационный файл в БД", "Неудается найти конфигурационный файл БД #schema"));
        errors.put("E181", new Error("Проблемы с очередью", "Проблемы с очередью: #e_message"));
        errors.put("E182", new Error("ОС не поддерживается", "ОС не поддерживается"));
        errors.put("E183", new Error("Метакласс не найден", "Мета класс для оптимизаций не найден;"));
        errors.put("E184", new Error("Документ не содержит обязательные поля", "Документ не содержит обязательные поля; "));
        errors.put("E185", new Error("Кредитор не найден", "Кредитор не найден в справочнике;"));
        errors.put("E186", new Error("Тип документа не найден", "Тип документа(#type) не найден;"));
        errors.put("E187", new Error("Договор не содержит обязательные поля", "Договор не содержит обязательные поля;"));
        errors.put("E188", new Error("Ключевое поле пустое", "Ключевое поле docs пустое;"));
        errors.put("E189", new Error("Субъект должен иметь идентификационные документы", "Субъект должен иметь идентификационные документы;"));
        errors.put("E190", new Error("Тип данных не определён", "Тип данных не определён #dataTypes"));
        errors.put("E191", new Error("ZIP-файл не содержит каких-либо файлов", "ZIP-файл не содержит каких-либо файлов"));
        errors.put("E192", new Error("Sync тайм-аут в reader-е", "Sync тайм-аут в reader-е"));
        errors.put("E193", new Error("Ошибка при проверки XML", "Ошибка при проверки XML"));
        errors.put("E194", new Error("Ошибка преобразования класса", "Ошибка преобразования класса: #localName , текст исключении :  #e_message"));
        errors.put("E195", new Error("Ошибка бизнес правил", "Ошибка бизнес правил #e_message"));
        errors.put("E196", new Error("Объект существует в базе", "Запись найдена в базе( #id ). Вставка не произведена;"));
        errors.put("E197", new Error("Кредитор установлен не правильно", "Кредитор установлен не правильно;"));
        errors.put("E198", new Error("Обновление не выполнено", "Запись не найдена в базе. Обновление не выполнено;"));
        errors.put("E199", new Error("Ошибка при обработке", "Ошибка при обработке описания протокола;"));
        errors.put("E200", new Error("Liferay пользователя не может быть пустым", "Параметр <Liferay пользователя> не может быть null;"));
        errors.put("E201", new Error("Не правильный запрос", "Не удалось получить ответ. #e_message"));
        errors.put("E202", new Error("Ошибка HTTP запроса", "Ошибка : HTTP код ошибки : #statusCode : #reasonPhrase"));
        errors.put("E203", new Error("Проблема с BPM", "Возможно Bonita не запущен, или URL является недействительным. Пожалуйста, проверьте имя хоста и номер порта. Используемый URL : #BONITA_URI , #e_message"));
        errors.put("E204", new Error("Неправильная первичная отчетнная дата", "Первичная отчетнная дата неправильно отформатирована"));
        errors.put("E205", new Error("Не правильный отчет", "Количество Отчет > 1"));
        errors.put("E206", new Error("Функция не поддерживается.", "Пока не поддерживается."));
        errors.put("E207", new Error("Поток схемы не может быть пустым", "Поток схемы не может быть пустым"));
        errors.put("E208", new Error("Невозможно открыть схему", "Невозможно открыть схему"));
        errors.put("E209", new Error("Не удается преобразовать аттрибута", "Не удается преобразовать #attribute в Meta Value: неизвестный simple type"));
        errors.put("E210", new Error("Неправильный тип", "Тип не сложный или простой"));
        errors.put("E211", new Error("Не удалось проверить последнюю ошибку", "Не удалось проверить последнюю ошибку"));
        errors.put("E212", new Error("Разрешенные операции рефов", "Разрешенные операции рефов [import] [filename]"));
        errors.put("E213", new Error("Заглавие должно соответствовать формату", "Заглавие должно соответствовать формату: <name>"));
        errors.put("E214", new Error("Правило не должно быть пустым", "Правило не должно быть пустым"));
        errors.put("E215", new Error("Не правильный набор витрин", "Набор витрин [meta,name,tableName,downPath] {value}"));
        errors.put("E216", new Error("Не сушествует путь", "Не сушествунет путь для downPath: #path"));
        errors.put("E217", new Error("Не правильный путь", "Путь аттрибута м имя столбца не может быть пустым"));
        errors.put("E218", new Error("Не правильный метакласс", "Метакласс, путь аттрибута м имя столбца не может быть пустым"));
        errors.put("E219", new Error("Не правильные аргументы", "Аргументы: витрина [status, set]"));
        errors.put("E220", new Error("Неподготовленный поток", "Неподготовленный поток"));
        errors.put("E221", new Error("Объект не может быть пустым", "IMetaClassRepository не может быть null."));
        errors.put("E222", new Error("Исключения тайм-аута", "Вызов процедуры тайм-аута исключения"));
        errors.put("E223", new Error("Должен быть каталог", "Должен быть каталог"));
        errors.put("E224", new Error("Не правильное начало формата", "Начало формата не правильный"));
        errors.put("E225", new Error("Не правильный конец формата", "Конец формата не правильный"));
        errors.put("E226", new Error("Дан слишком длительный период", "Дан слишком длительный период"));
        errors.put("E227", new Error("Badrt not searchable!!!", "Badrt not searchable!!!"));
        errors.put("E228", new Error("Объект не найден", "DocType с кодом #code не найдены"));
        errors.put("E229", new Error("Поиск не найден", "Поиск не найден"));
        errors.put("E230", new Error("Форма поиска не найдена", "Форма поиска не найдена"));
        errors.put("E231", new Error("Неправильное использование", "Неправильное использование"));
        errors.put("E232", new Error("Необходимо указать параметры", "RepDate, creditorId необходимы для пути кредитор!"));
        errors.put("E233", new Error("Необходимы указать параметры", "RepDate, creditorId и хэш необходимы для пути к файлу!"));
        errors.put("E234", new Error("Запись в базе не найдена", "Запись не была найдена в базе"));
        errors.put("E235", new Error("Некоррекная начальная дата отчета", "Невозможно разобрать начальную дату отчета."));
        errors.put("E236", new Error("Нет кредитора", "Нет кредитор"));
        errors.put("E237", new Error("Неправильная первая и вторая дата", "Первая дата должна быть меньше, чем вторая"));
        errors.put("E238", new Error("Нет прав для просмотра", "Нет прав для просмотра"));
        errors.put("E239", new Error("Нет прав", "Нет прав"));
        errors.put("E240", new Error("Доступ к более одному банку", "Доступ к более одному банку"));
        errors.put("E241", new Error("Нет доступа к кредиторам", "Нет доступа к кредиторам"));
        errors.put("E242", new Error("Ошибка сериализации", "Ошибка сериализации"));
        errors.put("E243", new Error("Мета является пустым", "Мета является null"));
        errors.put("E244", new Error("Услуги является пустыми", "Услуги является null"));
        errors.put("E245", new Error("Тип является пустым", "Тип является null"));
        errors.put("E246", new Error("Невозможно разобрать значение конфигурации", "Невозможно разобрать значение конфигурации #LAST_MAIL_HANDLER_LAUNCH_TIME_CODE #e_message"));
        errors.put("E247", new Error("Shared не может быть пустым", "Shared не может быть null"));
        errors.put("E248", new Error("Не удалось найти кодировку", "Не удалось найти кодировку #e_message"));
        errors.put("E249", new Error("Cвойств массива не может быть пустым", "Null неправильное значение для свойств массива"));
        errors.put("E250", new Error("Значение длины имен свойства не может быть пустым", "Null неправильное значение длины имен свойств"));
        errors.put("E251", new Error("Неправильные свойства и имена свойства", "Свойства и имена свойств должны содержать одинаковое количество элементов"));
        errors.put("E252", new Error("I/O exception", "I/O exception #e_message"));
        errors.put("E253", new Error("Parse error", "Parse error #e_message"));
        errors.put("E254", new Error("Неправильная коллекция", "Коллекции должны быть одинакового размера"));
        errors.put("E255", new Error("Пустые параметры", "Null параметры"));
        errors.put("E256", new Error("Нет больше записей", "Нет больше записей"));
        errors.put("E257", new Error("Нет такого поля", "Нет такого поля #filed"));
        errors.put("E258", new Error("Сообщение об ошибке", "Сообщение об ошибке"));
        errors.put("E259", new Error("Загрузка не возможна", "Не имеет id. Не возможно загружать."));
        errors.put("E260", new Error("Нет отчетной даты", "Дата отчета должна быть присвоена до сохранени Batch-а в базу данных"));
        errors.put("E261", new Error("Невозможно создать batch версию", "Batch не имеет id. Невозможно создать batch версию"));
        errors.put("E262", new Error("Batch id не может быть пустым", "Batch id не может быть null"));
        errors.put("E263", new Error("Неправильная версия пакета", "Версия пакета должна быть датой позднее"));
        errors.put("E264", new Error("Несколько правил с одинаковым id", "Несколько правил с одимаковым id"));
        errors.put("E265", new Error("Batch версия не имеет id.", "Batch версия не имеет id."));
        errors.put("E266", new Error("Правило не имеет id.", "Правило не имеет id."));
        errors.put("E267", new Error("Неправильный вызов метода", "Неправильный вызов метода"));
        errors.put("E268", new Error("Пакет несуществует", "Нет такой пакет : #pkgName"));
        errors.put("E269", new Error("Пакет не имеет информации о версии!", "Пакет #pkgName не имеет информации версии!"));
        errors.put("E270", new Error("Ключ не задан", "поисковой ключ не задан"));
        errors.put("E271", new Error("Необходимо создать витрины", "Необходимо создать витрины;"));
        errors.put("E272", new Error("Массив не поддерживается", "Произвольный массив не поддерживается!"));
        errors.put("E273", new Error("Ключи являются пустыми", "Ключи являются null!"));
        errors.put("E274", new Error("Неправильный комплексный элемент", "Комплексный элемент не может содержать комплексный массив"));
        errors.put("E275", new Error("Витрина не может содержать множество столбцов", "Витрина не может содержать множество столбцов"));
        errors.put("E276", new Error("Неизвестный тип кода", "Неизвестный simple тип кода"));
        errors.put("E277", new Error("Витрина не может содержать набор столбцов", "Витрина не может содержать набор столбцов: #type"));
        errors.put("E278", new Error("Запрос в витрины возвратил больше одной записи", "Запрос в витрины возвратил больше одной записи"));
        errors.put("E279", new Error("Витрина не найден", "Витрина не найден."));
        errors.put("E280", new Error("Запись не найдена", "ВАЖНЫЙ: Запись не найдена."));
        errors.put("E281", new Error("Тип не поддерживается", "Тип не поддерживается #type"));
        errors.put("E282", new Error("Класс не найден", "Класс не найден : #metaClassName"));
        errors.put("E283", new Error("Метакласс не содержит атрибутов", "Метакласс не содержит атрибутов"));
        errors.put("E284", new Error("Неизвестный тип", "Неизвестный тип;"));
        errors.put("E285", new Error("Ключевые простые массивы не поддерживются", "Ключевые простые массивы не поддерживются;"));
        errors.put("E286", new Error("Невозможно подключиться к сервисам", "Невозможно подключиться к сервисам"));
        errors.put("E287", new Error("Не запущено через портал Liferay!", "Не запущено через портал Liferay!"));
        errors.put("E288", new Error("Загрузка на витрину не доступна", "Загрузка на витрину не доступна"));
        errors.put("E289", new Error("Запись не удалена из очереди", "Запись не удалена из очереди"));
        errors.put("E290", new Error("Не могу применить бизнес правила: #e_message", "Не могу применить бизнес правила: #e_message"));
        errors.put("E291", new Error("Справочник не имеет ID", "Справочник не имеет ID;"));
        errors.put("E292", new Error("Запись не может быть удалена", "Запись не может быть удалена. На запись ссылаются другие объекты"));
        errors.put("E293", new Error("Неправильный субъект", "Субъект может быть физ.лицом, юр.лицом или кредитором"));
        errors.put("E294", new Error("Неверная ссылка на XSD", "Ссылка на XSD файл неверная: #path"));
        errors.put("E295", new Error("Запись не найдена в справочнике", "[#val] не найден в справочнике #metaclass"));
        errors.put("E296", new Error("Удаление невозможно", "Удаление невозможно на дату отличной от отчитываемой, отчитываема дата =  #reportDate,  заявлено = #claimed"));
        errors.put("E297", new Error("Удаление невозможно", "Объект имеет историю отличную от отчетной даты. Удаление невозможно. Дата истории = #hisReportDate"));
        errors.put("E298", new Error("Cправочник закрыт", "Cправочник \"#refName\" c значением \"#val\" является закрытым с даты #closedDate"));
        errors.put("E299", new Error("Закрытие невозможно", "Существует активная запись на отчетную дату #reportDate со значением #value. Закрытие невозможно"));
    }

    public static String getError(Errors code) {
        if (errors.get(code + "_" + LOCALE) == null)
            return errors.get(code + "").description;
        return errors.get(code + "_" + LOCALE).description;
    }


    public static String compose(Errors error, Object... params) {
        String message = String.valueOf(error);
        for (Object obj : params) {

            if (obj instanceof Date) {
                synchronized (Errors.class) {
                    obj = sdf.format(obj);
                }
            }

            if (String.valueOf(obj).length() > 255) {
                obj = String.valueOf(obj).substring(0, 255);
            }
            message += "|~~~|" + obj;
        }
        return message;
    }

    public static String replaceTags(Errors code, Object... params) {
        String error = getError(code);

        Matcher matcher = Pattern.compile("#\\s*(\\w+)")
                .matcher(error);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add("#" + matcher.group(1));
        }

        for (int i = 0; i < params.length; i++) {
            try {
                error = error.replaceFirst(matches.get(i), (String) params[i]);
            } catch (Exception ex) {
                throw new RuntimeException(compose(E199));
            }
        }
        return error;
    }

    public static String decompose(String message) {
        if (message == null) return null;

        String[] paramArr = message.split(Errors.SEPARATOR);

        try {// DT:checkme!
            Errors.valueOf(paramArr[0]);
        } catch (Exception e) {
            return checkLength(message);
        }

        String[] params = new String[paramArr.length - 1];
        System.arraycopy(paramArr, 1, params, 0, params.length);

        return replaceTags(Errors.valueOf(paramArr[0]), params);
    }


    public static String getTitle(String errorCode) {
        return errors.get(errorCode).title;
    }


    public static String checkLength(String message) {
        if (message != null && message.length() > 255) {
            message = message.substring(0, 255);
        }
        return message;
    }


    private static class Error {

        private String title;
        private String description;

        public Error(String title, String description) {

            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}
