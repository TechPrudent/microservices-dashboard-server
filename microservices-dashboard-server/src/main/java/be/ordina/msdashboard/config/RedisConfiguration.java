package be.ordina.msdashboard.config;

import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.cache.CachingProperties;
import be.ordina.msdashboard.cache.NodeCache;
import be.ordina.msdashboard.converters.NodeSerializer;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.store.NodeStore;
import be.ordina.msdashboard.store.RedisStore;
import be.ordina.msdashboard.config.RedisConfiguration.RedisOrMockCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Andreas Evers
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CachingProperties.class)
@Conditional(RedisOrMockCondition.class)
@AutoConfigureBefore(WebConfiguration.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
public class RedisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean
    public NodeStore nodeStore(final RedisConnectionFactory factory) {
        return new RedisStore(redisTemplate(factory), factory);
    }

    @Bean
    public CacheCleaningBean cacheCleaningBean(@Qualifier("nodeStore") NodeCache nodeCache){
        return new CacheCleaningBean(nodeCache, cachingProperties.isEvict());
    }

    @Bean
    public RedisTemplate<String,Node> redisTemplate(final RedisConnectionFactory factory){
        RedisTemplate<String, Node> virtualNodeTemplate = new RedisTemplate<>();
        virtualNodeTemplate.setConnectionFactory(factory);
        virtualNodeTemplate.setKeySerializer(new StringRedisSerializer());
        virtualNodeTemplate.setValueSerializer(new NodeSerializer());
        return virtualNodeTemplate;
    }

    @Bean
    @ConditionalOnProperty("redis.mock")
    public InMemoryRedis inMemoryRedis() {
        return new InMemoryRedis();
    }

    private class InMemoryRedis {

        @Value("${spring.redis.port:6379}")
        private int redisPort;

        private RedisServer redisServer;

        @PostConstruct
        public void startRedis() throws IOException {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
        }

        @PreDestroy
        public void stopRedis() {
            redisServer.stop();
        }
    }

    @Autowired
    private CachingProperties cachingProperties;

    @Bean
    @ConditionalOnMissingBean
    public CachingProperties cachingProperties() {
        return new CachingProperties();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisTemplate<String, Node> redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setDefaultExpiration(cachingProperties.getDefaultExpiration());
        RedisCachePrefix redisCachePrefix = new DefaultRedisCachePrefix();
        redisCachePrefix.prefix(cachingProperties.getRedisCachePrefix());
        cacheManager.setCachePrefix(redisCachePrefix);
        return cacheManager;
    }

    @Bean
    public KeyGenerator simpleKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    static class RedisOrMockCondition extends AnyNestedCondition {

        RedisOrMockCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty("spring.redis.host")
        static class SpringRedisCondition {}

        @ConditionalOnProperty("redis.mock")
        static class MockRedisCondition {}

    }
}
