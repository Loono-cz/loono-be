package cz.loono.backend.api.actuator

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory

@Component
class UptimeMetric : HealthIndicator {

    override fun health(): Health {
        val mxBean = ManagementFactory.getRuntimeMXBean()
        return Health.up().withDetail("Server uptime", mxBean.uptime).build()
    }
}
