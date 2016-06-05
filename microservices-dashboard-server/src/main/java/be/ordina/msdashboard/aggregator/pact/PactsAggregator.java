package be.ordina.msdashboard.aggregator.pact;

import be.ordina.msdashboard.aggregator.PactBrokerBasedAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.jayway.jsonpath.JsonPath;
import io.reactivex.netty.RxNetty;
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
				pactNode.withLinkedToNode(value);
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

	//@Cacheable(value = Constants.PACTS_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Node fetchPactNodesWithObservable() {
		NodeBuilder pactNode = new NodeBuilder();
		fetchPactNodesAsObservable().subscribe(node -> pactNode.withLinkedToNode(node));
		return pactNode.build();
	}

	//@Cacheable(value = Constants.PACTS_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Observable<Node> fetchPactNodesAsObservable() {
		Observable<String> urls = getPactUrlsFromBroker();
		return urls.map(url -> getNodesFromPacts(url))
				.flatMap(el -> el)
				.doOnNext(el -> LOG.info("Merged pact node! " + el.getId()))
				/*.doOnNext(el -> {
					LOG.info("Pact node discovered!");
					try {
						LOG.info("Sleeping now for 10 seconds");
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				})*/;
	}

	private Observable<String> getPactUrlsFromBroker() {
		LOG.info("Discovering pact urls");
		return RxNetty.createHttpGet(pactBrokerUrl + latestPactsUrl)
				.filter(r -> {
					if (r.getStatus().code() < 400) {
						return true;
					} else {
						LOG.warn("Exception {} for call {} with headers {}", r.getStatus(), pactBrokerUrl + latestPactsUrl, r.getHeaders().entries());
						return false;
					}
				})
				.flatMap(response -> response.getContent())
				.map(data -> data.toString(Charset.defaultCharset()))
				.onErrorReturn(Throwable::toString)
				.map(response -> (List<String>) JsonPath.read(response, selfHrefJsonPath))
				.map(jsonList -> Observable.from(jsonList))
				.flatMap(el -> el.map(obj -> (String) obj))
				.doOnNext(url -> LOG.info("Pact url discovered: " + url));
	}

	private Observable<Node> getNodesFromPacts(String url) {
		return RxNetty.createHttpGet(url)
				.filter(r -> {
					if (r.getStatus().code() < 400) {
						return true;
					} else {
						LOG.warn("Exception {} for call {} with headers {}", r.getStatus(), url, r.getHeaders().entries());
						return false;
					}
				})
				.flatMap(response -> response.getContent())
				.map(data -> data.toString(Charset.defaultCharset()))
				.onErrorReturn(Throwable::toString)
				.map(response -> {
					PactToNodeConverter pactToNodeConverter = new PactToNodeConverter();
					return pactToNodeConverter.convert(response, url);
				})
				.doOnNext(node -> LOG.info("Pact node discovered in url: " + url));
	}

	@Test
	public void printWiki() {
		pactBrokerUrl = "http://echo.jsontest.com/url1/echo.jsontest.com/url2/echo.jsontest.com";
		latestPactsUrl = "";
		selfHrefJsonPath = "$..*";
		getPactUrlsFromBroker().toBlocking().forEach(element -> System.out.println("Element: " + element));
	}
}
