package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;


/**
 * 3.11 使用特殊检索方法
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:45
 */
public class GetExample_Special {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端, 表引用
        HTable table = new HTable(conf, "testtable");

        // 查找已经存在的行
        Result result1 = table.getRowOrBefore(Bytes.toBytes("row1"), Bytes.toBytes("colfam1"));
        System.out.println("Found: " + Bytes.toString(result1.getRow()));

        //尝试查找不存在的行
        Result result2 = table.getRowOrBefore(Bytes.toBytes("row99"), Bytes.toBytes("colfam1"));
        System.out.println("Found: " + Bytes.toString(result2.getRow()));

        //
        for (KeyValue kv : result2.raw()) {
            System.out.println("Col: " + Bytes.toString(kv.getFamily()) + "/" + Bytes.toString(kv.getQualifier()));
        }

        Result result3 = table.getRowOrBefore(Bytes.toBytes("abc"), Bytes.toBytes("colfam1"));
        System.out.println("Found: " + result3); // null

        table.close();
    }
}
