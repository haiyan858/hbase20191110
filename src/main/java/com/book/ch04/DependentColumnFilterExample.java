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
 * 4.5 使用参考列过滤器，返回一些特定的列
 * DependentColumnFilter(byte[] family, byte[] qualifier, boolean dropDependentColumn) ...
 * dropDependentColumn 参数操作参考列： false or true 决定了参考列可以被返回还是被丢弃
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-11 23:33
 */
public class DependentColumnFilterExample {

    private static Table table = null;

    private static void filter(boolean drop, CompareFilter.CompareOp operator,
                               ByteArrayComparable comparator) throws IOException {
        Filter filter;
        if (operator != null) {
            filter = new DependentColumnFilter(Bytes.toBytes("colfam1"), Bytes.toBytes("col-5"), drop,
                    operator, comparator);
        } else {
            filter = new DependentColumnFilter(Bytes.toBytes("colfam1"), Bytes.toBytes("col-5"), drop);
        }

        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = table.getScanner(scan);
        System.out.println("Results of scan:");
        for (Result result : scanner) {
            for (Cell cell : result.rawCells()) {
                System.out.println("Cell: " + cell + ", Value: " +
                        Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
        }
        scanner.close();

        Get get = new Get(Bytes.toBytes("row-5"));
        get.setFilter(filter); //将同样的过滤器应用于Get实例
        Result result = table.get(get);
        for (Cell cell : result.rawCells()) {
            System.out.println("Cell: " + cell + ", Value: " +
                    Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
        }
    }

    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1", "colfam2");
        System.out.println("Adding rows to table...");
        helper.fillTable("testtable", 1, 10, 10, true, "colfam1", "colfam2");

        Connection connection = ConnectionFactory.createConnection(conf);
        table = connection.getTable(TableName.valueOf("testtable"));


        filter(true,CompareFilter.CompareOp.NO_OP,null);
        filter(false,CompareFilter.CompareOp.NO_OP,null);

        filter(true,CompareFilter.CompareOp.EQUAL,new BinaryPrefixComparator(Bytes.toBytes("val-5")));
        filter(false,CompareFilter.CompareOp.EQUAL,new BinaryPrefixComparator(Bytes.toBytes("val-5")));

        filter(true,CompareFilter.CompareOp.EQUAL,new RegexStringComparator(".*\\.5"));
        filter(false,CompareFilter.CompareOp.EQUAL,new RegexStringComparator(".*\\.5"));


    }
}
