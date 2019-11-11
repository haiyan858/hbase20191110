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
 * 例4.1 使用过滤器来挑选特定的行
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-11 23:33
 */
public class RowFilterExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1", "colfam2");
        System.out.println("Adding rows to table...");
        helper.fillTable("testtable", 1, 100, 100, "colfam1", "colfam2");

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));


        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes("colfam1"),Bytes.toBytes("col-0"));

        //创建过滤器，指定比较运算符和比较器，精确匹配
        Filter filter1 = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, //小于等于给定的值
                new BinaryComparator(Bytes.toBytes("row-22")));
        scan.setFilter(filter1);
        ResultScanner scanner1 = table.getScanner(scan);
        for(Result res: scanner1){
            System.out.println(res);
        }
        scanner1.close();


        Filter filter2 = new RowFilter(CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator(".*-.5")); //正则匹配行键
        scan.setFilter(filter2);
        ResultScanner scanner2 = table.getScanner(scan);
        for(Result res: scanner2){
            System.out.println(res);
        }
        scanner2.close();


        Filter filter3 = new RowFilter(CompareFilter.CompareOp.EQUAL,
                new SubstringComparator("-5")); //子串匹配
        scan.setFilter(filter3);
        ResultScanner scanner3 = table.getScanner(scan);
        for(Result res: scanner3){
            System.out.println(res);
        }
        scanner3.close();


    }
}
