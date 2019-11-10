package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;


/**
 * 3.7 使用原子性操作 compare-and-set
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:40
 */
public class PutExample_CheckAndPut {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端
        HTable table = new HTable(conf, "testtable");

        //指定一行来创建一个put
        Put put1 = new Put(Bytes.toBytes("row1"));
        //向Put中添加一个名为 colfam1:qual1 的列
        put1.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));

        //检查列是否存在，按检查的结果决定是否执行put操作
        boolean res1 = table.checkAndPut(Bytes.toBytes("row1"),
                Bytes.toBytes("colfam1"),
                Bytes.toBytes("qual1"),
                null, put1);
        System.out.println("Put applied: " + res1);
        // Put applied: true

        boolean res2 = table.checkAndPut(Bytes.toBytes("row1"),
                Bytes.toBytes("colfam1"),
                Bytes.toBytes("qual1"),
                null, put1);
        System.out.println("Put applied: " + res2);
        // 因为那个列的值已经存在, Put applied: false


        Put put2 = new Put(Bytes.toBytes("row1"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put2.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"), Bytes.toBytes("val2"));
        boolean res3 = table.checkAndPut(Bytes.toBytes("row1"),
                Bytes.toBytes("colfam1"),
                Bytes.toBytes("qual1"),
                Bytes.toBytes("val1"), put2);// 当上一次的put值存在时，写入新的值
        System.out.println("Put applied: " + res3);
        // Put applied: true


        Put put3 = new Put(Bytes.toBytes("row2"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put3.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val3"));
        boolean res4 = table.checkAndPut(Bytes.toBytes("row1"),
                Bytes.toBytes("colfam1"),
                Bytes.toBytes("qual1"),
                Bytes.toBytes("val1"), put3);// 检查一个不同行的值是否相等，然后写入另一行
        System.out.println("Put applied: " + res4); // 执行不到这里，上一行报错
        // Action's getRow must match the passed row
        //只能检查和修改同一行数据

        table.close();
    }
}
