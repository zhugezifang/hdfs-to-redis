package com.vince.xq;

import com.vince.xq.utils.CommonParaUtil;
import com.vince.xq.utils.RedisInstance;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class HdfsToRedis {
    private static int totalQps = 100;

    public static void main(String[] args) throws IOException {

        Properties properties = CommonParaUtil.paraUtil();
        if (properties.getProperty("qps") != null) {
            totalQps = Integer.parseInt(properties.getProperty("qps"));
        }

        SparkSession spark = SparkSession.builder()
                .appName("Spark-to-redis")
                .master("local")
                .getOrCreate();
        String filePath = "src/main/resources/input";
        if (properties.getProperty("hdfs.input.path") != null) {
            filePath = properties.getProperty("hdfs.input.path");
        }
        JavaRDD<String> lines = spark.read().textFile(filePath).javaRDD();

        JavaRDD<Row> result = lines.map(new Function<String, Row>() {
            @Override
            public Row call(String line) throws Exception {
                String[] str = line.split(" ");
                return RowFactory.create(str[0], str[1]);
            }
        });
        int partition = result.getNumPartitions();
        LongAccumulator totalInsert= jsc.sc().longAccumulator("fooCount");
        //System.out.println("par1:" + result.getNumPartitions());
        //result=result.repartition(2);
        //System.out.println("par2:" + result.getNumPartitions());
        int requiredQps = totalQps / partition;
        System.out.println("total:" + result.count());


        result.foreachPartition(it -> {
            Jedis jedis = RedisInstance.getInstance(properties.getProperty("redis.ip"), Integer.parseInt(properties.getProperty("redis.port")), properties.getProperty("redis.pwd"));
            System.out.println(it.hashCode());
            Pipeline pipeline = jedis.pipelined();
            AtomicLong atomicLong = new AtomicLong();
            long start = System.currentTimeMillis();
            it.forEachRemaining(v -> {
                        //System.out.println(v.getString(0)+":"+v.getString(1));
                        atomicLong.incrementAndGet();
                        qpsControll(start, requiredQps, atomicLong, it.hashCode());
                        pipeline.sadd(v.getString(0), v.getString(1));
                        totalInsert.add(1L);
                        if (atomicLong.get() % 1000 == 0) {
                            //每1000条提交一次
                            pipeline.sync();
                        }
                    }
            );
            pipeline.close();
            jedis.close();
        });
        //System.out.println(count.value());
        System.out.println("======insert count:"+totalInsert.value());
        spark.stop();
    }

    private static void qpsControll(long start, int requiredQps, AtomicLong count, int x) {
        //System.out.println("current count:"+x+":"+ count.get());
        long actualQps = 1000 * count.get() / (System.currentTimeMillis() - start);
        System.out.println(x + ":" + actualQps);
        if (actualQps > (long) requiredQps) {
            System.out.println("=====stop =====");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }


}

