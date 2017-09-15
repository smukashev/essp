package kz.bsbnb.usci.tools

import groovy.sql.Sql
import org.junit.Test

class tree {

    final static def propertiesPath = 'properties/oracle.properties'
    final static Properties props = new Properties()

    final static STRUCTURE_QUERY = '''SELECT * FROM (
  SELECT 'CLASS'              AS TYPE, T.ID, T.NAME, T.TITLE, NULL AS CONTAINING_ID, NULL AS CONTAINING_NAME,    NULL AS CONTAINING_TITLE,     NULL AS CLASS_ID,  NULL AS CLASS_NAME,    NULL AS CLASS_TITLE     
         ,T.BEGIN_DATE,       T.COMPLEX_KEY_TYPE,       T.PARENT_IS_KEY,        T.IS_CLOSABLE,       NULL AS CONTAINER_TYPE, NULL AS ARRAY_KEY_TYPE, NULL AS TYPE_CODE, NULL AS IS_KEY, NULL AS IS_OPTIONAL_KEY, NULL AS IS_NULLABLE_KEY, T.IS_REFERENCE,       NULL AS IS_CUMULATIVE, NULL AS IS_NULLABLE, NULL AS IS_REQUIRED, NULL AS IS_IMMUTABLE, NULL AS IS_FINAL, T.IS_DISABLED
  FROM EAV_M_CLASSES T
UNION ALL
  SELECT 'COMPLEX_SET'        AS TYPE, T.ID, T.NAME, T.TITLE, T.CONTAINING_ID,       CN.NAME AS CONTAINING_NAME, CN.TITLE AS CONTAINING_TITLE, CL.ID AS CLASS_ID, CL.NAME AS CLASS_NAME, CL.TITLE AS CLASS_TITLE 
         ,NULL AS BEGIN_DATE, NULL AS COMPLEX_KEY_TYPE, NULL AS PARENT_IS_KEY,  NULL AS IS_CLOSABLE, T.CONTAINER_TYPE,       T.ARRAY_KEY_TYPE,       NULL AS TYPE_CODE, T.IS_KEY,       NULL AS IS_OPTIONAL_KEY, NULL AS IS_NULLABLE_KEY, T.IS_REFERENCE,       T.IS_CUMULATIVE,       T.IS_NULLABLE,       T.IS_REQUIRED,       T.IS_IMMUTABLE,       T.IS_FINAL,       T.IS_DISABLED
  FROM EAV_M_COMPLEX_SET T,        EAV_M_CLASSES CN, EAV_M_CLASSES CL 
  WHERE T.CONTAINING_ID = CN.ID AND T.CLASS_ID = CL.ID
UNION ALL
  SELECT 'SIMPLE_SET'         AS TYPE, T.ID, T.NAME, T.TITLE, T.CONTAINING_ID,       CN.NAME AS CONTAINING_NAME, CN.TITLE AS CONTAINING_TITLE, NULL AS CLASS_ID,  NULL AS CLASS_NAME,    NULL AS CLASS_TITLE
         ,NULL AS BEGIN_DATE, NULL AS COMPLEX_KEY_TYPE, NULL AS PARENT_IS_KEY,  NULL AS IS_CLOSABLE, T.CONTAINER_TYPE,       T.ARRAY_KEY_TYPE,       T.TYPE_CODE,       T.IS_KEY,       NULL AS IS_OPTIONAL_KEY, NULL AS IS_NULLABLE_KEY, T.IS_REFERENCE,       T.IS_CUMULATIVE,       T.IS_NULLABLE,       T.IS_REQUIRED,       T.IS_IMMUTABLE,       T.IS_FINAL,       T.IS_DISABLED
  FROM EAV_M_SIMPLE_SET T,         EAV_M_CLASSES CN                   
  WHERE T.CONTAINING_ID = CN.ID
UNION ALL
  SELECT 'COMPLEX_ATTRIBUTE'  AS TYPE, T.ID, T.NAME, T.TITLE, T.CONTAINING_ID,       CN.NAME AS CONTAINING_NAME, CN.TITLE AS CONTAINING_TITLE, CL.ID AS CLASS_ID, CL.NAME AS CLASS_NAME, CL.TITLE AS CLASS_TITLE 
         ,NULL AS BEGIN_DATE, NULL AS COMPLEX_KEY_TYPE, NULL AS PARENT_IS_KEY,  NULL AS IS_CLOSABLE, T.CONTAINER_TYPE,       NULL AS ARRAY_KEY_TYPE, NULL AS TYPE_CODE, T.IS_KEY,       T.IS_OPTIONAL_KEY,       T.IS_NULLABLE_KEY,       NULL AS IS_REFERENCE, NULL AS IS_CUMULATIVE, T.IS_NULLABLE,       T.IS_REQUIRED,       T.IS_IMMUTABLE,       T.IS_FINAL,       T.IS_DISABLED
  FROM EAV_M_COMPLEX_ATTRIBUTES T, EAV_M_CLASSES CN, EAV_M_CLASSES CL 
  WHERE T.CONTAINING_ID = CN.ID AND T.CLASS_ID = CL.ID
UNION ALL
  SELECT 'SIMPLE_ATTRIBUTE'   AS TYPE, T.ID, T.NAME, T.TITLE, T.CONTAINING_ID,       CN.NAME AS CONTAINING_NAME, CN.TITLE AS CONTAINING_TITLE, NULL AS CLASS_ID,  NULL AS CLASS_NAME,    NULL AS CLASS_TITLE
         ,NULL AS BEGIN_DATE, NULL AS COMPLEX_KEY_TYPE, NULL AS PARENT_IS_KEY,  NULL AS IS_CLOSABLE, T.CONTAINER_TYPE,       NULL AS ARRAY_KEY_TYPE, T.TYPE_CODE,       T.IS_KEY,       T.IS_OPTIONAL_KEY,       T.IS_NULLABLE_KEY,       NULL AS IS_REFERENCE, NULL AS IS_CUMULATIVE, T.IS_NULLABLE,       T.IS_REQUIRED,       T.IS_IMMUTABLE,       T.IS_FINAL,       t.IS_DISABLED
  FROM EAV_M_SIMPLE_ATTRIBUTES T,  EAV_M_CLASSES CN
  WHERE T.CONTAINING_ID = CN.ID
)
ORDER BY TYPE, ID
'''

