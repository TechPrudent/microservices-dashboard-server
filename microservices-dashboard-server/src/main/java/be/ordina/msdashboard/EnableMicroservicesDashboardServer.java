package be.ordina.msdashboard;

import be.ordina.msdashboard.config.MicroservicesDashboardServerImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Andreas Evers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MicroservicesDashboardServerImportSelector.class)
public @interface EnableMicroservicesDashboardServer {

}