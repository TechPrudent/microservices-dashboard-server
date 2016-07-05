package be.ordina.simple;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMicroservicesDashboardServer
public class MicroservicesDashboardSimpleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicesDashboardSimpleApplication.class, args);
		print();
	}

	private static void print() {
		String input = "      {\n" +
				"        \"name\": \"SERVICE1\",\n" +
				"        \"instance\": [\n" +
				"          {\n" +
				"            \"instanceId\": \"localhost:service1:8089\",\n" +
				"            \"hostName\": \"localhost\",\n" +
				"            \"app\": \"SERVICE1\",\n" +
				"            \"ipAddr\": \"127.0.0.1\",\n" +
				"            \"status\": \"UP\",\n" +
				"            \"overriddenstatus\": \"UNKNOWN\",\n" +
				"            \"port\": {\n" +
				"              \"$\": 8089,\n" +
				"              \"@enabled\": \"true\"\n" +
				"            },\n" +
				"            \"securePort\": {\n" +
				"              \"$\": 443,\n" +
				"              \"@enabled\": \"false\"\n" +
				"            },\n" +
				"            \"countryId\": 1,\n" +
				"            \"dataCenterInfo\": {\n" +
				"              \"@class\": \"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\",\n" +
				"              \"name\": \"MyOwn\"\n" +
				"            },\n" +
				"            \"leaseInfo\": {\n" +
				"              \"renewalIntervalInSecs\": 30,\n" +
				"              \"durationInSecs\": 90,\n" +
				"              \"registrationTimestamp\": 1466173255014,\n" +
				"              \"lastRenewalTimestamp\": 1466414104546,\n" +
				"              \"evictionTimestamp\": 0,\n" +
				"              \"serviceUpTimestamp\": 1466173255014\n" +
				"            },\n" +
				"            \"metadata\": {\n" +
				"              \"@class\": \"java.util.Collections$EmptyMap\"\n" +
				"            },\n" +
				"            \"appGroupName\": \"SERVICE1\",\n" +
				"            \"homePageUrl\": \"http://localhost:8089/service1\",\n" +
				"            \"statusPageUrl\": \"http://localhost:8089/service1/info\",\n" +
				"            \"healthCheckUrl\": \"http://localhost:8089/service1/health\",\n" +
				"            \"vipAddress\": \"service1\",\n" +
				"            \"isCoordinatingDiscoveryServer\": \"false\",\n" +
				"            \"lastUpdatedTimestamp\": \"1466173255014\",\n" +
				"            \"lastDirtyTimestamp\": \"1466173254946\",\n" +
				"            \"actionType\": \"ADDED\"\n" +
				"          }\n" +
				"        ]\n" +
				"      },";
		String[] names = new String[]{"fat-jar-test",
				"customer-group",
				"customer-base",
				"admin",
				"user-preferences",
				"customer-addresses",
				"loyalty-program",
				"pizza-stock",
				"zuul",
				"billing-payments",
				"encryption",
				"order-processor",
				"customer-management",
				"user",
				"user-avatar",
				"agenda",
				"tariff-charges",
				"pizza-transactions",
				"customer-administrators",
				"user-billing-structure",
				"billing-accounts",
				"shop-management",
				"customer-transformer",
				"turbine",
				"hystrix",
				"awards",
				"billing-replicator",
				"web-feedback",
				"pizza-feedback",
				"customer-access",
				"billing-complaints",
				"historical-usage-prepaid",
				"offers",
				"customer-access-numbers",
				"transport",
				"usage",
				"ordering-loyalty",
				"ordering-pets",
				"marketing",
				"company-cars",
				"messenger"};
		for (String name : names) {
			String output = input.replace("service1", name);
			String output2 = output.replace("SERVICE1", name.toUpperCase());
			System.out.println(output2);
		}
	}
}
