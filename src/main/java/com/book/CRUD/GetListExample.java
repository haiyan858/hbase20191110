package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 3.9 从HBase中获取数据
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:41
 */
public class GetListExample {
    public static void main(String[] args) throws IOException {
        // 创建所需的配置
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端, 表引用
        HTable table = new HTable(conf, "testtable");

        byte[] cf1 = Bytes.toBytes("colfam1");
        byte[] qf1 = Bytes.toBytes("qual1");
        byte[] qf2 = Bytes.toBytes("qual2");//共用的字节数组
        byte[] row1 = Bytes.toBytes("row1");
        byte[] row2 = Bytes.toBytes("row2");

        List<Get> gets = new ArrayList<Get>();//Get实例列表

        Get get1 = new Get(row1);
        get1.addColumn(cf1, qf1);
        gets.add(get1);

        Get get2 = new Get(row2);
        get1.addColumn(cf1, qf1); //Get实例添加到列表
        gets.add(get2);

        Get get3 = new Get(row2);
        get1.addColumn(cf1, qf2);
        gets.add(get3);

        Result[] results = table.get(gets); //从Hbase中获取这些行和选定的列

        System.out.println("First iteration...");
        for (Result result : results) {
            String row = Bytes.toString(result.getRow());
            System.out.print("Row: " + row + " ");

            byte[] val = null;
            if (result.containsColumn(cf1, qf1)) { // 遍历结果并检查那些行中
                val = result.getValue(cf1, qf1);
                System.out.println("Value: " + Bytes.toString(val));
            }

            if (result.containsColumn(cf1, qf2)) { // 遍历结果并检查那些行中
                val = result.getValue(cf1, qf2);
                System.out.println("Value: " + Bytes.toString(val));
            }
        }

        System.out.println("Second iteration...");
        for (Result result : results) {
            for (KeyValue kv : result.raw()) {
                System.out.println("Row: " + Bytes.toString(kv.getRow()) + "Value: " + Bytes.toString(kv.getValue()));
            }
        }
    }
}
