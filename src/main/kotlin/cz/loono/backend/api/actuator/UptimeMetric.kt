package cz.loono.backend.api.actuator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.stereotype.Component

@Component
class UptimeMetric : HealthIndicator {

    @Autowired
    private lateinit var metricsEndpoint: MetricsEndpoint

    override fun health(): Health {
        val uptime = metricsEndpoint.metric("process.uptime", null).measurements
        return Health.up().withDetail("Server uptime", uptime[0].value).build()
    }
}
