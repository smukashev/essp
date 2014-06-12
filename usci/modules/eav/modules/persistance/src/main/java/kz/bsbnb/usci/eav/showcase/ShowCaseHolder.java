package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.model.Column;
import kz.bsbnb.ddlutils.model.Database;
import kz.bsbnb.ddlutils.model.Table;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ShowCaseHolder extends JDBCSupport
{
    private final static String TABLES_PREFIX = "EAV_SCH_";
    private final static String COLUMN_PREFIX = "SC_";
    private final static String DATA_TABLES_POSTFIX = "_DAT";
    private final static String IDX_TABLES_POSTFIX = "_IDX";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private ShowCase showCaseMeta;
    private ArrayList<String> idxPaths = new ArrayList<String>();

    public ShowCaseHolder()
    {
    }

    public ShowCaseHolder(ShowCase showCaseMeta)
    {
        this.showCaseMeta = showCaseMeta;
    }

    public ShowCase getShowCaseMeta()
    {
        return showCaseMeta;
    }

    public void setShowCaseMeta(ShowCase showCaseMeta)
    {
        this.showCaseMeta = showCaseMeta;
    }

    private String getDBType(IMetaType type) {
        if (type.isComplex()) {
            throw new IllegalArgumentException("ShowCase can't contain coplexType columns: " + type.toString());
        }

        if(type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();

        if (type.isSet()) {
            throw new IllegalArgumentException("ShowCase can't contain set columns: " + type.toString());
        }

        MetaValue metaValue = (MetaValue)type;

        switch (metaValue.getTypeCode()) {

            case INTEGER:
                return "NUMERIC";
            case DATE:
                return "TIMESTAMP";
            case STRING:
                return "VARCHAR";
            case BOOLEAN:
                return "NUMERIC";
            case DOUBLE:
                return "NUMERIC";
            default:
                throw new IllegalArgumentException("Unknown simple type code");
        }
    }

    private String getDBSize(IMetaType type) {
        if (type.isComplex()) {
            throw new IllegalArgumentException("ShowCase can't contain coplexType columns");
        }

        if(type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();


        if (type.isSet()) {
            throw new IllegalArgumentException("ShowCase can't contain set columns");
        }

        MetaValue metaValue = (MetaValue)type;

        switch (metaValue.getTypeCode()) {

            case INTEGER:
                return "10,0";
            case DATE:
                return null;
            case STRING:
                return "1024";
            case BOOLEAN:
                return "1";
            case DOUBLE:
                return "17,3";
            default:
                throw new IllegalArgumentException("Unknown simple type code");
        }
    }

    class IdxNode {
        String name;
        HashSet<IdxNode> nodes = new HashSet<IdxNode>();
        ArrayList<Integer> pathIndexes = new ArrayList<Integer>();

        IdxNode(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void addNode(IdxNode node, int index) {
            for (IdxNode n : nodes) {
                if (n.equals(node)) {
                    n.addIndex(index);
                    return;
                }
            }

            node.addIndex(index);
            nodes.add(node);
        }

        public void addIndex(int index) {
            pathIndexes.add(index);
        }

        public ArrayList<String> getPaths() {
            ArrayList<String> res = new ArrayList<String>();

            res.add(name);

            for (IdxNode n : nodes) {
                ArrayList<String> nPaths = n.getPaths();

                for (String curPath : nPaths) {
                    res.add(name + "." + curPath);
                }
            }

            return res;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IdxNode idxNode = (IdxNode) o;

            if (!name.equals(idxNode.name)) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return name.hashCode();
        }

        @Override
        public String toString()
        {
            return "IdxNode{" +
                    "name='" + name + '\'' +
                    ", nodes=" + nodes +
                    ", pathIndexes=" + pathIndexes +
                    '}';
        }
    }

    public void calculateIdxPaths() {
        if (showCaseMeta == null) {
            throw new IllegalStateException("Can't create tables without ShowCase meta information.");
        }

        idxPaths.clear();

        IdxNode curNode = new IdxNode("root");
        IdxNode root = curNode;
        Queue<IdxNode> nodesQueue = new ConcurrentLinkedQueue<IdxNode>();

        ArrayList<String> paths = new ArrayList<String>();

        int j = 0;
        for (ShowCaseField field : showCaseMeta.getFieldsList()) {
            paths.add(field.getAttributePath());
            curNode.addIndex(j++);
        }

        boolean done = false;

        while(!done) {
            boolean was = false;
            for (int i : curNode.pathIndexes) {
                String path = paths.get(i);

                if (path.trim().length() < 1) {
                    continue;
                }
                was = true;

                int ind = path.indexOf(".");
                if (ind > 0) {
                    curNode.addNode(new IdxNode(path.substring(0, ind)), i);
                    paths.set(i, path.substring(ind + 1));
                } else {
                    curNode.addNode(new IdxNode(path), i);
                    paths.set(i, "");
                }
            }

            nodesQueue.addAll(curNode.nodes);

            if (nodesQueue.size() > 0) {
                curNode = nodesQueue.poll();
            } else {
                done = !was;
            }
        }

        idxPaths = root.getPaths();

//        System.out.println("################");
//        for (String cPath : idxPaths) {
//            System.out.println(cPath);
//        }
    }

    public void createTables() {
        if (showCaseMeta == null) {
            throw new IllegalStateException("Can't create tables without ShowCase meta information.");
        }

        String dataTableName = TABLES_PREFIX + showCaseMeta.getTableName().toUpperCase() + DATA_TABLES_POSTFIX;
        String idxTableName = TABLES_PREFIX + showCaseMeta.getTableName().toUpperCase() + IDX_TABLES_POSTFIX;
        Database model = new Database();
        model.setName("model");

        Table dataTable = new Table();
        dataTable.setName(dataTableName);
        //table.setDescription();

        Column idColumn = new Column();

        idColumn.setName("ID");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        idColumn.setType("NUMERIC");
        idColumn.setSize("14,0");
        idColumn.setAutoIncrement(true);

        dataTable.addColumn(idColumn);

        Column idxColumn = new Column();

        idxColumn.setName("IDX_ID");
        idxColumn.setPrimaryKey(true);
        idxColumn.setRequired(true);
        idxColumn.setType("NUMERIC");
        idxColumn.setSize("14,0");
        idxColumn.setAutoIncrement(true);

        dataTable.addColumn(idxColumn);

        for (ShowCaseField field : showCaseMeta.getFieldsList()) {
            Column column = new Column();

            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType = showCaseMeta.getMeta().getEl(field.getAttributePath() + "." + field.getAttributeName());

            column.setType(getDBType(metaType));

            String columnSize = getDBSize(metaType);

            if (columnSize != null) {
                column.setSize(columnSize);
            }

            //column.setDefaultValue(xmlReader.getAttributeValue(idx));
            column.setAutoIncrement(false);
            //column.setDescription(xmlReader.getAttributeValue(idx));
            //column.setJavaName(xmlReader.getAttributeValue(idx));

            dataTable.addColumn(column);
        }

        model.addTable(dataTable);


        Table idxTable = new Table();
        idxTable.setName(idxTableName);
        //table.setDescription();

        idColumn = new Column();

        idColumn.setName("ID");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        idColumn.setType("NUMERIC");
        idColumn.setSize("14,0");
        idColumn.setAutoIncrement(true);

        idxTable.addColumn(idColumn);

        //calculateIdxPaths();
        generatePaths();

        for(String prefix : prefixToColumn.keySet()){
            Column column = new Column();
            column.setName(COLUMN_PREFIX + prefixToColumn.get(prefix) + "_ID");

            column.setPrimaryKey(false);
            column.setRequired(false);

            column.setType("NUMERIC");
            column.setSize("14,0");

            column.setAutoIncrement(false);

            idxTable.addColumn(column);
        }

        /*int i = 1;

        for (String cPath : idxPaths) {
            //System.out.println(cPath);
            Column column = new Column();

            column.setName(COLUMN_PREFIX + cPath.replace(".", "_").substring(
                    Math.max(COLUMN_PREFIX.length() + 5 - 32, 0), cPath.length()) + i++);
            column.setPrimaryKey(false);
            column.setRequired(false);

            column.setType("NUMERIC");
            column.setSize("14,0");

            //column.setDefaultValue(xmlReader.getAttributeValue(idx));
            column.setAutoIncrement(false);
            //column.setDescription(xmlReader.getAttributeValue(idx));
            //column.setJavaName(xmlReader.getAttributeValue(idx));

            idxTable.addColumn(column);
        } */

        model.addTable(idxTable);

        System.out.println(model.toVerboseString());

        Platform platform = PlatformFactory.createNewPlatformInstance(jdbcTemplate.getDataSource());

        platform.createModel(model, false, true);
    }

    class IdsHolder {
        private ArrayList<Long> idsApplied = new ArrayList<Long>();
        private ArrayList<Long> idsLoaded = new ArrayList<Long>();
        private ArrayList<Object> valuesLoaded = new ArrayList<Object>();
        private ArrayList<Object> valuesApplied = new ArrayList<Object>();

        public void addLoadedId(Long id) {
            idsLoaded.add(id);
        }

        public void addAppliedId(Long id) {
            idsApplied.add(id);
        }

        public void addAppliedValue(Object value) {
            valuesApplied.add(value);
        }

        public void addLoadedValue(Object value) {
            valuesLoaded.add(value);
        }

        @Override
        public String toString()
        {
            String result = "IdsHolder: \n";

            result += "idsLoaded: ";

            for (Long id : idsLoaded) {
                result += id + " ";
            }

            result += "\n";

            result += "idsApplied: ";

            for (Long id : idsApplied) {
                result += id + " ";
            }

            result += "\n";

            result += "valuesLoaded: \n";

            int i = 0;

            for (Object value : valuesLoaded) {
                result += showCaseMeta.getFieldsList().get(i++).getColumnName() + ": ";
                if (value != null) {
                    result += "\"" + value.toString() + "\"\n";
                } else {
                    result += "null\n";
                }
            }

            result += "valuesApplied: \n";

            i = 0;

            for (Object value : valuesApplied) {
                result += showCaseMeta.getFieldsList().get(i++).getColumnName() + ": ";
                if (value != null) {
                    result += "\"" + value.toString() + "\"\n";
                } else {
                    result += "null\n";
                }
            }

            result += "\n";

            return result;
        }
    }

    Map<String,String> prefixToColumn;

    public void generatePaths(){
        prefixToColumn = new HashMap<String, String>();
        Map<String,Integer> nextNumber = new HashMap<String,Integer>();
        Map<String,String> columnToPrefix = new HashMap<String, String>();

        for(ShowCaseField field : showCaseMeta.getFieldsList()){
            String pt = "root." + field.getAttributePath();
            String[] temp  = pt.split("\\.");
            String prefix = "";
            for(int i=0;i<temp.length;i++){
                if(temp[i].matches(".*\\d$"))
                    throw new IllegalArgumentException("Description ends with number !!!");
                prefix = prefix.equals("") ? temp[i] : prefix + "." + temp[i];
                if( !prefixToColumn.containsKey(prefix)){
                    if(!nextNumber.containsKey(temp[i]))
                        nextNumber.put(temp[i], 1);
                    int number = nextNumber.get(temp[i]);
                    nextNumber.put(temp[i], number + 1);
                    prefixToColumn.put(prefix, temp[i] + number);
                    columnToPrefix.put(temp[i] + number, prefix);
                }
            }
        }

        for( String s : nextNumber.keySet()) {
            if( nextNumber.get(s) == 2)
            {
                String prefix = columnToPrefix.get(s + 1);
                prefixToColumn.put( prefix, s);
            }
        }

        //for(String s: prefixToColumn.keySet()){
        //    System.out.println(s + " " + prefixToColumn.get(s));
        //}
    }

    class ShowCaseEntries{
        List<HashMap> entries = new ArrayList<HashMap>();
        public String columnName;

        ShowCaseEntries(IBaseEntity entity, String path, String columnName){
            this.columnName = columnName;
            gen(entity, path, new HashMap(), "", "root");
        }
        public List<HashMap> getEntries(){
            return entries;
        }
        public Set getCols(){
            if(entries.size() < 1) throw new RuntimeException("no entries");
            return entries.get(0).keySet();
        }
        public int getEntriesSize(){
            return entries.size();
        }
        public void gen(IBaseEntity entity,String curPath, HashMap map, String parent, String prefix){
            if(curPath == null || curPath.equals("")) return;

            MetaClass curMeta = entity.getMeta();
            String path = (curPath.indexOf('.') == -1 ) ? curPath: curPath.substring(0, curPath.indexOf('.'));
            String nextPath = curPath.indexOf('.') == -1 ? null : curPath.substring(curPath.indexOf('.') + 1);
            IMetaAttribute attribute = curMeta.getMetaAttribute(path);
            map.put(prefixToColumn.get(prefix) + "_id", entity.getId());
            //map.put( (parent.equals("") ? "root" : parent) + "_id", entity.getId());
            //System.out.println("deb " + parent + " " + prefix +" " +prefixToColumn.get(prefix));

            if(!attribute.getMetaType().isComplex()){
                //map.put( parent + "_id", entity.getId());
                map.put(prefixToColumn.get(prefix) + "_id", entity.getId());
                if(!attribute.getMetaType().isSet()){
                    map.put( columnName, entity.getEl(path));
                    //printMap(map);
                    entries.add(map);
                } else{
                    IBaseSet set = (IBaseSet) entity.getEl(path);
                    if(set != null){
                        for(IBaseValue o : set.get()){
                           HashMap nmap = (HashMap) map.clone();
                            //System.out.println(o.getValue());
                           nmap.put(columnName, o.getValue());
                            entries.add(nmap);
                        }
                    }
                }
            }else if(attribute.getMetaType().isSet()){
                IBaseSet next = (IBaseSet) entity.getEl(path);

                if(next!=null){
                    for(Object o : next.get()){
                        gen( (IBaseEntity) ( (IBaseValue) o).getValue(), nextPath, (HashMap) map.clone() , path, prefix + "." +path);
                    }
                }
            }else{
                IBaseEntity next = (IBaseEntity) entity.getEl(path);
                gen(next, nextPath, (HashMap) map.clone(), path, prefix+"."+path);
            }
        }
    }

    public void printMap(HashMap map){
        System.out.println("map: [");
         for(Object c: map.keySet())
             System.out.println(" | " + c + " " + map.get(c));
        System.out.println("]");
    }

    public void persistMap(HashMap map){
        showCaseMeta.getName();
        StringBuilder sql = new StringBuilder("insert into ")
                .append(TABLES_PREFIX + showCaseMeta.getTableName().toUpperCase() + "_IDX").append("(");
        StringBuilder placeholders = new StringBuilder();

        for (Iterator<String> iter = prefixToColumn.keySet().iterator(); iter.hasNext();) {
            String columnName = prefixToColumn.get(iter.next());
            sql.append( COLUMN_PREFIX + columnName + "_id");
            placeholders.append(map.get(columnName + "_id"));

            if (iter.hasNext()) {
                sql.append(",");
                placeholders.append(",");
            }
        }

        sql.append(") values (").append(placeholders).append(")");

        KeyHolder holder = new GeneratedKeyHolder();
        final String sql1 = sql.toString();
        System.out.println(sql1);
//        jdbcTemplate.update(sql.toString(),new Prepared);
//        System.out.println(holder.getKey().longValue());
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement( sql1.toString(), new String[]{"ID"});
                return ps;
            }
        }, holder);


        long lid = holder.getKey().longValue();

        sql = new StringBuilder("insert into ")
                .append(TABLES_PREFIX + showCaseMeta.getTableName().toUpperCase() + "_DAT").append("(");
        placeholders = new StringBuilder();

        Object[] vals = new Object[showCaseMeta.getFieldsList().size()];
        int i = 0;

        for(ShowCaseField field : showCaseMeta.getFieldsList()){
            sql.append( COLUMN_PREFIX + field.getColumnName() + ",");
            placeholders.append("?,");
            vals[i++] = map.get(field.getColumnName());
        }

        sql.append("IDX_ID");
        placeholders.append(lid);

        sql.append(" ) values (").append(placeholders).append(")");

        //System.out.println(sql);
        //jdbcTemplate.update(sql.toString());
        jdbcTemplate.update(sql.toString(), vals);

        //sql = new StringBuilder("");
    }

    boolean compatible(HashMap a, HashMap b){
        for(Object o : a.keySet()){
            String key = (String) o;
            if(key.endsWith("_id"))
              if(b.containsKey(key) && a.get(key) != b.get(key)) return false;
        }
        return true;
    }

    boolean merge(HashMap a, HashMap b){
        if(compatible(a,b)){
            for(Object o : b.keySet())
            {
                String key = (String) o;
                if(!a.containsKey(key))
                    a.put(key,b.get(key));
            }
            return true;
        }
        return false;
    }

    public void print(IBaseEntity entity){
        int n = showCaseMeta.getFieldsList().size();
        ShowCaseEntries[] showCases = new ShowCaseEntries[n];
        int i = 0;
        for(ShowCaseField field: showCaseMeta.getFieldsList()){
            showCases[i] = new ShowCaseEntries(entity,
                    field.getAttributePath() + "." + field.getAttributeName(),field.getColumnName());
            i++;
        }

        int allRecordsSize = 0;
        for(i=0;i<n;i++)
            allRecordsSize+=showCases[i].getEntriesSize();

        boolean [] was = new boolean[allRecordsSize];
        boolean [] usedGroup = new boolean[n];
        int [] id = new int[allRecordsSize];
        List<HashMap> entries = new ArrayList<HashMap>(allRecordsSize);

        int yk=0;
        for(i=0;i<n;i++)
            for(HashMap m : showCases[i].getEntries()){
                entries.add(yk, m);
                id[yk] = i;
                yk++;
            }

        boolean found = true;
        while(found){
            found = false;
            for(i=0;i<allRecordsSize;i++) if(!was[i]){
               was[i] = true;
               HashMap map = (HashMap) entries.get(i).clone();
               found = true;

                Arrays.fill(usedGroup, false);
                usedGroup[id[i]] = true;

               for(int j=0;j<allRecordsSize;j++) if(i!=j && !was[j] && !usedGroup[id[j]])
                   if(merge( map, entries.get(j)))
                   {
                       was[j] = true;
                       usedGroup[id[j]] = true;
                   }
               printMap(map);
               persistMap(map);
            }
        }
        /*for(i=0;i<n;i++){
            for(Object o : colSet){
                String path = (String) o;
                for(int j=0;j<showCases[i].getEntriesSize();j++)

            }
        }*/

    }

    private List<IdsHolder> prepare(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied,
                                    IBaseEntityManager entityManager) {
        ArrayList<IdsHolder> result = new ArrayList<IdsHolder>();

        IdsHolder curHolder = new IdsHolder();
        for (String cPath : idxPaths) {
            if (cPath.equals("root")) {
                if (baseEntityLoaded != null) {
                    curHolder.addLoadedId(baseEntityLoaded.getId());
                } else {
                    curHolder.addLoadedId(0L);
                }

                if (baseEntityApplied != null) {
                    curHolder.addAppliedId(baseEntityApplied.getId());
                } else {
                    curHolder.addAppliedId(0L);
                }

                continue;
            }

            cPath = cPath.substring(5);

            System.out.println("CurPath: " + cPath);

            IBaseEntity childBaseEntityLoaded = null;
            IBaseEntity childBaseEntityApplied = null;

            if (baseEntityLoaded != null) {
                childBaseEntityLoaded = (IBaseEntity)baseEntityLoaded.getEl(cPath);
            }

            if (baseEntityApplied != null) {
                childBaseEntityApplied = (IBaseEntity)baseEntityApplied.getEl(cPath);
            }

            long childBaseEntityLoadedId = 0;
            long childBaseEntityAppliedId = 0;

            if (childBaseEntityLoaded != null) {
                childBaseEntityLoadedId = childBaseEntityLoaded.getId();
            }

            if (childBaseEntityApplied != null) {
                childBaseEntityAppliedId = childBaseEntityApplied.getId();
            }

            curHolder.addLoadedId(childBaseEntityLoadedId);
            curHolder.addAppliedId(childBaseEntityAppliedId);
        }

        for(ShowCaseField field : showCaseMeta.getFieldsList()) {
            if (baseEntityApplied != null) {
                curHolder.addAppliedValue(baseEntityApplied.getEl(field.getAttributePath() +
                        "." + field.getAttributeName()));
            } else {
                curHolder.addAppliedValue(null);
            }

            if (baseEntityLoaded != null) {
                curHolder.addLoadedValue(baseEntityLoaded.getEl(field.getAttributePath() +
                        "." + field.getAttributeName()));
            } else {
                curHolder.addLoadedValue(null);
            }
        }

        result.add(curHolder);

        System.out.println(curHolder.toString());

        return result;
    }

    public void process(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied,
                      IBaseEntityManager entityManager) {
        List<IdsHolder> idsHolders = prepare(baseEntitySaving, baseEntityLoaded, baseEntityApplied, entityManager);
    }
}
