package kz.bsbnb.usci.eav.showcase;


import kz.bsbnb.ddlutils.model.*;

import java.util.ArrayList;

;

/**
 * Created by Bauyrzhan.Ibraimov on 01.09.2015.
 */
public class ShowCaseIndex {

    private Index ShowCaseIndex;
    String IndexName;
/*
    public static void main(String[] args)
    {
        String IndexType="nonunique";
        String TableName ="r_core_credit";
        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.add("Name");
        columnNames.add("Surname");
        columnNames.add("Middlename");


        kz.bsbnb.usci.eav.showcase.ShowCaseIndex index = new ShowCaseIndex(IndexType, TableName, columnNames);
    }*/

    public  ShowCaseIndex(String IndexType, String TableName, ArrayList<String> columnNames)
    {
        IndexName="";

        if(IndexType.equals("unique"))
        {
            ShowCaseIndex = new UniqueIndex();
        }
        else if(IndexType.equals("nonunique"))
        {
            ShowCaseIndex = new NonUniqueIndex();
        }


        if(TableName.contains("_")) {
            String[] parts = TableName.split("_");
            for(int i =0; i<parts.length; i++)
            {
                String part = parts[i];
                IndexName = IndexName+part.substring(0,1);
            }
        }
        else
        {
            IndexName = IndexName + TableName.substring(0,1);
        }

        IndexName = IndexName+"_IN";
        for(String columnName: columnNames)
        {
            IndexName = IndexName + "_";
            if(columnName.contains("_"))
            {
                String[] parts = columnName.split("_");
                for(int i=0; i<parts.length; i++)
                {
                    IndexName = IndexName+parts[i].substring(0,1);
                }
            }
            else
            {
                IndexName = IndexName+columnName.substring(0,1)+columnName.substring(columnName.length()-1);
            }

        }
        ShowCaseIndex.setName(IndexName);
        for(String columnName: columnNames)
        {
            ShowCaseIndex.addColumn(new IndexColumn(columnName));
        }
    }

    public Index getIndex(){ return ShowCaseIndex;}
}
