package cz.loono.backend.api.v1

import cz.loono.backend.schedule.ComingAndPassedExamNotificationTask
import cz.loono.backend.schedule.PlanNewExamReminderTask
import cz.loono.backend.schedule.PreventionReminderTask
import cz.loono.backend.schedule.SelfExamReminderTask
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TriggerController(
    private val comingAndPassedExamNotificationTask: ComingAndPassedExamNotificationTask,
    private val planNewExamReminderTask: PlanNewExamReminderTask,
    private val preventionReminderTask: PreventionReminderTask,
    private val selfExamReminderTask: SelfExamReminderTask
) {

    @GetMapping("v1/notify")
    fun triggerNotifications() {
        comingAndPassedExamNotificationTask.run()
        planNewExamReminderTask.run()
        preventionReminderTask.run()
        selfExamReminderTask.run()
    }
}
