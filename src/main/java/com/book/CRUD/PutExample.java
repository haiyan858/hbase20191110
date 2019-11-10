package com.book.CRUD;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 向HBase插入数据
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:06
 */
public class PutExample {

    public static void main(String[] args) throws IOException {

        Configuration conf = HBaseConfiguration.create();

        // ^^ PutExample
        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1");

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));

        Put put = new Put(Bytes.toBytes("row1"));

        put.addColumn(Bytes.toBytes("col1"), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));
        put.addColumn(Bytes.toBytes("col1"), Bytes.toBytes("qual2"), Bytes.toBytes("val2"));

        table.put(put);

        table.close();
        connection.close();

        helper.close();
    }
}
