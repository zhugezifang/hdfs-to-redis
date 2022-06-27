# hdfs-to-redis(qps control)

#### 介绍
HDFS-to-Redis 是一个大量数据写入redis的限流工具\
因为工作中经常会遇到数据预热缓存的需求，但是由于预热的数据量太大(几个亿的数据)，写入缓存经常出现问题\
能用最短的时间的将大量数据尽快写入缓存(耗时短)，但是会遇到缓存有数据写入限制，会出现瞬时流量高峰，因此需要进行流量限制\
#### 软件架构
软件架构说明\
1.利用spark rdd分区特性，实现高效并行写入缓存，做到耗时短\
2.对于较大流量数据，控制qps，暂停3s，实现流量控制，避免流量太高，写入redis出现问题
#### 使用说明
配置文件config.properties

1.redis.ip redis连接信息\
2.redis.port redis 端口信息\
3.redis.pwd redis 密码信息\
4.qps 需要控制的qps\
5.hdfs.input.path 需要写入的数据的路径(以key,value接口进行存储)