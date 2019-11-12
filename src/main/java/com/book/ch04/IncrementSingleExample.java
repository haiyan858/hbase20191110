package com.book.ch04;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 例4.17 使用单计数器自增方法
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-12 21:54
 */
public class IncrementSingleExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseHelper helper = HBaseHelper.getHelper(conf);

        helper.dropTable("testtable");
        helper.createTable("testtable", "daily");
        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));

        HTable htable = new HTable(conf, "counters");
        // 计数器值加1
        long cnt1 = table.incrementColumnValue(Bytes.toBytes("2011010"), Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);
        // 计数器值再加1
        long cnt2 = table.incrementColumnValue(Bytes.toBytes("2011010"), Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);
        // 得到当前计数器值，不做自增操作
        long current = table.incrementColumnValue(Bytes.toBytes("2011010"), Bytes.toBytes("daily"), Bytes.toBytes("hits"), 0);
        // 计数器值减1
        long cnt3 = table.incrementColumnValue(Bytes.toBytes("2011010"), Bytes.toBytes("daily"), Bytes.toBytes("hits"), -1);

        System.out.println("cnt1: " + cnt1 + ", cnt2: " + cnt2 + ", current: " + current + ", cnt3: " + cnt3);
        // cnt1: 1
        // cnt2: 2
        // current: 2
        // cnt3: 1
    }
}
