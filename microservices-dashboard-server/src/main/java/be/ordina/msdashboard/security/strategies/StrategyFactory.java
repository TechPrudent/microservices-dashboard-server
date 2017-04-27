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
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Factory class to create SecurityStrategy annotated classes
 *
 * @author Kevin Van Houtte
 */
@Repository
public class StrategyFactory {

    private ApplicationContext applicationContext;

    private Map<Class, List<Object>> annotatedTypes = new HashMap<>();
    private Map<Class, SecurityStrategy> strategyCache = new HashMap<>();

    public StrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> annotatedBeanClasses = applicationContext.getBeansWithAnnotation(SecurityStrategy.class);

        sanityCheck(annotatedBeanClasses.values());

        for (Object bean : annotatedBeanClasses.values()) {
            SecurityStrategy securityStrategyAnnotation = strategyCache.get(bean.getClass());
            getBeansWithSameType(securityStrategyAnnotation).add(bean);
        }
    }

    private void sanityCheck(Collection<Object> annotatedBeanClasses) {
        Set<String> usedStrategies = new HashSet<>();

        for (Object bean : annotatedBeanClasses) {
            SecurityStrategy securityStrategyAnnotation = AnnotationUtils.findAnnotation(bean.getClass(), SecurityStrategy.class);
            if (securityStrategyAnnotation == null) {
                try {
                    Object target = ((Advised) bean).getTargetSource().getTarget();
                    securityStrategyAnnotation = AnnotationUtils.findAnnotation(target.getClass(), SecurityStrategy.class);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Can't get a target");
                }
            }
            strategyCache.put(bean.getClass(), securityStrategyAnnotation);
            ifNotExistAdd(securityStrategyAnnotation.type(), strategyCache.get(bean.getClass()).protocol(), usedStrategies);
        }
    }

    private void ifNotExistAdd(Class type, SecurityProtocol profile, Set<String> usedStrategies) {
        ifNotExistAdd(type, profile.name(), usedStrategies);
    }

    private void ifNotExistAdd(Class type, String profile, Set<String> usedStrategies) {
        if (usedStrategies.contains(createKey(type, profile))) {
            throw new IllegalArgumentException("There can only be a single strategy for each type, found multiple for type '" + type + "' and profile '" + profile + "'");
        }
        usedStrategies.add(createKey(type, profile));
    }

    private String createKey(Class type, String profile) {
        return (type + "_" + profile).toLowerCase();
    }

    private List<Object> getBeansWithSameType(SecurityStrategy securityStrategyAnnotation) {
        List<Object> beansWithSameType = annotatedTypes.get(securityStrategyAnnotation.type());
        if (beansWithSameType != null) {
            return beansWithSameType;
        } else {
            List<Object> newBeansList = new ArrayList<>();
            annotatedTypes.put(securityStrategyAnnotation.type(), newBeansList);
            return newBeansList;
        }
    }


    public <T> T getStrategy(Class<T> strategyType, SecurityProtocol securityProtocol) {
        List<Object> strategyBeans = annotatedTypes.get(strategyType);
        Assert.notEmpty(strategyBeans, "No strategies found of type '" + strategyType.getName() + "', are the strategies marked with @SecurityStrategy?");

        Object profileStrategy = findStrategyMatchingProfile(strategyBeans, securityProtocol);
        if (profileStrategy == null) {
            throw new IllegalArgumentException("No strategy found for type '" + strategyType + "'");
        }
        //noinspection unchecked
        return (T) profileStrategy;
    }

    private Object findStrategyMatchingProfile(List<Object> strategyBeans, SecurityProtocol securityProtocol) {
        Object defaultStrategy = null;
        for (Object bean : strategyBeans) {
            SecurityStrategy securityStrategyAnnotation = strategyCache.get(bean.getClass());
            if (Objects.equals(securityStrategyAnnotation.protocol(),securityProtocol)) {
                return bean;
            }
        }
        return defaultStrategy;
    }
}