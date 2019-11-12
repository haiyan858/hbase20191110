package com.book.ch04.coprocessor;

import com.book.util.HBaseHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * 例4.19 检查特定get请求的region observer
 *
 * @Author cuihaiyan
 * @Create_Time 2019-11-12 22:14
 */
public class LoadWithTableDescriptorExample {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);

        HBaseHelper helper = HBaseHelper.getHelper(conf);
        helper.dropTable("testtable");

        TableName tableName = TableName.valueOf("testtable");

        FileSystem fs = FileSystem.get(conf);

        //得到包含协处理器实现的JAR文件的地址
        Path path = new Path(fs.getUri() + Path.SEPARATOR + "test.jar");

        HTableDescriptor htd = new HTableDescriptor(tableName);
        htd.addFamily(new HColumnDescriptor("colfam1"));
        //将协处理器添加到表描述符中
        htd.setValue("COPROCESSOR$1", path.toString() +
                "|" + RegionObserverExample.class +
                "|" + Coprocessor.PRIORITY_USER);

        //添加集群的管理API并添加这个表
        Admin admin = connection.getAdmin();
        admin.createTable(htd);

        System.out.println(admin.getTableDescriptor(tableName)); // co LoadWithTableDescriptorExample-4-Check Verify if the definition has been applied as expected.
        admin.close();
        connection.close();

    }

}
