package kz.bsbnb.usci.receiver

import org.junit.Test

/**
 * Created by emles on 11.10.17
 */
class XmlTemplite {

    @Test
    void doTeplite() {

        def binding = [firstname: "Jochen", lastname: "Theodorou",
                       nickname : "blackdrag", salutation: "Dear"]
        def engine = new groovy.text.XmlTemplateEngine()
        def text = '''<?xml version="1.0" encoding="UTF-8"?>
   <document xmlns:gsp='http://groovy.codehaus.org/2005/gsp' xmlns:foo='baz' type='letter'>
     <gsp:scriptlet>def greeting = "${salutation}est"</gsp:scriptlet>
     <gsp:expression>greeting</gsp:expression>
     <foo:to>$firstname "$nickname" $lastname</foo:to>
     How are you today?
   </document>
   '''
        def template = engine.createTemplate(text).make(binding)
        println template.toString()

    }

}



