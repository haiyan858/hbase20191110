package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 3.6 向Hbase中插入一个空的Put实例
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:36
 */
public class PutEmptyExample {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端
        HTable table = new HTable(conf, "testtable");

        //创建一个列表用于存储Put实例
        List<Put> puts = new ArrayList<Put>();

        //指定一行来创建一个put
        Put put1 = new Put(Bytes.toBytes("row1"));
        //向Put中添加一个名为 colfam1:qual1 的列
        put1.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));
        puts.add(put1);

        Put put2 = new Put(Bytes.toBytes("row2"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put2.add(Bytes.toBytes("BOGUS"), Bytes.toBytes("qual1"), Bytes.toBytes("val2"));
        puts.add(put2);

        Put put3 = new Put(Bytes.toBytes("row2"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put2.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"), Bytes.toBytes("val3"));
        puts.add(put3);

        Put put4 = new Put(Bytes.toBytes("row2"));
        puts.add(put4); // 没有内容的Put

        // 捕获本地异常然后提交更新
        try {
            table.put(puts);
        } catch (Exception e) {
            System.err.println("Error: " + e);
            table.flushCommits();
        }

        table.close();
    }
}
