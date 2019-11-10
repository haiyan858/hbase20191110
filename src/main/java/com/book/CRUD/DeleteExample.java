package com.book.CRUD;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 3.12 从Hbase中删除数据
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 16:49
 */
public class DeleteExample {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端, 表引用
        //HTable table = new HTable(conf,"testtable");

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtablle");
        helper.createTable("testtable", 100, "colfam1", "colfam2");
        helper.put("testtable",
                new String[]{"row1"},
                new String[]{"colfam1", "colfam2"},
                new String[]{"qual1", "qual1", "qual2", "qual2", "qual3", "qual3"},
                new long[]{1, 2, 3, 4, 5, 6},
                new String[]{"val1", "val1", "val2", "val2", "val3", "val3"});

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));

        // 创建针对特定行的Delete实例
        Delete delete = new Delete(Bytes.toBytes("row1"));

        //设置时间戳
        delete.setTimestamp(1);

        //删除一列中的特定版本，如果不存在则不删除
        delete.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), 1);

        //删除一列中的全部版本
        delete.addColumns(Bytes.toBytes("colfam2"), Bytes.toBytes("qual1"));
        //删除一列中的给定版本和所有更旧的版本
        delete.addColumns(Bytes.toBytes("colfam2"), Bytes.toBytes("qual3"), 15);

        //删除整个列族，包括所有的列和版本
        delete.addFamily(Bytes.toBytes("colfam3"));
        //删除给定列族中所有列的给定版本和所有更旧的版本
        delete.addFamily(Bytes.toBytes("colfam3"), 3);

        //从Hbase表中删除数据
        table.delete(delete);

        table.close();
        connection.close();

        System.out.println("After delete call...");
        helper.dump("testtable", new String[]{"row1"}, null, null);
        helper.close();
    }
}
