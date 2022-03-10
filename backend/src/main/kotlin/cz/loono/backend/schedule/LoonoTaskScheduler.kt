package cz.loono.backend.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LoonoTaskScheduler(
    private val dailyTasks: List<DailySchedulerTask>
) {
    @Scheduled(cron = "\${scheduler.cron.daily-task}") // each day at 3AM
    fun executeDailyTasks() {
        dailyTasks.forEach(DailySchedulerTask::run)
    }
}
