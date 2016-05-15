package be.ordina.msdashboard.aggregator.pact;

import be.ordina.msdashboard.aggregator.PactBrokerBasedAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.jayway.jsonpath.JsonPath;
import io.reactivex.netty.RxNetty;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import rx.Observable;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.*;

public class PactsAggregator extends PactBrokerBasedAggregator<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(PactsAggregator.class);

	private static final long TIMEOUT = 17000L;

	@Cacheable(value = Constants.PACTS_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Node fetchPactNodes() {
		NodeBuilder pactNode = new NodeBuilder();
		for (FutureTask<Node> task : getFutureTasks()) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Node value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("Task {} is done: {}", key, task.isDone());
				pactNode.withLinkedNode(value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.warn("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching pacts");
		return pactNode.build();
	}

	@Override
	protected Callable<Node> instantiateAggregatorTask(String pactUrl) {
		return new SinglePactCollectorTask(pactUrl);
	}

	// REACTIVE WAY

	@Cacheable(value = Constants.PACTS_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Node fetchPactNodesWithObservable() {
		NodeBuilder pactNode = new NodeBuilder();
		fetchPactNodesAsObservable().subscribe(node -> pactNode.withLinkedNode(node));
		return pactNode.build();
	}

	public Observable<Node> fetchPactNodesAsObservable() {
		Observable<String> urls = getPactUrlsFromBroker();
		return urls.map(url -> getNodesFromPacts(url))
				.flatMap(el -> el);
	}

	@Test
	public void printWiki() {
		pactBrokerUrl = "http://echo.jsontest.com/url1/echo.jsontest.com/url2/echo.jsontest.com";
		latestPactsUrl = "";
		selfHrefJsonPath = "$..*";
		getPactUrlsFromBroker().toBlocking().forEach(element -> System.out.println("Element: " + element));
	}

	private Observable<String> getPactUrlsFromBroker() {
		return RxNetty.createHttpGet(pactBrokerUrl + latestPactsUrl)
				.flatMap(response -> response.getContent())
				.map(data -> data.toString(Charset.defaultCharset()))
				.map(response -> (List<String>) JsonPath.read(response, selfHrefJsonPath))
				.map(jsonList -> Observable.from(jsonList))
				.flatMap(el -> el.map(obj -> (String) obj))
				.doOnNext(System.out::println);
	}

	private Observable<Node> getNodesFromPacts(String url) {
		return RxNetty.createHttpGet(url)
				.flatMap(response -> response.getContent())
				.map(data -> data.toString(Charset.defaultCharset()))
				.map(response -> {
					PactToNodeConverter pactToNodeConverter = new PactToNodeConverter();
					return pactToNodeConverter.convert(response, url);
				})
				.doOnNext(System.out::println);
	}
}
