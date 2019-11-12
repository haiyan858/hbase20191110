package com.book.ch04;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 例4.18 增加一行中多个计数器的计数
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-12 21:58
 */
public class IncrementMultipleExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "daily", "weekly");

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));

        Increment increment1 = new Increment(Bytes.toBytes("20190101"));

        increment1.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("clicks"), 1);
        increment1.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);

        increment1.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("clicks"), 10);
        increment1.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("hits"), 10);

        Result result1 = table.increment(increment1);
        for (KeyValue kv : result1.raw()) {
            System.out.println("KV: " + kv + " Value: " + kv.getValue());
        }
        //输出如下：
        //KV: 20190101/daily:clicks/1301948275827/Put/vlen=8 value:1
        //KV: 20190101/daily:hits/1301948275827/Put/vlen=8 value:1
        //KV: 20190101/weekly:clicks/1301948275827/Put/vlen=8 value:10
        //KV: 20190101/weekly:hits/1301948275827/Put/vlen=8 value:10

        Increment increment2 = new Increment(Bytes.toBytes("20190101"));

        increment2.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("clicks"), 5);
        increment2.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);

        increment2.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("clicks"), 0);
        increment2.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("hits"), -5);

        Result result2 = table.increment(increment1);
        for (KeyValue kv : result2.raw()) {
            System.out.println("KV: " + kv + " Value: " + kv.getValue());
        }
        //输出如下：
        //KV: 20190101/daily:clicks/1301948275827/Put/vlen=8 value:6
        //KV: 20190101/daily:hits/1301948275827/Put/vlen=8 value:2
        //KV: 20190101/weekly:clicks/1301948275827/Put/vlen=8 value:10
        //KV: 20190101/weekly:hits/1301948275827/Put/vlen=8 value:5


    }
}
