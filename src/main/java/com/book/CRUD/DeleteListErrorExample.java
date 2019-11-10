package com.book.CRUD;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 3.14 从Hbase中删除错误数据
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 12:36
 */
public class DeleteListErrorExample {

    public static void main(String[] args) throws IOException {
        // 关闭日志
        Logger.getLogger("org.apache.zookeeper").setLevel(Level.OFF);

        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", 100, "colfam1", "colfam2");
        helper.put("testtable",
                new String[] { "row1" },
                new String[] { "colfam1", "colfam2" },
                new String[] { "qual1", "qual1", "qual2", "qual2", "qual3", "qual3" },
                new long[]   { 1, 2, 3, 4, 5, 6 },
                new String[] { "val1", "val2", "val3", "val4", "val5", "val6" });
        helper.put("testtable",
                new String[] { "row2" },
                new String[] { "colfam1", "colfam2" },
                new String[] { "qual1", "qual1", "qual2", "qual2", "qual3", "qual3" },
                new long[]   { 1, 2, 3, 4, 5, 6 },
                new String[] { "val1", "val2", "val3", "val4", "val5", "val6" });
        helper.put("testtable",
                new String[] { "row3" },
                new String[] { "colfam1", "colfam2" },
                new String[] { "qual1", "qual1", "qual2", "qual2", "qual3", "qual3" },
                new long[]   { 1, 2, 3, 4, 5, 6 },
                new String[] { "val1", "val2", "val3", "val4", "val5", "val6" });
        System.out.println("Before delete call...");
        helper.dump("testtable", new String[]{ "row1", "row2", "row3" }, null, null);

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));


        List<Delete> deletes = new ArrayList<Delete>();

        // 创建针对特定行的Delete实例
        Delete delete1 = new Delete(Bytes.toBytes("row1"));
        //为删除行的Delete实例设置时间戳
        delete1.setTimestamp(4);
        deletes.add(delete1);

        Delete delete2 = new Delete(Bytes.toBytes("row2"));
        //删除一列的最新版本
        delete2.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
        //删除另一列中的给定版本和所有更旧的版本
        delete2.addColumns(Bytes.toBytes("colfam2"), Bytes.toBytes("qual3"),5);
        deletes.add(delete2);

        Delete delete3 = new Delete(Bytes.toBytes("row3"));
        //删除整个列族，包括所有的列和版本
        delete3.addFamily(Bytes.toBytes("colfam1"));
        //在整个列族中，删除给定版本和所有更旧的版本
        delete3.addFamily(Bytes.toBytes("colfam2"),3);
        deletes.add(delete3);

        Delete delete4 = new Delete(Bytes.toBytes("row2"));
        //删除错误数据
        delete4.addColumn(Bytes.toBytes("BOFGUS"),Bytes.toBytes("qual1"));
        deletes.add(delete4);

        try{
            //从Hbase表中删除多行数据
            table.delete(deletes);
        }catch(Exception e){
            System.err.println("Error: " + e);
        }
        table.close();

        System.out.println("Delete length: "+ deletes.size());
        for(Delete delete: deletes){
            System.out.println(delete);
        }


        table.close();
        connection.close();
        System.out.println("After delete call...");
        helper.dump("testtable", new String[]{ "row1", "row2", "row3" }, null, null);
        helper.close();
    }

}
