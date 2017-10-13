package kz.bsbnb.usci.tools
/**
 * Created by emles on 28.09.17
 */

class Tools {

    def creationDate = "1506578767"
    def creator = "Администратор"
    def modificationDate = "1506578949"
    def modifier = "Администратор"

    static final Tools tools = Tools.newInstance()

    static final Tools getInstance() {
        tools
    }

    def genObjectId = {

        def generator = { String alphabet, int n ->
            new Random().with {
                (1..n).collect { alphabet[nextInt(alphabet.length())] }.join()
            } as String
        }

        generator((('0'..'9') + ('A'..'F')).join(), 8) + "-" +
                generator((('0'..'9') + ('A'..'F')).join(), 4) + "-" +
                generator((('0'..'9') + ('A'..'F')).join(), 4) + "-" +
                generator((('0'..'9') + ('A'..'F')).join(), 4) + "-" +
                generator((('0'..'9') + ('A'..'F')).join(), 12)

    }

    def number = 30;
    def nextId = {
        "o${++number}"
    }
    def carrId = {
        "o${number}"
    }

    def xmlClass = { id, objectId, name, code, attributesXml = "", operationsXml = "" ->
        """
    <o:Class Id="$id">
        <a:ObjectID>$objectId</a:ObjectID>
        <a:Name>$name</a:Name>
        <a:Code>$code</a:Code>
        <a:CreationDate>$creationDate</a:CreationDate>
        <a:Creator>$creator</a:Creator>
        <a:ModificationDate>$modificationDate</a:ModificationDate>
        <a:Modifier>$modifier</a:Modifier>
        <a:UseParentNamespace>0</a:UseParentNamespace>
        $attributesXml
        $operationsXml
    </o:Class>""" as String
    }

    def xmlAttribute = { id, objectId, name, code, dataType, visibility ->
        """
            <o:Attribute Id="$id">
                <a:ObjectID>$objectId</a:ObjectID>
                <a:Name>$name</a:Name>
                <a:Code>$code</a:Code>
                <a:CreationDate>$creationDate</a:CreationDate>
                <a:Creator>$creator</a:Creator>
                <a:ModificationDate>$modificationDate</a:ModificationDate>
                <a:Modifier>$modifier</a:Modifier>
                <a:DataType>$dataType</a:DataType>
                <a:Attribute.Visibility>$visibility</a:Attribute.Visibility>
            </o:Attribute>""" as String
    }

    def xmlAttributes = { List<String> attributes ->
        !attributes.empty ? """
            <c:Attributes>${attributes.join('')}
            </c:Attributes>""" : ""
    }

    def xmlClasses = { List<String> classes ->
        !classes.empty ?
                """<c:Classes>${classes.join('')}
</c:Classes>""" : ""
    }

