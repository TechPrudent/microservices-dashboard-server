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
package be.ordina.msdashboard.events;

import java.io.Serializable;

/**
 * Simple system event when something goes wrong
 *
 * @author Andreas Evers
 */
public class SystemEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private Throwable throwable;

    public SystemEvent() {
    }

    public SystemEvent(String message) {
        this.message = message;
    }

    public SystemEvent(String message, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SystemEvent that = (SystemEvent) o;

        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return throwable != null ? throwable.equals(that.throwable) : that.throwable == null;

    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
        return result;
    }
}
