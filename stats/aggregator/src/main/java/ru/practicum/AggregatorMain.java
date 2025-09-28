package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.service.AggregationStarter;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregatorMain {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorMain.class, args);

        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}