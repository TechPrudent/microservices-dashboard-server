package com.pxs.dependencies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pxs.utilities.interceptors.MappedDiagnosticContextInterceptor;

@Configuration
@ComponentScan(basePackages = "com.pxs")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableAsync
public class Application extends WebMvcConfigurerAdapter {

	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(mappedDiagnosticContextInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public MappedDiagnosticContextInterceptor mappedDiagnosticContextInterceptor() {
		return new MappedDiagnosticContextInterceptor();
	}

	@Bean
	public CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor() {
		return new CommonAnnotationBeanPostProcessor();
	}
}
