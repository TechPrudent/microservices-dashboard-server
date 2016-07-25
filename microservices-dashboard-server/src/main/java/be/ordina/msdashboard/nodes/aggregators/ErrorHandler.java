/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ordina.msdashboard.nodes.aggregators;

import be.ordina.msdashboard.nodes.model.NodeEvent;
import be.ordina.msdashboard.nodes.model.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Andreas Evers
 */
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    private ApplicationEventPublisher publisher;

    public ErrorHandler(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void handleNodeWarning(String serviceId, String message) {
        logger.warn(message);
        publisher.publishEvent(new NodeEvent(serviceId, message));
    }

    public void handleNodeError(String serviceId, String message, Throwable el) {
        logger.error(message, el);
        publisher.publishEvent(new NodeEvent(serviceId, message, el));
    }

    public void handleSystemError(String message, Throwable el) {
        logger.error(message, el);
        publisher.publishEvent(new SystemEvent(message, el));
    }
}
