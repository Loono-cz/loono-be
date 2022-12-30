package cz.loono.backend.schedule

import cz.loono.backend.db.model.CronLog
import cz.loono.backend.db.repository.CronLogRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class LoonoTaskScheduler(
    private val dailyTasks: List<DailySchedulerTask>,
    private val cronLogRepository: CronLogRepository
) {
    @Scheduled(cron = "\${scheduler.cron.daily-task}") // each day at 3AM
    fun executeDailyTasks() {
        // dailyTasks.forEach(DailySchedulerTask::run)
        dailyTasks.forEach { task ->
            task.run()
            cronLogRepository.save(
                CronLog(
                    functionName = "${task.javaClass::getName}",
                    status = "RUN CHECK",
                    message = "$task",
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}
