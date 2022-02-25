package cz.loono.backend.schedule

/**
 * Any implementation of this interface would be automatically picked up by LoonoTaskScheduler and executed
 * once a day using the cron expression specified at 'scheduler.cron.data-update'
 */
interface DailySchedulerTask {
    fun run()
}
