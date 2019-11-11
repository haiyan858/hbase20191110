package com.book.ch04;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 例4.4 使用值过滤器
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-11 23:33
 */
public class ValueFilterExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1", "colfam2");
        System.out.println("Adding rows to table...");
        helper.fillTable("testtable", 1, 10, 10, "colfam1", "colfam2");

        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection.getTable(TableName.valueOf("testtable"));


        Filter filter = new ValueFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(".4"));

        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = table.getScanner(scan);

        System.out.println("Results of scan:");
        for (Result result : scanner) {
//            for (KeyValue kv : result.raw()) {
//                System.out.println("KV: " + kv + ",Value: " + Bytes.toString(kv.getValue()));
//            }
            for (Cell cell : result.rawCells()) {
                System.out.println("Cell: " + cell + ", Value: " + Bytes.toString(cell.getValueArray(),
                        cell.getValueOffset(), cell.getValueLength()));
            }
        }
        scanner.close();


        Get get = new Get(Bytes.toBytes("row-5"));
        get.setFilter(filter); //将同样的过滤器应用于Get实例
        Result result = table.get(get);
//        for (KeyValue kv : result.raw()) {
//            System.out.println("KV: " + kv + ",Value: " + Bytes.toString(kv.getValue()));
//        }
        for (Cell cell: result.rawCells()){
            System.out.println("Cell: " + cell + ", Value: " + Bytes.toString(cell.getValueArray(),
                    cell.getValueOffset(), cell.getValueLength()));
        }

    }
}
