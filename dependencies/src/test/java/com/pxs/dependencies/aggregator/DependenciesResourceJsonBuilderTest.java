package com.pxs.dependencies.aggregator;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class DependenciesResourceJsonBuilderTest {

	@InjectMocks
	private DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder;

	@Spy
	private ObjectMapper primaryObjectMapper;

	@Mock
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	@Test
	public void testBuild() throws Exception {
		doReturn(new HashMap<String, Object>()).when(healthIndicatorsAggregator).fetchCombinedDependencies();

		dependenciesResourceJsonBuilder.build();

		verify(healthIndicatorsAggregator, times(1)).fetchCombinedDependencies();
	}
}