    def xmlAssociation = { id, objectId, name, code,
                           roleAIndicator,
                           roleBIndicator,
                           roleAName,
                           roleBName,
                           roleAMultiplicity,
                           roleBMultiplicity,
                           roleAId,
                           roleBId
        ->

        // <a:Comment></a:Comment>
        // <a:Description></a:Description>
        // <a:Annotation></a:Annotation>

        // <a:RoleAIndicator></a:RoleAIndicator> Association
        // <a:RoleAIndicator>A</a:RoleAIndicator> Aggregation
        // <a:RoleAIndicator>C</a:RoleAIndicator> Composition

        // <a:RoleAVisibility></a:RoleAVisibility> public
        // <a:RoleAVisibility>*</a:RoleAVisibility> package
        // <a:RoleBVisibility>-</a:RoleBVisibility> private
        // <a:RoleAVisibility>#</a:RoleAVisibility> protected

        // <a:RoleAChangeability></a:RoleAChangeability> Changeable
        // <a:RoleAChangeability>A</a:RoleAChangeability> Add-only
        // <a:RoleAChangeability>F</a:RoleAChangeability> Frozen
        // <a:RoleAChangeability>R</a:RoleAChangeability> Read-only

        // <a:RoleANavigability>1</a:RoleANavigability>
        // <a:RoleANavigability>0</a:RoleANavigability>
        // <a:RoleAPersistent>1</a:RoleAPersistent>
        // <a:RoleAPersistent>0</a:RoleAPersistent>
        // <a:RoleAVolatile>1</a:RoleAVolatile>
        // <a:RoleAVolatile>0</a:RoleAVolatile>

        """
            <o:Association Id="$id">
                <a:ObjectID>$objectId</a:ObjectID>
                <a:Name>$name</a:Name>
                <a:Code>$code</a:Code>
                <a:CreationDate>$creationDate</a:CreationDate>
                <a:Creator>$creator</a:Creator>
                <a:ModificationDate>$modificationDate</a:ModificationDate>
                <a:Modifier>$modifier</a:Modifier>
                ${roleAIndicator ? "<a:RoleAIndicator>$roleAIndicator</a:RoleAIndicator>" : ""}
                ${roleBIndicator ? "<a:RoleBIndicator>$roleBIndicator</a:RoleBIndicator>" : ""}
                ${roleAName ? "<a:RoleBName>$roleAName</a:RoleBName>" : ""}
                ${roleBName ? "<a:RoleBName>$roleBName</a:RoleBName>" : ""}
                ${roleAMultiplicity ? "<a:RoleAMultiplicity>$roleAMultiplicity</a:RoleAMultiplicity>" : ""}
                ${roleBMultiplicity ? "<a:RoleBMultiplicity>$roleBMultiplicity</a:RoleBMultiplicity>" : ""}
                <a:ExtendedAttributesText>
                </a:ExtendedAttributesText>
                <c:Object1>
                    <o:Class Ref="$roleBId"/>
                </c:Object1>
                <c:Object2>
                    <o:Class Ref="$roleAId"/>
                </c:Object2>
            </o:Association>""" as String
    }

    def xmlAssociations = { List<String> associations ->
        !associations.empty ?
                """     <c:Associations>${associations.join('')}
</c:Associations>""" : ""
    }

    def xmlAssociationSymbol = { id, associationId, roleAId, roleBId,
                                 rectStr,
                                 pointsStr
        ->
        """
    <o:AssociationSymbol Id="$id">
        <a:CreationDate>$creationDate</a:CreationDate>
        <a:ModificationDate>$modificationDate</a:ModificationDate>
        ${rectStr ? "<a:Rect>$rectStr</a:Rect>" : ""}
        ${pointsStr ? "<a:ListOfPoints>$pointsStr</a:ListOfPoints>" : ""}
        <a:CornerStyle>1</a:CornerStyle>
        <a:ArrowStyle>3592</a:ArrowStyle>
        <a:LineColor>16744448</a:LineColor>
        <a:ShadowColor>12632256</a:ShadowColor>
        <a:FontList>CENTER 0 Arial Unicode MS,8,N
            SOURCE 0 Arial Unicode MS,8,N
        </a:FontList>
        <c:SourceSymbol>
            <o:ClassSymbol Ref="$roleAId"/>
        </c:SourceSymbol>
        <c:DestinationSymbol>
            <o:ClassSymbol Ref="$roleBId"/>
        </c:DestinationSymbol>
        <c:Object>
            <o:Association Ref="$associationId"/>
        </c:Object>
    </o:AssociationSymbol>""" as String
    }

    def xmlClassSymbol = { id, classId,
                           rectStr
        ->
        """
    <o:ClassSymbol Id="$id">
        <a:CreationDate>$creationDate</a:CreationDate>
        <a:ModificationDate>$modificationDate</a:ModificationDate>
        <a:IconMode>-1</a:IconMode>
        ${rectStr ? "<a:Rect>$rectStr</a:Rect>" : ""}
        <a:LineColor>16744448</a:LineColor>
        <a:FillColor>16770222</a:FillColor>
        <a:ShadowColor>12632256</a:ShadowColor>
        <a:FontList>STRN 0 Arial Unicode MS,8,N
            QDNM 0 Arial Unicode MS,8,N
            CNTR 0 Arial Unicode MS,8,N
            Attributes 0 Arial Unicode MS,8,N
            ClassPrimaryAttribute 0 Arial Unicode MS,8,U
            Operations 0 Arial Unicode MS,8,N
            InnerClassifiers 0 Arial Unicode MS,8,N
            LABL 0 Arial Unicode MS,8,N
        </a:FontList>
        <a:BrushStyle>6</a:BrushStyle>
        <a:GradientFillMode>65</a:GradientFillMode>
        <a:GradientEndColor>16777215</a:GradientEndColor>
        <c:Object>
            <o:Class Ref="$classId"/>
        </c:Object>
    </o:ClassSymbol>""" as String
    }

