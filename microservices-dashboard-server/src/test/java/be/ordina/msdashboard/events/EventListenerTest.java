package be.ordina.msdashboard.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EventListenerTest {

	private EventListener eventListener = new EventListener();
	
	@SuppressWarnings("unchecked")
	@Test
	public void operationsOnEvents(){
		eventListener.handleContextRefresh(new SystemEvent());
		assertThat(eventListener.getEvents().size()).isEqualTo(1);
		eventListener.deleteEvents();
		assertThat(eventListener.getEvents()).isEmpty();
	}
}
