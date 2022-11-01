package cz.loono.backend.api.v1

import cz.loono.backend.schedule.ComingAndPassedExamNotificationTask
import cz.loono.backend.schedule.PlanNewExamReminderTask
import cz.loono.backend.schedule.PreventionReminderTask
import cz.loono.backend.schedule.SelfExamReminderTask
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(headers = ["app-version"])
@ManagedResource(objectName = "LoonoMBean:category=MBeans,name=triggerBean")
class TriggerController(
    private val comingAndPassedExamNotificationTask: ComingAndPassedExamNotificationTask,
    private val planNewExamReminderTask: PlanNewExamReminderTask,
    private val preventionReminderTask: PreventionReminderTask,
    private val selfExamReminderTask: SelfExamReminderTask
) {

    @GetMapping("v1/notify")
    @ManagedOperation
    fun triggerNotifications() {
        comingAndPassedExamNotificationTask.run()
        planNewExamReminderTask.run()
        preventionReminderTask.run()
        selfExamReminderTask.run()
    }
}
