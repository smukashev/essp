package kz.bsbnb.usci.showcase

import groovy.json.JsonBuilder
import kz.bsbnb.usci.core.service.IMetaFactoryService
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass as MC
import kz.bsbnb.usci.eav.showcase.ShowCase
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

    @Test
    void print$MetaClasses$ShowCases() {

        final List<MC> metaClasses = metaFactory.getMetaClasses()

        final List<ShowCase> showCases = showcaseService.list()

        metaClasses.each { MC metaClass ->

            (1..64).each { print '#' }

            print """
metaClass: ${new JsonBuilder(metaClass).toPrettyString()}

"""
        }

        showCases.findAll { it.downPath }.each { ShowCase showCase ->

            (1..64).each { print '#' }

            print """
name: $showCase.name
className: $showCase.meta.className
tableName: $showCase.tableName
downPath: $showCase.downPath

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

"""

        }

    }

    @Test
    void view$MetaClasses$ShowCases() {

    }

}