    def xmlSymbols = { List<String> symbols ->
        !symbols.empty ?
                "<c:Symbols>${symbols.join('')}</c:Symbols>" : ""
    }

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

    def generate = { outFilePath, classes, associations ->

        classes = classes.collect { it.value }
        associations = associations.collect { it.value }

        String sSymbols
        if (false)
            sSymbols = tools.xmlSymbols(associations.collect { Association association -> association.symbolXml() } +
                    classes.collect { Class role -> role.symbolXml() })
        else
            sSymbols = ""

        String sClasses = tools.xmlClasses(classes.collect { Class role -> role.classXml() })

        String sAssociations = tools.xmlAssociations(associations.collect { Association association -> association.associationXml() })

        String xml = tools.resource("class_diagram_templite.xml").text
        xml = xml
                .replaceAll("%sSymbols", sSymbols)
                .replaceAll("%sClasses", sClasses)
                .replaceAll("%sAssociations", sAssociations)

        File file = new File(outFilePath)
        file.createNewFile()
        file.text = xml

    }

}

class Entity {

    final Tools tools = Tools.getInstance()

    String id, objectId, symbolId, name, code

    Entity() {
        this.id = tools.nextId()
        this.objectId = tools.genObjectId()
        this.symbolId = tools.nextId()
    }

}

class Class extends Entity {

    List<Attribute> attributes
    List operations

    List[] rectangle

    Class() {
        super()
    }

    String classXml() {
        tools.xmlClass(
                id, objectId, name, code,
                tools.xmlAttributes(
                        attributes.collect { Attribute attribute -> attribute.attributeXml() }
                )
        )
    }

    String symbolXml() {
        tools.xmlClassSymbol(symbolId, id,
                /*rectangle*/
                "((-1500,-1500), (1500,1500))")
    }

    @Override
    String toString() {
        """id = $id, objectId = $objectId, symbolId: $symbolId, name: $name, code: $code"""
    }

}

class Attribute extends Entity {

    String dataType, visibility

    Attribute() {
        super()
    }

    String attributeXml() {
        tools.xmlAttribute(id, objectId, name, code, dataType, visibility)
    }

}

class Association extends Entity {

    String roleAIndicator, roleBIndicator
    String roleAName, roleBName
    String roleAMultiplicity, roleBMultiplicity
    Class roleAObject, roleBObject

    List[] rectangle, points

    Association() {
        super()
    }

    String associationXml() {
        tools.xmlAssociation(id, objectId, name, code,
                roleAIndicator,
                roleBIndicator,
                roleAName,
                roleBName,
                roleAMultiplicity,
                roleBMultiplicity,
                roleAObject.id,
                roleBObject.id)
    }

    String symbolXml() {
        tools.xmlAssociationSymbol(symbolId, id, roleAObject.id, roleBObject.id,
                /*rectangle,
                points*/
                "((-1500,-1500), (1500,1500))",
                "((-1500,-1500), (1500,1500))"
        )
    }

}

final Tools tools = Tools.getInstance()


final String outFilePath = "/opt/projects/usci/usci/modules/tools/src/main/temp/class_diagram_output.oom"

final Map<String, Class> classes = [
        roleA: Class.newInstance(name: "roleA", code: "roleA", attributes: [
                Attribute.newInstance(name: "attribute_1", code: "attribute_1", dataType: "int", visibility: "-"),
        ]),
        roleB: Class.newInstance(name: "roleB", code: "roleB", attributes: [
        ])
]

final Map<String, Association> associations = [
        associationA: Association.newInstance(name: "association", code: "association",
                roleAIndicator: "C", roleBIndicator: null,
                roleAName: null, roleBName: "Name",
                roleAMultiplicity: "0..1", roleBMultiplicity: "0..*",
                roleAObject: classes.roleA, roleBObject: classes.roleB
        )
]


tools.generate(outFilePath, classes, associations)



