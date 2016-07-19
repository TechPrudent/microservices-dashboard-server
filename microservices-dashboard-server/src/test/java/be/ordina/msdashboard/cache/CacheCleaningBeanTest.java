package be.ordina.msdashboard.cache;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.mockito.Mockito;

public class CacheCleaningBeanTest {

	@Test
	public void cleanEvictTrue(){
		NodeCache nodeCache = Mockito.mock(NodeCache.class);
		CacheCleaningBean cacheCleaningBean= new CacheCleaningBean(nodeCache, true);
		cacheCleaningBean.clean();
		verify(nodeCache).evictGraphCache();
		verify(nodeCache).evictHealthsCache();
		verify(nodeCache).evictIndexesCache();
		verify(nodeCache).evictPactsCache();
	}
	
	@Test
	public void cleanEvictFalse(){
		NodeCache nodeCache = Mockito.mock(NodeCache.class);
		CacheCleaningBean cacheCleaningBean= new CacheCleaningBean(nodeCache, false);
		cacheCleaningBean.clean();
		verifyZeroInteractions(nodeCache);
	}
}
