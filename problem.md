### 原始的 RAG 确实和 Memory 有冲突，所以必须引入“查询重写（Query Rewriting）”来调和。

#### **为什么会有冲突？**

1. **Memory（记忆）的工作原理**：
   - AI 记住了你上文说的是“可颂”。
   - 所以当你下文只说“多少钱”时，AI 脑子里会自动补全成“可颂多少钱”并回答你。
   - **Memory 存在于 AI 的上下文窗口中**。
2. **RAG（检索）的工作原理**：
   - 检索器（Retriever）是一个**无状态的搜索引擎**。
   - 它只看**当前这一句**查询词。
   - 如果你只传“多少钱”给检索器，它不知道上文是可颂，只能去数据库里把所有跟“价格”有关的文档都捞出来（可能捞出马克杯、咖啡豆的价格）。
3. **冲突点**：
   - **AI 脑子清楚（有 Memory），但眼睛瞎了（检索到了错误资料）。**
   - AI 虽然知道你在问可颂，但它手边的参考资料里全是马克杯和咖啡豆的价格（因为检索器只搜了“多少钱”）。
   - 这时候 AI 就会陷入混乱：是该回答可颂（但没资料）？还是回答马克杯（有资料但用户没问）？

#### **怎么解决冲突？—— 查询重写**

查询重写（Query Rewriting）就是**连接 Memory 和 RAG 的桥梁**。

- **第一步（利用 Memory）**： 先让一个轻量级 AI 看一眼历史记录（Memory）和当前问题（“多少钱”）。 AI 说：“哦，根据上文，他其实想问的是‘可颂多少钱’。” -> **这就是重写**。
- **第二步（服务 RAG）**： 拿着这个补全后的完美句子“可颂多少钱”，去检索器里搜。 这时候检索器就能精准捞出“可颂”的文档了。

#### 总结

并不是 RAG 和 Memory 互斥，而是**检索器（Retriever）太笨，不懂上下文**。

所以我们必须多加一层中间件（查询重写），**把“隐含上下文的问题”翻译成“独立完整的查询语句”**，这样检索器才能听懂，RAG 才能和 Memory 完美配合。这就是我们在代码里加 `simpleChatClient` 做重写的根本原因。





### Redis客户端之间的冲突

#### **为什么会有冲突？**

通知/系统模块:使用spring-boot-starter-data-redis，其默认为Lettuce客户端用于通用缓存和WebSocket会话。
Al模块:spring-ai-redis (版本1.0.0)依赖Jedis进行向量操作。当两者同时存在时，Spring Boot 的自动配置功能可能会变得混乱，或者Lettuce的配置可能"胜出"，导致AI模块无法获得其所期望的特定VectorStore bean (这需要与Jedis连接)。

#### 怎么解决冲突？

手动定义了VectorStore Bean，明确为AI向量存储创建了一个JedisPooled客户端，确保它不与应用程序其余部分使用的Lettuce客户端冲突。





### 缓存穿透

1. 设置null值

   ```java
   // .disableCachingNullValues(); // 注释掉这行，允许缓存 null
   ```

2. 布隆过滤器

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version> 
</dependency>
```

```java
package com.coffee.common.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 定义商品 ID 的布隆过滤器
     */
    @Bean
    public RBloomFilter<Long> productBloomFilter() {
        // 1. 获取布隆过滤器实例 (Redis Key 名称)
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("bloom:product:ids");

        // 2. 初始化 (如果 Redis 里还没有这个 Key)
        // tryInit(预计元素数量, 期望误判率)
        // 例如：预计 100 万商品，误判率 0.03 (3%)
        bloomFilter.tryInit(1000000L, 0.03);

        return bloomFilter;
    }
}
```

```java
package com.coffee.system.runner;

import com.coffee.system.service.ProductService;
import com.coffee.system.domain.entity.Product;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 项目启动时，把数据库里现有的商品 ID 全部加载到布隆过滤器
 */
@Component
public class BloomFilterRunner implements CommandLineRunner {

    @Autowired
    private RBloomFilter<Long> productBloomFilter;
    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) throws Exception {
        // 1. 查询所有商品 ID (为了性能，建议只查 ID 字段)
        List<Product> list = productService.list();
        
        // 2. 批量添加
        for (Product product : list) {
            productBloomFilter.add(product.getId());
        }
        System.out.println("布隆过滤器预热完成，加载商品数量：" + list.size());
    }
}
```

```java
@Autowired
private RBloomFilter<Long> productBloomFilter;

