package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.model.Column;
import kz.bsbnb.ddlutils.model.Database;
import kz.bsbnb.ddlutils.model.Table;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ShowCaseHolder extends JDBCSupport
{
    private final static String TABLES_PREFIX = "EAV_SCH_";
    private final static String COLUMN_PREFIX = "SC_";
    private final static String DATA_TABLES_POSTFIX = "_DAT";
    private final static String IDX_TABLES_POSTFIX = "_IDX";

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

        calculateIdxPaths();

        int i = 1;

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
        }

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

            IBaseEntity childBaseEntitySaving = null;
            IBaseEntity childBaseEntityLoaded = null;
            IBaseEntity childBaseEntityApplied = null;

            if (baseEntitySaving != null) {
                childBaseEntitySaving = (IBaseEntity)baseEntitySaving.getEl(cPath);
            }

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
