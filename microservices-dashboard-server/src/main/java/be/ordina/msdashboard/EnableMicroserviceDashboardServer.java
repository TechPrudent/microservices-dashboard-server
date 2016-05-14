package be.ordina.msdashboard;

import be.ordina.msdashboard.config.MicroserviceDashboardServerImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Andreas Evers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MicroserviceDashboardServerImportSelector.class)
public @interface EnableMicroserviceDashboardServer {

}