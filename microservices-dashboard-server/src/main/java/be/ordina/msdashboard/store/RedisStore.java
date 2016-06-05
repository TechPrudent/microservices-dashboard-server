package be.ordina.msdashboard.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import be.ordina.msdashboard.cache.NodeCache;
import be.ordina.msdashboard.converters.JsonToObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import rx.Observable;

public class RedisStore implements NodeCache, NodeStore {

	private RedisTemplate<String, Node> redisTemplate;

	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public RedisStore(final RedisTemplate<String, Node> redisTemplate,
					  final RedisConnectionFactory redisConnectionFactory) {
		this.redisTemplate = redisTemplate;
		((JedisConnectionFactory) redisConnectionFactory).setTimeout(10000);
		this.redisConnectionFactory = redisConnectionFactory;
	}

	@Override
	public Collection<Node> getAllNodes() {
		List<Node> results = new ArrayList<>();
		Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
		for (String key : keys) {
			Node node = redisTemplate.opsForValue().get(key);
			results.add(node);
		}
		return results;
	}

	@Override
	public Observable<Node> getAllNodesAsObservable() {
		return Observable.from(getAllNodes());
	}

	@Override
	public void saveNode(final String nodeData) {
		Node node = getNode(nodeData);
		String nodeId = node.getId();
		node.getDetails().put(VIRTUAL_FLAG, true);
		redisTemplate.opsForValue().set(KEY_PREFIX + nodeId, node);
	}

	@Override
	public void deleteNode(final String nodeId) {
		redisTemplate.delete(nodeId);
	}

	@Override
	public void deleteAllNodes() {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	private Node getNode(String nodeData) {
		JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
		return converter.convert(nodeData);
	}

	@Override
	public void flushDB() {
		redisConnectionFactory.getConnection().flushDb();
	}

	@Override
	@CacheEvict(value = Constants.HEALTH_CACHE_NAME, allEntries = true)
	public void evictHealthsCache() {
	}

	@Override
	@CacheEvict(value = Constants.INDEX_CACHE_NAME, allEntries = true)
	public void evictIndexesCache() {
	}

	@Override
	@CacheEvict(value = Constants.PACTS_CACHE_NAME, allEntries = true)
	public void evictPactsCache() {
	}
}