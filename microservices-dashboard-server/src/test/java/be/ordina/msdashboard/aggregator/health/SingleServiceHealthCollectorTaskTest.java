package be.ordina.msdashboard.aggregator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static be.ordina.msdashboard.constants.Constants.GROUP;
import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.STATUS;
import static be.ordina.msdashboard.constants.Constants.TYPE;
import static be.ordina.msdashboard.constants.Constants.VERSION;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import be.ordina.msdashboard.model.Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import be.ordina.msdashboard.model.Node;

@RunWith(MockitoJUnitRunner.class)
public class SingleServiceHealthCollectorTaskTest {

	@InjectMocks
	@Spy
	private SingleServiceHealthCollectorTask task = new SingleServiceHealthCollectorTask(new Service("", "", 10),  null, null);

	@Mock
	private ResponseEntity<Map> response;

	@Mock
	private HealthToNodeConverter healthToNodeConverter;

	private RestTemplate restTemplate;

	@Before
	public void onSetUp() throws URISyntaxException {
		restTemplate = mock(RestTemplate.class, RETURNS_DEEP_STUBS);
		when(task.getRestTemplate()).thenReturn(restTemplate);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class))).thenReturn(response);
		when(response.getBody()).thenReturn(new HashMap());
		when(healthToNodeConverter.convert(any(Map.class))).thenReturn(mockResponseEntity());
	}

	@Test
	public void unmarshallsHealths() throws Exception {
		Node node = task.call();
		assertThat(node.getLinkedToNodes().size()).isEqualTo(2);
	}

	private Node mockResponseEntity() {
		Node node = new Node();
		node.getDetails().put(STATUS, "UP");
		node.getDetails().put(TYPE, MICROSERVICE);

		Node bciNode = new Node();
		bciNode.setId("bciManageCustomerContact");
		bciNode.getDetails().put(STATUS, "UP");
		bciNode.getDetails().put(VERSION, "4.0");
		bciNode.getDetails().put(TYPE, "SOAP");
		bciNode.getDetails().put(GROUP, "BCI");

		Node cslNode = new Node();
		cslNode.setId("cslCustomer");
		cslNode.getDetails().put(STATUS, "UP");
		cslNode.getDetails().put(VERSION, "Customer 4.0 (since 2012 CRC03)");
		cslNode.getDetails().put(TYPE, "SOAP");
		cslNode.getDetails().put(GROUP, "CSL");

		Node diskSpaceNode = new Node();
		diskSpaceNode.setId("diskSpace");
		diskSpaceNode.getDetails().put(STATUS, "UP");
		diskSpaceNode.getDetails().put("free", 165026881536L);
		diskSpaceNode.getDetails().put("threshold", 10485760);
		diskSpaceNode.getDetails().put(GROUP, "CSL");

		node.getLinkedToNodes().add(bciNode);
		node.getLinkedToNodes().add(cslNode);
		node.getLinkedToNodes().add(diskSpaceNode);

		return node;
	}
}
