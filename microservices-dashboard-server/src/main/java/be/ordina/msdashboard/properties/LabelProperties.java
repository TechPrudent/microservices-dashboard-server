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
package be.ordina.msdashboard.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Andreas Evers
 * @author Tim Ysewyn
 */
@ConfigurationProperties("msdashboard.label")
public class LabelProperties {

	private Lane lane = new Lane();
	private Type type = new Type();

	public Lane getLane() {
		return lane;
	}

	public Type getType() {
		return type;
	}

	public static class Lane {
		/**
		 * Label for UI components lane.
		 */
		private String ui = "UI Components";

		/**
		 * Label for resources lane.
		 */
		private String resources = "Resources";

		/**
		 * Label for microservices lane.
		 */
		private String microservices = "Microservices";

		/**
		 * Label for backends lane.
		 */
		private String backends = "Backends";

		public String getUi() {
			return ui;
		}

		public void setUi(String ui) {
			this.ui = ui;
		}

		public String getResources() {
			return resources;
		}

		public void setResources(String resources) {
			this.resources = resources;
		}

		public String getMicroservices() {
			return microservices;
		}

		public void setMicroservices(String microservices) {
			this.microservices = microservices;
		}

		public String getBackends() {
			return backends;
		}

		public void setBackends(String backends) {
			this.backends = backends;
		}
	}

	public static class Type {

		/**
		 * Label for DB type.
		 */
		private String db = "DB";

		/**
		 * Label for microservice type.
		 */
		private String microservice = "MICROSERVICE";

		/**
		 * Label for resource type.
		 */
		private String resource = "RESOURCE";

		/**
		 * Label for REST type.
		 */
		private String rest = "REST";

		/**
		 * Label for SOAP type.
		 */
		private String soap = "SOAP";

		/**
		 * Label for JMS type.
		 */
		private String jms = "JMS";

		public String getDb() {
			return db;
		}

		public void setDb(String db) {
			this.db = db;
		}

		public String getMicroservice() {
			return microservice;
		}

		public void setMicroservice(String microservice) {
			this.microservice = microservice;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getRest() {
			return rest;
		}

		public void setRest(String rest) {
			this.rest = rest;
		}

		public String getSoap() {
			return soap;
		}

		public void setSoap(String soap) {
			this.soap = soap;
		}

		public String getJms() {
			return jms;
		}

		public void setJms(String jms) {
			this.jms = jms;
		}
	}

}
