package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 3.8 从HBase中获取数据
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:41
 */
public class GetExample {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端, 表引用
        HTable table = new HTable(conf, "testtable");

        //使用指定行键，构建一个Get实例
        Get get = new Get(Bytes.toBytes("row1"));
        // 向Get实例中添加一个列
        get.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
        Result result = table.get(get);
        byte[] val = result.getValue(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
        System.out.println("Value: " + Bytes.toString(val));
        // Value: val1

        table.close();
    }
}
