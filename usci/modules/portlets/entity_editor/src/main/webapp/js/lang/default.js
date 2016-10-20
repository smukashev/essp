var label_SEND = 'Отправить';
var label_EDIT = 'Редактировать';
var label_DATE = 'Дата';
var label_CODE = 'Код';
var label_TITLE = 'Наименование';
var label_ITEMS = 'Элементы';
var label_ERROR = 'Ошибка';
var label_ERROR_NO_DATA = 'Не возможно получить данные';
var label_VIEW = 'Просмотр';
var label_ERROR_NO_DATA_FOR = 'Не возможно получить данные: {0} ';
var label_INFO = 'Информация';
var label_SAVE = 'Сохранить';
var label_CANCEL = 'Отмена';
var label_VALUE = 'Значение';
var label_SIMPLE = 'Простой';
var label_ARRAY = 'Массив';
var label_TYPE = 'Тип';
var label_INPUT_FORM = "Форма ввода";
var label_REF = 'Справочник';
var label_ITEMS = 'Элементы';
var label_ENTITY_ID = 'Идентификатор сущности';
var label_DEL = 'Удалить';
var label_CLOSE = 'Закрыть';
var label_CLASS = 'Класс';
var LABEL_UPDATE = 'Обновить';
var label_date = 'Дата';
var label_CONFIRM_CHANGES = "Подтвердить изменения";
var label_REQUIRED_FIELD = "Обязательное поле";
var label_SUBJECT_NAME = "Быстрый просмотр";
var label_LOADING = "Идет загрузка...";

Date.dayNames = [
    'Воскресенье',
    'Понедельник',
    'Вторник',
    'Среда',
    'Четверг',
    'Пятница',
    'Суббота'
];
Date.monthNames = [
    'Январь',
    'Февраль',
    'Март',
    'Апрель',
    'Май',
    'Июнь',
    'Июль',
    'Август',
    'Сентябрь',
    'Октябрь',
    'Ноябрь',
    'Декабрь'
];

Ext.Date.monthNames = Date.monthNames;
Ext.Date.dayNames = Date.dayNames;

Ext.apply(Ext.DatePicker.prototype, {
    todayText          : "Сегодня",
    minText            : "Эта дата раньше минимальной даты",
    maxText            : "Эта дата позже максимальной даты",
    disabledDaysText   : "",
    disabledDatesText  : "",
    monthNames         : Date.monthNames,
    dayNames           : Date.dayNames,
    nextText           : 'Следующий месяц (Control+Вправо)',
    prevText           : 'Предыдущий месяц (Control+Влево)',
    monthYearText      : 'Выбор месяца (Control+Вверх/Вниз для выбора года)',
    todayTip           : "{0} (Пробел)",
    format             : "d.m.y",
    okText             : " OK ",
    cancelText         : "Отмена",
    startDay           : 1
});
Ext.apply(Ext.form.DateField.prototype, {
    disabledDaysText  : "Не доступно",
    disabledDatesText : "Не доступно",
    minText           : "Дата в этом поле должна быть позже {0}",
    maxText           : "Дата в этом поле должна быть раньше {0}",
    invalidText       : "{0} не является правильной датой - дата должна быть указана в формате {1}",
    format            : "d.m.y",
    altFormats        : "d.m.y|d/m/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-m-d"
});
