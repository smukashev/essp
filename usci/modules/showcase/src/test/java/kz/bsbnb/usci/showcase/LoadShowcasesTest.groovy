package kz.bsbnb.usci.showcase

import groovy.json.JsonBuilder
import kz.bsbnb.usci.core.service.IMetaFactoryService
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass as MC
import kz.bsbnb.usci.eav.showcase.ShowCase as SC
import kz.bsbnb.usci.showcase.service.ShowcaseService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by emles on 17.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class LoadShowcasesTest {

    @Autowired
    private IMetaFactoryService metaFactory

    @Autowired
    private ShowcaseService showcaseService

    private Closure iterateMetaClasses

    {
        iterateMetaClasses = { Set<String> set, mCs, Closure closure ->
            mCs
                    .findAll { MC metaClazz -> if (set.contains(metaClazz.className)) return false; else set.add(metaClazz.className); return true }
                    .each { MC metaClazz ->

                List childMCs = []

                metaClazz.getAttributeNames().each { String name ->
                    def meta = metaClazz.getMemberType(name)
                    if (meta instanceof MC) childMCs.add(meta)
                }

                if (true) iterateMetaClasses(set, childMCs, closure)

                closure(metaClazz)

            }
        }
    }

    private Closure iterateShowCases

    {
        iterateShowCases = { Set<String> set, sCs, Closure closure ->
            sCs
                    .findAll { it.downPath }
                    .findAll { SC showCase -> if (set.contains(showCase.name)) return false; else set.add(showCase.name); return true }
                    .each { SC showCase ->

                if (true) iterateShowCases(set, showCase.childShowCases, closure)

                closure(showCase)

            }
        }
    }

    @Test
    void print$ShowCases() {

        final List<SC> showCases = showcaseService.list()

        Integer count = 0

        Set<String> set = new TreeSet<>()

        iterateShowCases(set, showCases) { SC showCase ->

            (1..64).each { print '#' }

            /*print """
count: ${++count}
name: $showCase.name
className: $showCase.actualMeta.className
tableName: $showCase.tableName
downPath: $showCase.downPath

final: $showCase.isFinal
child: $showCase.isChild

searchable: $showCase.actualMeta.searchable
parentIsKey: $showCase.actualMeta.parentIsKey

fieldsList: ${new JsonBuilder(showCase.fieldsList).toPrettyString()}

customFieldsList: ${new JsonBuilder(showCase.customFieldsList).toPrettyString()}

historyKeyFieldsList: ${new JsonBuilder(showCase.historyKeyFieldsList).toPrettyString()}

rootKeyFieldsList: ${new JsonBuilder(showCase.rootKeyFieldsList).toPrettyString()}

indexes: ${new JsonBuilder(showCase.indexes).toPrettyString()}
${
                false ?
                        """
meta: ${new JsonBuilder(showCase.meta).toPrettyString()}""" :
                        ""
            }
${
                true ?
                        """
actualMeta: ${new JsonBuilder(showCase.actualMeta).toPrettyString()}""" :
                        ""
            }
${
                false ?
                        """
showCase: ${new JsonBuilder(showCase).toPrettyString()}""" :
                        ""
            }

"""*/

            print """
count: ${++count}
name: $showCase.name
className: $showCase.actualMeta.className

searchable: $showCase.actualMeta.searchable
parentIsKey: $showCase.actualMeta.parentIsKey

rootKeyFieldsList: ${new JsonBuilder(showCase.rootKeyFieldsList).toPrettyString()}

historyKeyFieldsList: ${new JsonBuilder(showCase.historyKeyFieldsList).toPrettyString()}

customFieldsList: ${new JsonBuilder(showCase.customFieldsList).toPrettyString()}

fieldsList: ${new JsonBuilder(showCase.fieldsList).toPrettyString()}

"""

        }

    }

    @Test
    void print$MetaClasses() {

        final List<MC> metaClasses = metaFactory.getMetaClasses()

        Integer count = 0

        Set<String> set = new TreeSet<>()

        iterateMetaClasses(set, metaClasses) { MC metaClazz ->

            (1..64).each { print '#' }

            print """
count: ${++count}
metaClass: ${new JsonBuilder(metaClazz).toPrettyString()}

"""
        }

    }

    @Test
    void print$Tables() {

    }

}



