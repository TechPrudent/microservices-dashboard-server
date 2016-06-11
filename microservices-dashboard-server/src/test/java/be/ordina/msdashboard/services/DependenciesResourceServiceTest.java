package be.ordina.msdashboard.services;

import be.ordina.msdashboard.aggregator.DependenciesGraphResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.ObservablesToGraphConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DependenciesResourceServiceTest {

	@InjectMocks
	private DependenciesResourceService dependenciesResourceService;

	@Mock
	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;

	@Mock
	private ObservablesToGraphConverter observablesToGraphConverter;

	@Test
	public void getDependenciesGraphResourceJson() {
		Map<String, Object> graph = Collections.emptyMap();

		doReturn(graph).when(dependenciesGraphResourceJsonBuilder).build();
		doReturn(graph).when(observablesToGraphConverter).build();

		Map<String, Object> returnedGraph = dependenciesResourceService.getDependenciesGraphResourceJson();

		verify(observablesToGraphConverter).build();

		assertThat(returnedGraph).isEqualTo(graph);
	}
}