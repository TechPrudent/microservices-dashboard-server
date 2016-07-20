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
package be.ordina.msdashboard.aggregators;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import rx.Observable;

import java.nio.charset.Charset;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * @author Andreas Evers
 */
public class NettyServiceCaller {

    private static final Logger logger = LoggerFactory.getLogger(NettyServiceCaller.class);

    private ErrorHandler errorHandler;

    public NettyServiceCaller(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Observable<Map<String, Object>> retrieveJsonFromRequest(String serviceId, HttpClientRequest<ByteBuf> request) {
        return RxNetty.createHttpRequest(request)
                .publish().autoConnect()
                .doOnError(el -> errorHandler.handleNodeError(serviceId, format("Error retrieving node(s) for url {} with headers {}: {}",
                        request.getUri(), request.getHeaders().entries(), el), el))
                .filter(r -> {
                    if (r.getStatus().code() < 400) {
                        return true;
                    } else {
                        errorHandler.handleNodeWarning(serviceId, "Exception " + r.getStatus() + " for url " + request.getUri() + " with headers " + r.getHeaders().entries());
                        return false;
                    }
                })
                .flatMap(AbstractHttpContentHolder::getContent)
                .map(data -> data.toString(Charset.defaultCharset()))
                .map(response -> {
                    JacksonJsonParser jsonParser = new JacksonJsonParser();
                    return jsonParser.parseMap(response);
                })
                .doOnNext(r -> logger.info("Json retrieved from call: {}", r))
                .onErrorResumeNext(Observable.empty());
    }
}
