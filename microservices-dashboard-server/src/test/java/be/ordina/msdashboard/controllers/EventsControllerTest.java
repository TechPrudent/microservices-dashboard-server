package be.ordina.msdashboard.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import be.ordina.msdashboard.events.EventListener;
import be.ordina.msdashboard.events.SystemEvent;

@RunWith(MockitoJUnitRunner.class)
public class EventsControllerTest {

    @InjectMocks
    private EventsController eventsController;
    
    @Mock
    private EventListener eventListener;
    
    @Test
    public void getAllNodes() {
    	ConcurrentLinkedDeque<SystemEvent> events = new ConcurrentLinkedDeque<SystemEvent>();
    	events.add(new SystemEvent());
        when(eventListener.getEvents()).thenReturn(events);
        
        Collection<SystemEvent> nodes = eventsController.getAllNodes();
        assertThat(nodes).isNotNull();
        assertThat(nodes.size()).isEqualTo(1);
    }

    @Test
    public void deleteAllNodes() {
    	eventsController.deleteAllNodes();
    	verify(eventListener).deleteEvents();
    }
}