    static {
        props.load(Thread.currentThread().contextClassLoader.getResourceAsStream(propertiesPath))
    }

    def static getSql = {

        def (url, user, password, driver) = [props.getProperty('jdbc.url'), props.getProperty('jdbc.user'), props.getProperty('jdbc.password'), props.getProperty('jdbc.driver')]

        return Sql.newInstance(url, user, password, driver)

    }

    enum META_INFO_TYPE {

        CLASS("CLASS"),
        COMPLEX_SET("COMPLEX_SET"),
        SIMPLE_SET("SIMPLE_SET"),
        COMPLEX_ATTRIBUTE("COMPLEX_ATTRIBUTE"),
        SIMPLE_ATTRIBUTE("SIMPLE_ATTRIBUTE")

        String type

        META_INFO_TYPE(String type) {
            this.type = type
        }

    }

    final Boolean TO_PRINT_A = false
    final Boolean TO_PRINT_B = false

    @Test
    void doTest() {

        final Sql sql = getSql()

        final List metaInfo = []

        def getMetaInfo = {

            sql.eachRow(STRUCTURE_QUERY) { row ->
                metaInfo.add([
                        "TYPE"            : row.TYPE,
                        "ID"              : row.ID,
                        "NAME"            : row.NAME,
                        "TITLE"           : row.TITLE,
                        "CONTAINING_ID"   : row.CONTAINING_ID,
                        "CONTAINING_NAME" : row.CONTAINING_NAME,
                        "CONTAINING_TITLE": row.CONTAINING_TITLE,
                        "CLASS_ID"        : row.CLASS_ID,
                        "CLASS_NAME"      : row.CLASS_NAME,
                        "CLASS_TITLE"     : row.CLASS_TITLE,
                        "BEGIN_DATE"      : row.BEGIN_DATE,
                        "COMPLEX_KEY_TYPE": row.COMPLEX_KEY_TYPE,
                        "PARENT_IS_KEY"   : row.PARENT_IS_KEY,
                        "IS_CLOSABLE"     : row.IS_CLOSABLE,
                        "CONTAINER_TYPE"  : row.CONTAINER_TYPE,
                        "ARRAY_KEY_TYPE"  : row.ARRAY_KEY_TYPE,
                        "TYPE_CODE"       : row.TYPE_CODE,
                        "IS_KEY"          : row.IS_KEY,
                        "IS_OPTIONAL_KEY" : row.IS_OPTIONAL_KEY,
                        "IS_NULLABLE_KEY" : row.IS_NULLABLE_KEY,
                        "IS_REFERENCE"    : row.IS_REFERENCE,
                        "IS_CUMULATIVE"   : row.IS_CUMULATIVE,
                        "IS_NULLABLE"     : row.IS_NULLABLE,
                        "IS_REQUIRED"     : row.IS_REQUIRED,
                        "IS_IMMUTABLE"    : row.IS_IMMUTABLE,
                        "IS_FINAL"        : row.IS_FINAL,
                        "IS_DISABLED"     : row.IS_DISABLED
                ])
            }

            if (false)
                metaInfo.each { Map row ->
                    row.eachWithIndex { Map.Entry<Object, Object> entry, int i ->
                        print "/$i/ $entry.key: \"$entry.value\" "
                    }
                    println()
                }

        }

        def nodePrint = {
            final StringBuffer buffer = new StringBuffer()
            buffer.append "type: ${type}"
            buffer.append ", id: ${id}"
            buffer.append ", name: ${name}"
            buffer.append ", title: ${title}"
            if (_this.complex && type != META_INFO_TYPE.CLASS) {
                buffer.append ", classId: ${classId}"
                buffer.append ", className: ${className}"
                buffer.append ", classTitle: ${classTitle}"
            }
            buffer.append "\n"
            print buffer.toString()
        }

        def simpleNode = { META_INFO_TYPE type, id, String name, String title = null ->
            java.lang.Object node = java.lang.Object.newInstance()
            node.metaClass._this = node
            node.metaClass.type = type
            node.metaClass.id = id
            node.metaClass.name = name
            node.metaClass.title = title
            node.metaClass.complex = false
            node.metaClass.print = nodePrint
            node
        }

        def complexNode = { Object node, classId, String className, String classTitle = null, List childs = [] ->
            node.metaClass.classId = classId
            node.metaClass.className = className
            node.metaClass.classTitle = classTitle
            node.metaClass.childs = childs
            node.metaClass.complex = true
            node.metaClass.print = nodePrint
            node
        }

        def classNode = { Object node, class_ ->
            node.metaClass.class_ = class_
            node
        }

        def searchNodes = {

            List nodes = []

            metaInfo
                    .findAll { it.TYPE == META_INFO_TYPE.CLASS.type }
                    .sort { a, b -> a.ID <=> b.ID }
                    .eachWithIndex { rowCl, i ->
                def node = simpleNode(META_INFO_TYPE.valueOf(rowCl.TYPE), rowCl.ID, rowCl.NAME, rowCl.TITLE)
                List childs = []
                if (TO_PRINT_A) println "TYPE: \"$rowCl.TYPE\", ID: $rowCl.ID, NAME: $rowCl.NAME. "
                metaInfo
                        .findAll {
                    it.TYPE == META_INFO_TYPE.COMPLEX_SET.type || it.TYPE == META_INFO_TYPE.SIMPLE_SET.type || it.TYPE == META_INFO_TYPE.COMPLEX_ATTRIBUTE.type || it.TYPE == META_INFO_TYPE.SIMPLE_ATTRIBUTE.type
                }
                .sort { a, b -> a.ID <=> b.ID }
                        .eachWithIndex { rowCmSt, j ->
                    if (false) println "rowCl.ID: $rowCl.ID, rowCmSt.CONTAINING_ID: $rowCmSt.CONTAINING_ID"
                    if (rowCl.ID == rowCmSt.CONTAINING_ID) {
                        def child = simpleNode(META_INFO_TYPE.valueOf(rowCmSt.TYPE), rowCmSt.ID, rowCmSt.NAME, rowCmSt.TITLE)
                        if (TO_PRINT_A) print "\tTYPE: \"$rowCmSt.TYPE\", ID: $rowCmSt.ID, NAME: $rowCmSt.NAME"
                        switch (rowCmSt.TYPE) {
                            case META_INFO_TYPE.COMPLEX_SET.type:
                            case META_INFO_TYPE.COMPLEX_ATTRIBUTE.type:
                                child = complexNode(child, rowCmSt.CLASS_ID, rowCmSt.CLASS_NAME, rowCmSt.CLASS_TITLE, [])
                                if (TO_PRINT_A) print ", CLASS_ID: $rowCmSt.CLASS_ID, CLASS_NAME: $rowCmSt.CLASS_NAME"
                                break
                        }
                        if (TO_PRINT_A) println ". "
                        childs.add(child)
                    }
                }
                nodes.add(complexNode(node, rowCl.CLASS_ID, rowCl.CLASS_NAME, rowCl.CLASS_TITLE, childs))
            }

            nodes

        }

        def treeNode = {

            List roots = []

            nodes
                    .sort { a, b -> (a.ID <=> b.ID) }
                    .eachWithIndex { node, i ->



            }

        }

        getMetaInfo()

        if (TO_PRINT_A) println(); println()

        List nodes = searchNodes()

        if (TO_PRINT_B) nodes.each { node ->
            node.print()
            if (node.complex) {
                node.childs.each { child ->
                    print "\t"
                    child.print()
                }
            }
        }

        List roots = treeNode()

    }

}



