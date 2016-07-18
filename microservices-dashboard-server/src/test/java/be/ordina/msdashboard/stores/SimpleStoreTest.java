package be.ordina.msdashboard.stores;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import be.ordina.msdashboard.converters.ObjectToJsonConverter;
import be.ordina.msdashboard.model.Node;

@RunWith(MockitoJUnitRunner.class)
public class SimpleStoreTest {

    @InjectMocks
    private SimpleStore simpleStore;
    
    @Test
	public void savingAndDeletingNodes(){
		 ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<Node>();
		 
		 Node node1 = new Node("some id");
		 simpleStore.saveNode(converter.convert(node1));
		 assertThat(simpleStore.getAllNodes().size()).isEqualTo(1);
		 
		 Node node2 = new Node("another id");
		 simpleStore.saveNode(converter.convert(node2));
		 simpleStore.deleteNode("some id");
		 assertThat(simpleStore.getAllNodes().iterator().next().getId()).isEqualTo("another id");
		 
		 simpleStore.deleteAllNodes();
		 assertThat(simpleStore.getAllNodes().size()).isEqualTo(0);
		 
		 Node node3 = new Node("again another id");
		 simpleStore.saveNode(converter.convert(node3));
		 assertThat(simpleStore.getAllNodesAsObservable()).isNotNull();
		 
		 simpleStore.flushDB();
		 assertThat(simpleStore.getAllNodes().size()).isEqualTo(0);
	}
}
