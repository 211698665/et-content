package cn.henu.jedis;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.henu.common.jedis.JedisClient;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class JedisTest {
	@Test
	public void testJedis()throws Exception{
		//创建一个jedis对象，参数host和port
		Jedis jedis = new Jedis("192.168.25.129",6379);
		String string = jedis.get("key1");
		System.out.println(string);
		//关闭连接
		jedis.close();
	}
	@Test
	public void testJedisPool() {
		//创建一个连接池对象
		JedisPool jedisPool = new JedisPool("192.168.25.129",6379);
		//从连接池获得一个连接
		Jedis jedis = jedisPool.getResource();
		//使用jedis操作redis
		String string = jedis.get("key1");
		System.out.println(string);
		//每次使用完毕后，关闭连接连接池回收资源
		jedis.close();
		//关闭连接池，连接池单例的
		jedisPool.close();
	}
	
	@Test
	public void testJedisCluster() throws Exception{
		//创建一个jedisCluster对象，有一个参数nodes是一个set类型，set中包含若干个HostAndPort对象
		Set<HostAndPort> nodes = new HashSet<>();
		nodes.add(new HostAndPort("192.168.25.129",7001));
		nodes.add(new HostAndPort("192.168.25.129",7002));
		nodes.add(new HostAndPort("192.168.25.129",7003));
		nodes.add(new HostAndPort("192.168.25.129",7004));
		nodes.add(new HostAndPort("192.168.25.129",7005));
		nodes.add(new HostAndPort("192.168.25.129",7006));
		JedisCluster jedisCluster = new JedisCluster(nodes);
		jedisCluster.set("RUAN", "JIAN");
		String string = jedisCluster.get("RUAN");
		System.out.println(string);
		//关闭JedisCluster对象
		jedisCluster.close();
		
	}
	//测试单机版和集群版的redis，代码是一样的，测试集群的时候只需要把配置文件里的单机的配置文件注释掉就行了
	@Test
	public void testJedisClient() throws Exception{
		//初始化一个spring容器
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-jedis.xml");
		//获得jedisClient对象
		JedisClient jedisClient = applicationContext.getBean(JedisClient.class);
		jedisClient.set("mytest","123");
		String string = jedisClient.get("mytest");
		System.out.println(string);
	}
}
