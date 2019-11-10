HBase 之客户端API 基础知识
代码 https://github.com/larsgeorge/hbase-book

org.apache.hadoop.hbase.client.HTable类 CRUD操作
	推荐：用户只创建一次HTable实例，而且是每个线程创建一个，然后在客户端应用的生存期内复用
		  如果需要多个HTable 实例，可以使用HTableTool类
	修改操作只保证行级别的原子性（同时保护到所有列）

3.2 CRUD 操作
	3.2.1 put方法：单行和多行
		1、单行put：
			创建Put实例
				put(byte[] row, long ts, RowLock rowlock)
				行键：转为byte[]数组：Bytes类提供
					static byte[] toBytes[ByteBuffer bb]
					static byte[] toBytes[String s]
					static byte[] toBytes[boolean b]
					...
				添加数据: 添加一列数据
					Put add(byte[] family, byte[] qualifier, byte[] value)
					Put add(byte[] family, byte[] qualifier, long ts, byte[] value)
					Put add(KeyValue kv) throws IOException
						KeyValue 实例代表了一个唯一的数据单元格
					获取KeyValue实例，调用get()方法
						List<KeyValue> get(byte[] family, byte[] qualifier)
					遍历Put实例中每一个可用的KeyValue 实例
					Map<byte[] , List<KeyValue>>getFamilyMap()
					KeyValue实例包含完整地址(行键，列族，列限定符及时间戳) 和实际数据，是最底层类

		2、KeyValue类：
			构造函数
				KeyValue(byte[] row, int roffset, int rlength,
						byte[] family, int foffset,int flength,
						byte[] qualifier,int qoffset, int qlength,
						long timestamp, Type type,
						byte[] value, int voffset,int vlength)

				上面的成员都有对应的getter方法，可以获取字节数组以及offset和length
			byte[] getRow() 行键，Put构造器中的row参数（常用）
			byte[] getKey() 键，一个单元格的坐标，用的是原始的字节数组格式
			比较器：内部类，实现了Comparator接口的
				KeyComparator
				KVComparator ，静态实例 KV_COMPARATOR 直接访问使用
				RowComparator
				MetaKeyComparator
				MetaComparator
				RootKeyComparator
				RootComparator
			创建一个KeyValue实例的集合：
				TreeSet<KeyValue> set = new TreeSet<KeyValue>(KeyValue.COMPARATOR)
			KeyValue 实例还有一个变量，代表该实例的唯一坐标：类型
				KeyValue实例所有可能的类型值：
					Put
					Delete 墓碑标记
					DeleteColumn
					DeleteFamily
				查看KeyValue实例的类型：
					String toString()
						打印的内容为：<row-key>/<family>:<qualifier>/<version>/<type>/<value-length>

		3、客户端的写缓冲区
			每一个put操作实际上都是一个RPC操作，它将客户端数据传送到服务器然后返回。
			写缓冲区 write buffer ，负责收集put操作，然后调用RPC操作一次性将put送往服务器
				全局交换机控制着该缓冲区是否在使用
				void setAutoFlush(boolean autoFlush)
				boolean isAutoFlush()
				默认情况下，客户端缓冲区是禁用的，可以通过将自动刷写autoflush设置为false激活缓冲区
					table.setAutoFlush(false) //激活写缓冲区
				配置写缓冲区的大小,默认2MB
					long getWriteBufferSize()
					void setWriteBufferSize(long writeBufferSize) throws IOException
					给每一个用户创建的HTable实例都设置缓冲区的大小十分麻烦，为了避免这个麻烦，可以
					在配置文件 hbase-site.xml 中添加一个较大的预设值: 20MB
						<property>
							<name>hbase.client.write.buffer</name>
							<value>20971520</value>
						</property>
			缓冲区刷写：
				显式刷写 table.flushCommits()
				隐式刷写 调用方法 put() 或者 setWriteBufferSize() 触发/ HTable类的close()

		4、Put列表
			客户端API可以插入单个Put实例，同时也有批量处理操作的高级特性，其调用形式如下：
				void put(List<Put> puts) throws IOException
		5、原子性操作 compare-and-set
			特别的put调用，保证自身操作的原子性：检查写 check and put
			boolean checkAndPut(byte[] row, byte[] family, byte[] qualifier, byte[] value, Put put) throws IOException
			只能检查和修改同一行数据

	3.2.2 get方法
		1、单行get
			Result get(Get get) throws IOException
			构造Get实例：
				Get(byte[] row)
				Get(byte[] row, RowLock rowlock)
		2、Result类
		3、Get 列表
			Result[] get(List<Get> gets) throws IOException
		4、获取数据的相关方法
			boolean exists(Get get) throws IOException
			Result getRowOrBefore(byte[] row, byte[] family) throws IOException

	3.2.3 删除方法
		1、单行删除
			Delete实例
				Delete(byte[] row)
				Delete(byte[] row , long timestamp, RowLock rowLock)
			缩小要删除的给定行中涉及数据的范围
				Delete deleteFamily(byte[] family)
				Detele deleteFamily(byte[] family, long timestamp)
				Delete deleteColumns(byte[] family, byte[] qualifier)
				Delete deleteColumns(byte[] family, byte[] qualifier, long timestamp)
				Delete deleteColumn(byte[] family, byte[] qualifier)
				Delete deleteColumn(byte[] family, byte[] qualifier, long timestamp)
				void setTimeStamp(long timestamp)

		2、Delete列表
			包含Delete实例的列表
			void delete(List<Delete> deletes) throws IOException
			
		3、原子性操作 compare-and-delete
			boolean checkAndDelete(byte[] row, byte[] family, byte[] qualifier, 
									byte[] value, Delete delete) throws IOException
			如果检查失败,则不执行删除操作,调用返回false
			如果检查成功,则执行删除操作,调用发挥true
		
3.3批量处理操作
	批量处理跨多行的不同操作
	基于列表的删除或者查询,都是基于batch()方法实现的
	
	batch(List<? extends Row> actions, Object[] results) 
	//Method that does a batch call on Deletes, Gets, Puts, Increments, Appends, RowMutations.
	Row类下的子类: Put、Get、Delete 等等

3.4行锁
	服务器端隐式加锁: region 服务器提供了一个行锁的特性, 保证了只有一个客户端获取一行数据相应的锁,同时对该行进行修改
	客户端显式加锁: 
		RowLock lockRow(byte[] row) throws IOException //加锁:指定行键
		void unlockRow(RowLock rl) throws IOExeptionn //自动释放锁或者锁的租期超时
		默认的锁超时时间是1分钟, 可以修改默认值 hbase-site.xml 为2分钟: 
			<property>
				<name>hbase.regionserver.lease.period</name>
				<value>120000</value> 
			</property>




3.5扫描
	3.5.1 什么是扫描
	3.5.2 ResultScanner类
	3.5.3 缓存与批量处理
3.6各种特性
	3.6.1 HTable的实用方法
	3.6.2 Bytes类
	