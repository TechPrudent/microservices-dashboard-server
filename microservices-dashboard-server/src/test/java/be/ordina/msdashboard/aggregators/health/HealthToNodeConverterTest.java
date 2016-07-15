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
package be.ordina.msdashboard.aggregators.health;

import be.ordina.msdashboard.model.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static be.ordina.msdashboard.constants.Constants.CONFIGSERVER;
import static be.ordina.msdashboard.constants.Constants.DISCOVERY;
import static be.ordina.msdashboard.constants.Constants.DISK_SPACE;
import static be.ordina.msdashboard.constants.Constants.HYSTRIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andreas Evers
 */
public class HealthToNodeConverterTest {

    private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

    @Test
    public void shouldConvertMostBasicNode() throws IOException {
        Map<String, Object> source = OBJECT_READER.forType(Map.class).readValue("{\"status\":\"UP\"}");

        Observable<Node> observable = HealthToNodeConverter.convertToNodes("svc1", source);

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        observable.toBlocking().subscribe(testSubscriber);
        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).extracting("id").containsExactly("svc1");
        assertThat(nodes).extracting("details").extracting("status").containsExactly("UP");
        assertThat(nodes).extracting("details").extracting("type").containsExactly("MICROSERVICE");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenStatusMissing() throws IOException {
        Map<String, Object> source = OBJECT_READER.forType(Map.class).readValue("{\"test\":\"test\"}");

        Observable<Node> observable = HealthToNodeConverter.convertToNodes("svc1", source);

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        observable.toBlocking().subscribe(testSubscriber);
    }

    @Test
    public void shouldEmitNestedNodes() throws IOException {
        Map<String, Object> source = OBJECT_READER.forType(Map.class)
                .readValue("{\"status\":\"UP\", " +
                        "\"svc2\": {\"status\": \"DOWN\", \"version\": \"1.0.0\", " +
                        "\"type\": \"SOAP\", \"group\": \"test\"}}");

        Observable<Node> observable = HealthToNodeConverter.convertToNodes("svc1", source);

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        observable.toBlocking().subscribe(testSubscriber);
        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).extracting("id").containsExactly("svc2", "svc1");
        assertThat(nodes).extracting("details").extracting("status").containsExactly("DOWN", "UP");
        assertThat(nodes).extracting("details").extracting("type").containsExactly("SOAP", "MICROSERVICE");
        assertThat(nodes).extracting("details").extracting("version").containsExactly("1.0.0", null);
        assertThat(nodes).extracting("details").extracting("group").containsExactly("test", null);
        assertThat(nodes).filteredOn("id", "svc1").flatExtracting("linkedToNodeIds").containsExactly("svc2");
        assertThat(nodes).filteredOn("id", "svc2").flatExtracting("linkedFromNodeIds").containsExactly("svc1");
    }

    @Test
    public void shouldIgnoreCommonNodes() throws IOException {
        Map<String, Object> source = OBJECT_READER.forType(Map.class)
                .readValue("{\"status\":\"UP\", " +
                        "\"" + HYSTRIX + "\": {\"status\": \"DOWN\", \"version\": \"1.0.0\", " +
                        "\"type\": \"SOAP\", \"group\": \"test\"}, " +
                        "\"" + DISK_SPACE + "\": {\"status\": \"DOWN\", \"version\": \"1.0.0\", " +
                        "\"type\": \"SOAP\", \"group\": \"test\"}, " +
                        "\"" + DISCOVERY + "\": {\"status\": \"DOWN\", \"version\": \"1.0.0\", " +
                        "\"type\": \"SOAP\", \"group\": \"test\"}, " +
                        "\"" + CONFIGSERVER + "\": {\"status\": \"DOWN\", \"version\": \"1.0.0\", " +
                        "\"type\": \"SOAP\", \"group\": \"test\"}}");

        Observable<Node> observable = HealthToNodeConverter.convertToNodes("svc1", source);

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        observable.toBlocking().subscribe(testSubscriber);
        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).extracting("id").containsExactly("svc1");
        assertThat(nodes).extracting("details").extracting("status").containsExactly("UP");
        assertThat(nodes).extracting("details").extracting("type").containsExactly("MICROSERVICE");
    }

    @Test
    public void shouldConvertOwnDetails() throws IOException {
        Map<String, Object> source = OBJECT_READER.forType(Map.class)
                .readValue("{\"status\":\"UP\", \"foo\":\"bar\"}");

        Observable<Node> observable = HealthToNodeConverter.convertToNodes("svc1", source);

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        observable.toBlocking().subscribe(testSubscriber);
        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).extracting("id").containsExactly("svc1");
        assertThat(nodes).extracting("details").extracting("status").containsExactly("UP");
        assertThat(nodes).extracting("details").extracting("type").containsExactly("MICROSERVICE");
        assertThat(nodes).extracting("details").extracting("foo").containsExactly("bar");
    }
}
