

HBASE 原理和设计
http://lxw1234.com/archives/2016/09/719.htm

架构： zk群、Master群、RegionServer群

数据组织：列簇：分开存储

原理：
	RegionServer的定位：
		Client 客户端通过访问ZK 来请求目标数据的地址
		三层索引：从zk获取-ROOT-信息，再从-ROOT-获取 .META. 表信息，最后从.META.表中查到RS地址后缓存
		Client获取到目标RS地址后，直接向该地址发送数据请求
	Region数据写入：
		数据写入采用WAL( write ahead log)的形式: 先写log，再写数据
		（HBASE 是一个append类型的数据库，所以记录HLog的操作都是简单的put操作-delete/update 都被转化为put进行）
	HLog
		HLog写入：HBASE实现WAL方式产生的日志信息，其内部是一个简单的顺序日志；
				主要作用：在RS出现意外崩溃的时候，可以尽量多的恢复数据
		HLog过期：HLog的清理，HLog的监控线程 hbase.master.cleaner.interval 配置周期
	Memstore：是Region的内部缓存，hbase.hregion.menstore.flush.size 配置
		数据存储：RS写完HLog以后，数据写入的下一个目标就是region的memstore
				(memstore是LSM-Tree 结构组织，所以能够合并大量对于相同rowkey上的更新操作)
		数据刷盘：memstore中数据在一定条件下进行刷写操作，使数据持久化到相应的存储设备上
				  触发memstore刷盘的操作方式：
						1、通过全局内存控制，触发memstore刷盘
							memstore整体内存占用上限 hbase.regionserver.global.memstore.upperLimit 配置
							在内存下降到指定值之后停止刷盘操作 hbase.regionserver.global.memstore.lowerLimit 配置
						2、手动触发memstore刷盘：API
						3、memstore上限触发数据刷盘：memstore大小 hbase.hregion.memstore.flush.size 配置
		刷盘的影响：
				  直接影响：在数据刷盘开始到结束的这段时间内，该region上的访问都是被拒绝的(加锁了)
	StoreFile: memstore在触发刷盘之后会被写入底层存储，每次memstore的刷盘都会相应的生成一个存储文件HFile，
			   storeFile是HFile在HBase层的轻量级封装
		Compact：大量小HFile 进行文件合并，生成大的HFile（有特定的线程运行compact）
			minor compact：也叫small compact, 选取部分HFile进行compact，HFile数量满足阈值 & 选取的HFIle大小不能超过设置的值
						   阈值 hbase.hstore.compactionThreshold 参数配置
						   大小 hbase.hregion.max.filesize 参数设置
			major compact：也叫 large compact, 对整个region下相同列簇的所有HFile 进行compact
							也就是说major compact结束后，同一个列簇的HFile会被合并成一个
							但是合并过程比较长，对底层I/O的压力相对较大
							重要的功能：清理过期或者被删除的数据
		Split：多个HFile合并为单个HFile文件之后，随着数据量的不断写入，单个HFile也会越来越大，会影响查询性能
			   region的split方案来解决大的HFile造成查询时间过长问题
			   一个打的region通过split操作，会生成两个小的region，称之为 Daughter（逻辑上划分，并没有设计底层数据的重组）

Hbase 设计：是一个分布式数据库，其性能的好坏主要取决于内部表的设计和资源的分配是否合理
	表的设计
		Rowkey设计：一般建议rowkey的开始部分以hash或者MD5散列，尽量做到rowkey的头部是均匀分布的，
				    禁止采用时间、用户ID等明显有分段现象的标志直接当做rowkey使用
		列簇设计：在线查询，尽量不要设计多个列簇，不同列簇在存储上是分开的
				   多列簇设计会造成在数据查询的时候读取更多的文件，从而消耗更多的I/O
		TTL设计：允许列簇定义数据过期时间，数据一旦超过过期时间，可以被major compact 进行清理
	Region设计
			region 过大会导致 major compact 调用的周期变长
			region 过小意味着更多的region split风险, region容量过小，在数量达到上限后，region需要split来拆分，
			(通过不断的split产生新的region，容易导致因为内存不足而出现OOM)
			需要前期数据量估算和region的预分配


LSM-tree 
96年论文 《the Log-Structured Merge-Tree(LSM-Tree)》
磁盘的顺序写和随机写
LSM-Tree 是一个多层结构 ，会有合并的动作（类似归并排序）

LSM-Tree 针对的是：写密集、少量查询


数据结构（Java）
https://mp.weixin.qq.com/s/6WRDutAamqkhprg6Xf49eg
数组、链表、队列、栈、集合、散列表、
树
	二叉树
	满二叉树
	完全二叉树
	平衡二叉树
	堆



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
	租约超时机制
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
		类似于数据库系统中的游标 cursor ,并利用了HBase提供的底层顺序存储的数据结构
	3.5.2 ResultScanner类
		每一行数据封装成一个Result实例,并将所有的Result实例放入一个迭代器中
		扫描器租约机制
	3.5.3 缓存与批量处理
		缓存是向行级别的操作,批量是面向列级别的操作
		一次RPC请求获取多行数据, 扫描器缓存方法可以实现
		设置表级别的扫描器缓存
			void setScannerCaching(int scannerCaching)
			int getScannerCaching()
		Scan实例的扫描器缓存大小设置
			<property>
				<name>hbase.client.scanner.caching</name>
				<value>10</value> 
			</property>
		扫描级别的缓存
			void setCaching(int caching)
			int getCaching()
		RPC请求次数与服务器端的内存消耗找到平衡点
