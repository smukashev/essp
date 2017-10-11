package kz.bsbnb.usci.receiver.tools

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass as MC
import kz.bsbnb.usci.eav.showcase.ShowCase as SC
import kz.bsbnb.usci.eav.showcase.ShowCaseField
import kz.bsbnb.usci.showcase.service.ShowcaseService
import kz.bsbnb.usci.sync.service.IMetaFactoryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Created by emles on 05.10.17
 */
@Component
class ShowCaseUtils {

    protected Logger logger = LoggerFactory.getLogger(ShowCaseUtils.class)

    @Autowired
    @Qualifier(value = "remoteMetaFactoryService")
    private RmiProxyFactoryBean rmiMetaFactoryService;

    private IMetaFactoryService metaFactory

    @Autowired
    @Qualifier(value = "remoteShowcaseService")
    private RmiProxyFactoryBean rmiShowcaseService;

    private ShowcaseService showcaseService

    List<MC> metaClasses

    List<SC> showCases

    @PostConstruct
    public void init() {

        metaFactory =
                (IMetaFactoryService) rmiMetaFactoryService.getObject();
        showcaseService =
                (ShowcaseService) rmiShowcaseService.getObject();

        metaClasses = metaFactory.getMetaClasses()
        showCases = showcaseService.list()

    }

    private Closure iterateShowCases

    {
        iterateShowCases = { Boolean _return = false, Set<String> set = new TreeSet<>(), List<SC> shCss = showCases, Closure closure ->
            shCss
                    .findAll { SC showCase -> true /*showCase.downPath != null*/ /*showCase.tableName.startsWith("CORE_")*/ }
                    .findAll { SC showCase -> if (set.contains(showCase.name)) return false; else set.add(showCase.name); if (_return) return false; else return true }
                    .each { SC showCase ->

                if (!_return) {
                    _return = closure(showCase)
                    if (true) iterateShowCases(_return, set, showCase.childShowCases, closure)
                }

            }
        }
    }

    ShowCaseUtils() {
        super()
    }

    enum ColumnType {

    }

    class Column {

        String type
        String name

        Column() {
            super()
        }

    }

    class Key extends Column {

        String sourceTable
        String sourceColumn

        Key() {
            super()
        }

    }

    class Table {

        String name

        MetaClass meta

        List<org.jooq.Table> tables = []

        Boolean searchable
        Boolean parentKey
        Boolean finaI
        Boolean child

        List<Key> rootKeys = []
        List<Key> childKeys = []

        List<Column> columns = []

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

            List listRootKeys = []
            List listChildKeys = []
            List listColumnsKeys = []

            def add = { keys, ShowCaseField filed ->
                def className = filed.columnName.replaceAll("_id", "")
                /*def className = (filed.attributePath != null && !filed.attributePath.equals("root")) ?
                        filed.attributePath :
                        filed.columnName.replaceAll("_id", "")*/
                def tableName = ""
                iterateShowCases { SC shCs ->
                    if (shCs.actualMeta.className.equals(className)) {
                        tableName = shCs.tableName
                        return true
                    }
                    return false
                }
                keys.add([attributeId  : filed.attributeId,
                          attributePath: filed.attributePath,
                          type         : filed.type,
                          name         : filed.columnName,
                          className    : className,
                          tableName    : tableName
                ])

            }

            showCase.rootKeyFieldsList.each { filed ->
                add(listRootKeys, filed)
            }

            showCase.customFieldsList
                    .each { filed ->
                listRootKeys.each { key ->
                    if (filed.columnName.equals(key.name)) {
                        key.attributePath = filed.attributePath
                        key.className = filed.attributePath
                        if (filed.attributePath.equals("root")) key.tableName = "root"
                    }
                }
            }

            showCase.customFieldsList
                    .findAll { custom -> !listRootKeys.find { root -> custom.columnName.equals(root.name) } }
                    .each { filed ->
                add(listChildKeys, filed)
            }

            showCase.fieldsList.each { filed ->
                add(listColumnsKeys, filed)
            }

            listRootKeys.each { filed ->
                rootKeys.add Key.newInstance(type: filed.type, name: filed.name, sourceTable: filed.tableName, sourceColumn: filed.name)
            }

            listChildKeys.each { filed ->
                childKeys.add Key.newInstance(type: filed.type, name: filed.name, sourceTable: filed.tableName, sourceColumn: filed.name)
            }

            listColumnsKeys.each { filed ->
                columns.add Column.newInstance(type: filed.type, name: filed.name)
            }

            meta = showCase.actualMeta

        }

    }

    def genTables() {

        List<Table> tables = []

        iterateShowCases { SC shCs ->
            tables.add(new Table(shCs))
            return false
        }

        tables

    }

}



