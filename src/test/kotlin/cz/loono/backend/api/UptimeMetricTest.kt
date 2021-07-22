package cz.loono.backend.api

import cz.loono.backend.api.actuator.UptimeMetric
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.metrics.MetricsEndpoint

class UptimeMetricTest {

    @InjectMocks
    private lateinit var uptimeMetric: UptimeMetric

    @Mock
    private lateinit var metricsEndpoint: MetricsEndpoint

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testUptimeMetric() {

        val metricResponse = mock<MetricsEndpoint.MetricResponse>()
        val sample = mock<MetricsEndpoint.Sample>()
        whenever(sample.value).thenReturn(2.4)
        whenever(metricResponse.measurements).thenReturn(listOf(sample))
        whenever(metricsEndpoint.metric("process.uptime", null)).thenReturn(metricResponse)

        val health = uptimeMetric.health()

        assert(health.details["Server uptime"] == 2.4)
    }
}
