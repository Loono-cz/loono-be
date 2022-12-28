package cz.loono.backend.metrics

import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {

    @Bean
    fun countedAspect(registry: MeterRegistry): CountedAspect {
        return CountedAspect(registry)
    }
}
