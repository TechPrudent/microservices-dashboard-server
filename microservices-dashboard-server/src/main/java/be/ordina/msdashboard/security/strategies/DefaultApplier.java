/*
 * Copyright 2012-2017 the original author or authors.
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

package be.ordina.msdashboard.security.strategies;

import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import be.ordina.msdashboard.security.strategy.SecurityStrategy;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;

/**
 * When there is no security configured, a default applier will execute
 *
 * @author Kevin van Houtte
 */
@SecurityStrategy(type = SecurityProtocolApplier.class, protocol = SecurityProtocol.NONE)
public class DefaultApplier implements SecurityProtocolApplier {

    /**
     * Does not apply any security headers to the request
     * @param request
     */
    @Override
    public void apply(HttpClientRequest<ByteBuf> request) {
    }
}
