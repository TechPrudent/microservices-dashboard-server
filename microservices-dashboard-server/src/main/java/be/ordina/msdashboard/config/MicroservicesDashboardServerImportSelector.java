package be.ordina.msdashboard.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Defers our {@code @Configuration}-classes imports to process after normal @Configuration-classes
 *
 * @author Andreas Evers
 */
public class MicroservicesDashboardServerImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] { WebConfiguration.class.getCanonicalName(),
                RedisConfiguration.class.getCanonicalName() };
    }

}
