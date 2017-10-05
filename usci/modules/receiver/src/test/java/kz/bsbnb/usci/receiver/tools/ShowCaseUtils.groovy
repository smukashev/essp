package kz.bsbnb.usci.receiver.tools

import kz.bsbnb.usci.core.service.IMetaFactoryService
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass as MC
import kz.bsbnb.usci.eav.showcase.ShowCase as SC
import kz.bsbnb.usci.showcase.service.ShowcaseService
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by emles on 05.10.17
 */
class ShowCaseUtils {

    @Autowired
    private IMetaFactoryService metaFactory

    @Autowired
    private ShowcaseService showcaseService

    enum ColumnType {

    }

    class Column {

        String type
        String name

        String attributePath
        Long attributeId

    }

    class Key extends Column {
        Table sourceTable
        Key sourceColumn
    }

    class Table {

        String name

        Boolean searchable
        Boolean parentKey
        Boolean finaI
        Boolean child

        List<Key> rootKeys
        List<Key> foreignKeys

        List<Column> columns

        Table() {
            super()
        }

        Table(SC showCase) {

            this()

            this.name = showCase.tableName

            this.searchable = showCase.actualMeta.searchable
            this.parentKey = showCase.actualMeta.parentIsKey
            this.finaI = showCase.final
            this.child = showCase.child

            /*
            *
            * Общее для всех:
            *
            *   attributeId:    Можно будет использовать для поиска класса ключа
            *                   Может быть null или 0
            *   columnName:     Имя столбца
            *                   Если ключь, то возможно имя столбца есть имя класса + "_id" или просто имя класса
            *   attributePath:  Можно будет использовать для поиска класса ключа (?)
            *                   root - ключь по родителю
            *                   Может быть равен columnName
            *                   Если другое - класс владелец поля (?)
            *                   Приоритет на customFieldsList, если там нет родителя, то в fieldsList
            *
            * fieldsList - все поля (могут быть и некоторые ключи)
            *
            * customFieldsList - все ключи, не всегда поподают из historyKeyFieldsList и rootKeyFieldsList (?)
            *
            * historyKeyFieldsList - Ключи для идентификации сущности
            *
            * rootKeyFieldsList - Ключь для идентификации сущности по родителю
            *
            *
            **/



        }

    }

    void genTables() {

        final List<MC> metaClasses = metaFactory.getMetaClasses()

        final List<SC> showCases = showcaseService.list()

    }

}



