package kz.bsbnb.usci.receiver.tools

/**
 * Created by emles on 11.10.17
 */
class GspHelper {

    def resource = { resourcePath ->

        def url = System.getResource(resourcePath)
        if (!url) url = getClass().getResource(resourcePath)
        if (!url) url = getClass().getClassLoader().getResource(resourcePath)
        if (!url) url = Thread.getResource(resourcePath)
        if (!url) url = Thread.currentThread().getContextClassLoader().getResource(resourcePath)

        def uri = url.toURI()

        def resourceFile = new File(uri)

        resourceFile

    }

    def gsp(String text, Map binding) {
        def engine = new groovy.text.XmlTemplateEngine()
        def template = engine.createTemplate(text).make(binding)
        return template.toString()
    }

    def gsp(Reader reader, Map binding) {
        def engine = new groovy.text.XmlTemplateEngine()
        def template = engine.createTemplate(reader).make(binding)
        return template.toString()
    }

    def gsp(Reader reader, Writer writer, Map binding) {
        def engine = new groovy.text.XmlTemplateEngine()
        def template = engine.createTemplate(reader).make(binding)
        template.writeTo(writer)
        writer.flush()
        writer.close()
    }

    def gsp(File file, Map binding) {
        def engine = new groovy.text.XmlTemplateEngine()
        def template = engine.createTemplate(file).make(binding)
        return template.toString()
    }

    def gsp(File inFile, File outFile, Map binding) {
        def engine = new groovy.text.XmlTemplateEngine()
        def template = engine.createTemplate(inFile).make(binding)
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)))
        return template.writeTo(writer)
        writer.flush()
        writer.close()
    }

}