@Override
// 注意：如果用了布隆过滤器，这里就可以不用缓存空值了 (.disableCachingNullValues 也可以开启)
public ProductDetailVO getMenuDetail(Long id) {
    // 【核心拦截】先问布隆过滤器：ID 是否存在？
    // 如果返回 false，说明绝对不存在，直接返回 null，拦截穿透！
    if (!productBloomFilter.contains(id)) {
        log.warn("布隆过滤器拦截非法请求，ID: {}", id);
        return null;
    }

    // ... 下面继续查 Redis、查数据库的逻辑 ...
    // @Cacheable 会自动处理
    return super.getMenuDetail(id);
}
```

```java
// 在 ProductService 的 save 方法中
public boolean saveProduct(Product product) {
    boolean success = this.save(product);
    if (success) {
        // 同步添加到布隆过滤器
        productBloomFilter.add(product.getId());
    }
    return success;
}
```

### 缓存雪崩

```java
Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>();

// 【重点】给用户信息 (CacheKeyConstants.User.INFO) 设置 7 天过期
// 注意：这里的 Key 字符串必须和你 @Cacheable(value="...") 里的 value 一模一样
initialCacheConfigurations.put("user:info", config.entryTtl(Duration.ofDays(7)));

// 如果有字典数据，可以设置永久或更长
initialCacheConfigurations.put("system:dict", config.entryTtl(Duration.ofDays(30)));

return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withInitialCacheConfigurations(initialCacheConfigurations)
        .build();
```

### 缓存击穿

1. Spring Cache 自带的 `sync=true`

   // 核心修改：添加 sync = true 

   ```
   @Cacheable(value = CacheKeyConstants.Product.APP_DETAIL, key = "#p0", sync = true)
   ```

2. 分布式互斥锁

   ```xml
   <dependency>
       <groupId>org.redisson</groupId>
       <artifactId>redisson-spring-boot-starter</artifactId>
       <version>3.27.0</version> 
   </dependency>
   ```

   ```java
   @Autowired
   private RedissonClient redissonClient;
   @Autowired
   private RedisTemplate<String, Object> redisTemplate;
   
   public ProductDetailVO getMenuDetail(Long id) {
       String cacheKey = "product:detail:" + id;
       String lockKey = "lock:product:detail:" + id;
   
       // 1. 查缓存
       ProductDetailVO cachedData = (ProductDetailVO) redisTemplate.opsForValue().get(cacheKey);
       if (cachedData != null) {
           return cachedData;
       }
   
       // 2. 缓存未命中，准备重建。先获取互斥锁
       RLock lock = redissonClient.getLock(lockKey);
       boolean isLocked = false;
       try {
           // tryLock(等待时间, 锁自动释放时间, 单位)
           // waitTime=0 表示不等待，抢不到直接放弃（或者你可以设置等待几秒）
           // leaseTime=10s 防止死锁，10秒后自动释放
           isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
           
           if (isLocked) {
               // 3. 【双重检查】抢到锁后，再次查缓存（防止上一个线程已经重建好了）
               cachedData = (ProductDetailVO) redisTemplate.opsForValue().get(cacheKey);
               if (cachedData != null) {
                   return cachedData;
               }
   
               // 4. 查询数据库 (模拟耗时操作)
               ProductDetailVO dbData = getFromDB(id); // 你的查库逻辑
   
               // 5. 写入缓存 (设置正常的过期时间，如 30 分钟)
               redisTemplate.opsForValue().set(cacheKey, dbData, 30, TimeUnit.MINUTES);
               
               return dbData;
           } else {
               // 6. 没抢到锁，说明有人在重建。
               // 策略 A：休眠一会，递归重试 (推荐)
               Thread.sleep(50);
               return getMenuDetail(id);
               
               // 策略 B：直接返回失败或空
           }
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       } finally {
           // 7. 释放锁 (必须判断是否是当前线程持有的锁)
           if (isLocked && lock.isHeldByCurrentThread()) {
               lock.unlock();
           }
       }
   }
   ```

3. 逻辑过期 

```java
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RedisData {
    private LocalDateTime expireTime; // 逻辑过期时间
    private Object data;              // 真实数据
}
```

```java
// 这是一个预热方法，可以在单元测试或系统启动时调用
public void saveProductToRedis(Long id, Long expireSeconds) {
    // 1. 查数据库
    ProductDetailVO product = getFromDB(id);
    
    // 2. 封装逻辑过期
    RedisData redisData = new RedisData();
    redisData.setData(product);
    // 设置逻辑过期时间：当前时间 + 指定秒数
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
    
    // 3. 写入 Redis (注意：这里不设置 Redis 的物理 TTL，或者设置得非常长)
    redisTemplate.opsForValue().set("product:detail:" + id, redisData);
}
```

```java
// 线程池 (实际开发中建议用配置类创建单独的线程池)
private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