3.6各种特性
	3.6.1 HTable的实用方法
	3.6.2 Bytes类
	




第4章 客户端API 高级特性

4.1 过滤器
	HBase内置过滤器
	自定义过滤器
	4.1.1 过滤器简介
		HBase提供的可以直接使用的类
		谓词下推(predicate push down)继承Filter类来实现自己的需求，所有的过滤器都在服务端生效
			保证被过滤掉的数据不会被传送到客户端
		可以在客户端实现过滤的功能，但会影响系统性能，应当尽量避免
		1. 过滤器层次结构
			最底层是Filter接口和FilterBase抽象类
			特殊过滤器 CompareFilter，需要提供两个参数：比较运算符和比较器
			
		2. 比较运算符
			LESS              匹配小于设定值的值
			LESS_OR_EQUAL     匹配小于或等于设定值的值
			EQUAL             匹配等于设定值的值
			NOT_EQUAL         匹配不等于设定值的值
			GREATER_OR_EQUAL  匹配大于或等于设定值的值
			GREATER           匹配大于设定值的值
			NO_OP             排除一切值
			当过滤器被应用时，比较运算符可以决定什么被包含，什么被排除，用于筛选数据的子集
			或者是一些特定数据
			
		3. 比较器 comparator
			比较器提供了多种方法来比较不同的键值
			比较器都继承自 WritableByteArrayComparable、其实现了接口： Writable和Comparable
			HBase提供的比较器(基于CompareFilter)：需要提供一个阈值
				BinaryComparator         使用Bytes.compareTo() 比较当前值与阈值
				BinaryPrefixComparator	 使用Bytes.compareTo() 比较当前值与阈值,从左端开始前缀匹配
				NullComparator            不做匹配，只判断当前值是不是null
				BitComparator             位级比较：BitWiseOp类提供的与、或、异或
				RegexStringComparator     正则表达式
				SubstringComparator       把阈值和表中数据当做String实例，同时通过contains() 匹配字符串
				后三种比较器，运算符只能搭配： EQUAL、NOT_EQUAL
			基于字符串的比较器比基于字节的比较器更慢，更消耗资源。
			截取字符串子串和正则式的处理也需要话费额外的时间
		
	4.1.2 比较过滤器 comparison filter
		1. 行过滤器 RowFilter
		2. 列族过滤器 FamilyFilter
		3. 列名过滤器 QualifierFilter
		4. 值过滤器 ValueFilter
		5. 参考列过滤器 DependentColumnFilter
		  这种过滤器于扫描操作的批量处理功能不兼容
	4.1.3 专用过滤器
		1. 单值过滤器 SingleColumnValueFilter 用一列的值决定是否一行数据被过滤
		2. 单列排除过滤器 SingleColumnValueExcludeFilter
		3. 前缀过滤器 PrefixFilter 与前缀匹配的行返回到客户端
		4. 分页过滤器 PageFilter 对结果按行分页
		5. 行键过滤器 KeyOnlyFilter 将结果中KeyValue实例的键返回，而不需要返回实际的数据
		6. 首次行键过滤器 FirstKeyOnlyFilter 访问一行中的第一列, 行数统计用到
		7. 包含结束的过滤器 InclusiveStopFilter 扫描操作中，将结束行包括到结果中
		8. 时间戳过滤器 TimestampsFilter 对扫描结果中的版本进行细粒度的控制
		9. 列计数过滤器 ColumnCountGetFilter 限制每行最多取回多少列
		10. 列分页过滤器 ColumnPaginationFilter 与PageFilter相似，对一行的所有列进行分页
		11. 列前缀过滤器 ColumnPrefixFilter 类似与PrefixFilter，对列名称进行前缀匹配过滤
		12. 随机行过滤器 RandomRowFilter 让结果中包含随机行，传入参数chance: 0.0-1.0取值
	4.1.4 附加过滤器 decorating filter
		1. 跳转过滤器 SkipFilter
		  这个过滤器包装了一个用户提供的过滤器，当被包装的过滤器遇到一个需要过滤的KeyValue实例时，
		  用户可以拓展并过滤整行数据
		  new SkipFilter(new ValueFilter());
		2. 全匹配过滤器 WhileMatchFilter
		  当一条数据被过滤掉时，直接放弃本次扫描操作
		附加过滤器与其他过滤器都实现了Filter接口，可以把自身功能和其他过滤器加以组合，变成一个有
		新功能的过滤器
	4.1.5 过滤器列表 FilterList
		多个过滤器共同限制返回到客户端的接口
		
		FilterList(List<Filter> rowFilters)
		FilterList(Operator operator)
		FilterList(Operator operator, List<Filter> rowFilters)
		参数 List<Filter> rowFilters 以列表的形式组合过滤器
		参数 Operator operator 决定了组合它们的结果
		
		FilterList.Operator 的可选枚举值
			MUST_PASS_ALL 当所有过滤器都允许包含这个值时，这个值才会被包含在结果中
				       也就是说没有过滤器会忽略这个值
			MUST_PASS_ONE  只要有一个过滤器允许包括这个值，那这个就会包含在结果中
		
	4.1.6 自定义过滤器
		实现Filter接口或者直接继承FilterBase类
		Filter.ReturnCode 的值类型
			INCLUDE 
			SKIP
			NEXT_COL
			NEXT_ROW
			SEEK_NEXT_USING_HINT 
		过滤器处理一行数据的逻辑流程，不太懂？？？
		
	4.1.7 过滤器总结
