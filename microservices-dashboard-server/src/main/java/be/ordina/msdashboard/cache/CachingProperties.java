package be.ordina.msdashboard.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Andreas Evers
 */
@Configuration
@ConfigurationProperties("spring.cache")
public class CachingProperties {

	private String redisCachePrefix = "DEFAULT_PREFIX_";
	private long defaultExpiration = 30;
	private boolean evict = true;

	public boolean isEvict() {
		return evict;
	}

	public void setEvict(boolean evict) {
		this.evict = evict;
	}

	public long getDefaultExpiration() {
		return defaultExpiration;
	}

	public void setDefaultExpiration(long defaultExpiration) {
		this.defaultExpiration = defaultExpiration;
	}

	public String getRedisCachePrefix() {
		return redisCachePrefix;
	}

	public void setRedisCachePrefix(String redisCachePrefix) {
		this.redisCachePrefix = redisCachePrefix;
	}
}
