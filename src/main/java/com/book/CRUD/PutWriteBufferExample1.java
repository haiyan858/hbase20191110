package com.book.CRUD;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 3.3 Hbase 缓冲区强制刷写数据的示例
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-10 17:16
 */
public class PutWriteBufferExample1 {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        // 实例化一个新的客户端
        HTable table = new HTable(conf,"testtable");
        System.out.println("Auto Flush: "+ table.isAutoFlush());// Auto Flush: true
        table.setAutoFlush(false); //激活写缓冲区

        //指定一行来创建一个put
        Put put1 = new Put(Bytes.toBytes("row1"));
        //向Put中添加一个名为 colfam1:qual1 的列
        put1.add(Bytes.toBytes("colfam1"),Bytes.toBytes("qual1"),Bytes.toBytes("val1"));
        table.put(put1);

        Put put2 = new Put(Bytes.toBytes("row2"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put2.add(Bytes.toBytes("colfam1"),Bytes.toBytes("qual1"),Bytes.toBytes("val2"));
        table.put(put2);

        Put put3 = new Put(Bytes.toBytes("row3"));
        //向Put中添加一个名为 colfam1:qual2 的列
        put2.add(Bytes.toBytes("colfam1"),Bytes.toBytes("qual1"),Bytes.toBytes("val3"));
        table.put(put3);

        Get get = new Get(Bytes.toBytes("row1"));
        Result res1 = table.get(get);
        //试图加载先前存储的行，结果会打印出:
        System.out.println("Result: "+res1); //Result: keyvalues=NONE

        //强刷缓冲区，会产生一个RPC请求
        table.flushCommits();

        Result res2 = table.get(get);
        //可以被读取
        System.out.println("Result: "+res2); //Result:keyvalues={row1/colfam1:qual1/1300267114099/Put/vlen=4}


        table.close();


    }
}