4.2 计数器
	4.2.1 计数器简介
		incr
		get_counter
	4.2.2 单计数器
		long incrementColumnValue(byte[] row,byte[] family, byte[] qualifier, long amout)
		long incrementColumnValue(byte[] row,byte[] family, byte[] qualifier, long amout, boolean writeToWAL)
		
	4.2.3 多计数器
		Result increment(Increment increment) throws IOException
		Increment()
		Increment(byte[] row)
		Increment(byte[] row, RowLock rowLock)
	
4.3 协处理器
	把一部分计算移动到数据的存放端：协处理器 coprocessor
	4.3.1 协处理器简介
	4.3.2 Coprocessor类
		Priority和State
		Coprocessor.Priority枚举类定义的优先级
		SYSTEM  高优先级，定义最先被执行的协处理器
		USER    定义其他的协处理器，按顺序执行
		协处理器的优先级决定了执行的顺序：系统SYSTEM级协处理器在用户USER级协处理器之前执行
		CoprocessorEnvironment 用来在协处理器的生命周期中保持其状态
		Coprocessor.State 枚举类定义的状态
			UNINSTALLED 协处理器最初的状态，没有环境，也没有被初始化
			INSTALLED   实例装载了它的环境参数
			STARTING    协处理器将要开始工作，也就是说start()方法将要被调用 
			ACTIVE      一旦start()方法被调用，当前状态设置为active
			STOPPING    stop()方法被调用之前的状态
			STOPPED     一旦stop()方法将控制权交给框架, 协处理器会被设置为状态stopped
		
		CoprocessorHost类，它维护所有协处理器实例和他们专用的环境，子类应用在不同的使用环境
		例如：master和region服务器等环境
		
		Coprocessor、CoprocessorEnvironment和CoprocessorHost 这3个类形成了协处理器类的基础。
		
	4.3.3 协处理器加载
		静态加载
			1.从配置中加载: 加载的协处理器影响所有表
			以下配置中的类名替换为自定义的类名
			<property>
				<name>hbase.coprocessor.region.classes</name>
				<value>coprocessor.RegionObserveExample,coprocessor.AnotherCoprocessor</value>
			</property>
			<property>
				<name>hbase.coprocessor.master.classes</name>
				<value>coprocessor.MasterObserveExample</value>
			</property>
			<property>
				<name>hbase.coprocessor.wal.classes</name>
				<value>coprocessor.WALObserverExample,bar.foo.MyWALObserver</value>
			</property>
			配置文件中的配置项的顺序非常重要，这个顺序决定了执行顺序
			
			配置项的作用：
				hbase.coprocessor.master.classes  MasterCoprocessorHost  Master服务器
				hbase.coprocessor.region.classes  RegionCoprocessorHost  Region服务器
				hbase.coprocessor.wal.classes     WALCoprocessorHost     Region服务器
			
			当一张表的region被打开时，hbase.coprocessor.region.classes 定义的协处理器会被加载。
			注意用户不能指定具体是哪张表或那个region加载这个类：默认的协处理器会被每张表和每个region加载
			
			2.从表描述符中加载
				针对特定表的region
				用户只能在与region相关的协处理器上使用这种方法，而不能在master或WAL相关的协处理器上使用
				在表描述符中利用HTableDescriptor.setValue()方法定义它们
					键：必须以COPROCESSOR开头
					值：必须符合的格式 <path-to-jar>|<classname>|<priority>
				eg:
				'COPROCESSOR$1'=>'hdfs://localhost:8020/users/leon/test.jar|coprocess.Test|SYSTEM' 系统优先级
				'COPROCESSOR$2'=>'/Users/laura/test2.jar|coprocess.AnotherTest|USER'  用户优先级
				$<number> 后缀可以改变定义中的顺序
			
		动态加载
	4.3.4 RegionObserve类
		1.处理region生命周期事件
		2.处理客户端API事件
		3.RegionCoprocessorEnvironment类
		4.ObserverContext类
		5.BaseRegionObserver类
	4.3.5 MasterObserver类
		1.MasterCoprocessorEnvironment 类
		2.BaseMasterObserver类
	4.3.6 endpoint 动态调用实现
		1.CoprocessorProtocol接口
		2.BaseEndpointCoprocessor类
4.4 HTablePool
4.5 连接管理
	共享ZooKeeper连接
	缓存通用资源

