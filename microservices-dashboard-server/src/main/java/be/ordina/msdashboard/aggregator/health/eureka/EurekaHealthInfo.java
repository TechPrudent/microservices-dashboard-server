package be.ordina.msdashboard.aggregator.health.eureka;

/**
 * Holds the health information for a service instance
 */
public class EurekaHealthInfo {

    private String description;
    private String status;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "EurekaHealthInfo{" +
                "description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
