package com.payconiq.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.payqonic.data"})
@ComponentScan(basePackages = {"com.payqonic"})
@EntityScan(basePackages = {"com.payqonic.data"})
public class StocksConfig {
}
