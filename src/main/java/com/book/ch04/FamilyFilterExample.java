package com.book.ch04;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 例4.2 使用过滤器返回特定的列族
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-11 23:33
 */
public class FamilyFilterExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1", "colfam2", "colfam3", "colfam4");
        System.out.println("Adding rows to table...");
        helper.fillTable("testtable", 1, 10, 2, "colfam1", "colfam2", "colfam3", "colfam4");

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));


        Filter filter1 = new FamilyFilter(CompareFilter.CompareOp.LESS,
                new BinaryComparator(Bytes.toBytes("colfam3")));
        Scan scan = new Scan();
        scan.setFilter(filter1);
        ResultScanner scanner = table.getScanner(scan); //使用过滤器扫描表
        for(Result result: scanner){
            System.out.println(result);
        }
        scanner.close();

        Get get1 = new Get(Bytes.toBytes("row-5"));
        get1.setFilter(filter1);
        Result result1 = table.get(get1);
        System.out.println("Result of get(): "+result1);

        Filter filter2 = new FamilyFilter(CompareFilter.CompareOp.EQUAL,
                new BinaryComparator(Bytes.toBytes("colfam3")));
        Get get2 = new Get(Bytes.toBytes("row-5"));
        get2.addFamily(Bytes.toBytes("colfam1"));
        get2.setFilter(filter2);
        Result result2 = table.get(get2);
        System.out.println("Result of get(): "+result2);

    }
}
