package com.devtool.designparttern.strategy;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2024/1/30
 */
@Component
public class StrategyFactory implements InitializingBean, ApplicationContextAware {

    private final Map<String, Strategy> strategyMap = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Strategy> strategyMap = this.applicationContext.getBeansOfType(Strategy.class);

        if (strategyMap.isEmpty()) {
            return;
        }

        this.strategyMap.putAll(strategyMap);
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Strategy getStrategy(String name) {
        return this.getStrategy(name, Strategy.class);
    }

    public <T> T getStrategy(String name, Class<T> clazz) {
        try {
            Strategy strategy = this.strategyMap.get(name);
            return clazz.cast(strategy);
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