public ProductDetailVO getMenuDetailWithLogicalExpire(Long id) {
    String cacheKey = "product:detail:" + id;
    String lockKey = "lock:product:detail:" + id;

    // 1. 查缓存
    RedisData redisData = (RedisData) redisTemplate.opsForValue().get(cacheKey);
    
    // 2. 如果缓存完全不存在 (未预热)，直接降级查库或返回空
    if (redisData == null) {
        return getFromDB(id); // 或者返回 null
    }

    // 3. 判断是否逻辑过期
    ProductDetailVO vo = (ProductDetailVO) redisData.getData();
    LocalDateTime expireTime = redisData.getExpireTime();
    
    // 如果 未过期，直接返回
    if (expireTime.isAfter(LocalDateTime.now())) {
        return vo;
    }

    // 4. 已过期，需要缓存重建
    // 尝试获取互斥锁 (注意：这里只需要 tryLock，不需要等待)
    RLock lock = redissonClient.getLock(lockKey);
    boolean isLocked = lock.tryLock(); // 非阻塞获取

    if (isLocked) {
        // 5. 抢到锁了，开启独立线程去执行耗时的重建任务
        CACHE_REBUILD_EXECUTOR.submit(() -> {
            try {
                // 重建缓存逻辑
                this.saveProductToRedis(id, 1800L); // 重新设为 30 分钟后逻辑过期
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 释放锁
                lock.unlock();
            }
        });
    }

    // 6. 无论是否抢到锁，都直接返回“旧数据” (这就是性能高的原因)
    return vo;
}
```

### 订单超时未支付自动取消”功能

```java
package com.coffee.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Queue;

/**
 * RabbitMQ 延迟队列配置
 * 用于处理订单超时自动取消
 */
@Configuration
public class RabbitMqConfig {

    // 延迟交换机名称
    public static final String DELAY_EXCHANGE_NAME = "order.delay.exchange";
    // 订单超时队列名称
    public static final String ORDER_TIMEOUT_QUEUE_NAME = "order.timeout.queue";
    // 路由键
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    /**
     * 定义延迟交换机
     * 注意：需要安装 rabbitmq_delayed_message_exchange 插件
     * type: "x-delayed-message"
     */
    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct"); // 内部路由方式
        return new CustomExchange(DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    /**
     * 定义订单超时队列
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(ORDER_TIMEOUT_QUEUE_NAME, true);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding bindingOrderTimeout(Queue orderTimeoutQueue, CustomExchange orderDelayExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderDelayExchange)
                .with(ORDER_TIMEOUT_ROUTING_KEY)
                .noargs();
    }
}
```

#### 第一步：发送延迟消息（生产者）

**位置**：`OrderServiceImpl.java` -> `sendDelayMessage` 方法

当你调用 `createOrder` 创建订单成功后，代码执行了以下操作：

1. **发送时机**：订单入库后，事务提交前（或内）。
2. **发送内容**：只发送了 `orderId`（订单ID）。
3. **设置延迟**：
   - 代码逻辑：`message.getMessageProperties().setDelay(time)`。
   - 逻辑意图：告诉 RabbitMQ，“这条消息你先帮我拿着，**30分钟**（或者你配置的时间）后再投递出去”。

#### 第二步：等待与投递（RabbitMQ 服务端）

- 消息到达 `order.delay.exchange` 后，**不会**立即进入队列。
- 它会保存在 Mnesia 数据库（插件内部机制）中等待。
- 倒计时结束（比如 30分钟后），交换机才会把消息路由到 `order.timeout.queue`。

#### 第三步：处理超时逻辑（消费者）

**位置**：`OrderTimeOutListener.java`

消费者监听 `order.timeout.queue`，收到消息后执行以下逻辑：

1. **查单**：根据 `orderId` 查询数据库中的订单状态。
2. **判断**：
   - **如果订单已支付**：说明用户在 30 分钟内付钱了，消息直接丢弃（Ack），什么都不做。
   - **如果订单还是“待支付”**：说明用户超时了。
3. **执行取消**：
   - 修改订单状态为 `CANCELLED` (已取消)。
   - 写取消原因：“订单超时未支付，自动取消”。
   - **回滚库存**：调用 `SkuStockService.releaseStock` 把占用的库存还回去。
   - **退回优惠券**：调用 `SmsCouponService.releaseCoupon` 把用户用的券还回去。
4. **确认消息**：最后手动调用 `channel.basicAck` 告诉 MQ 任务完成。





### RabbitMQ 取餐通知消息序列化格式不匹配导致消费失败

管理端点击“制作完成”将订单状态更新为“待取餐”时，取餐通知消息未进入 PICKUP_NOTIFICATION_QUEUE，消息被重新发布到错误交换器，NotificationListener 未收到消息，用户未收到取餐通知。

#### 为什么会这样？

1. 缺少 JSON 消息转换器配置

- 未配置 Jackson2JsonMessageConverter

- RabbitMQ 使用默认 Java 序列化，消息体为二进制

- 接收端期望 JSON 字符串，导致解析失败

#### 怎么解决

添加 JSON 消息转换器（核心修复）在 RabbitMqConfig.java 中添加：

```java
@Bean
public MessageConverter jsonMessageConverter() {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    converter.setCreateMessageIds(true);
    return converter;
}
```

