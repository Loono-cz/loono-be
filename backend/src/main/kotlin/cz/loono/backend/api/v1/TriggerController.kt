package cz.loono.backend.api.v1

import cz.loono.backend.api.service.AccountService
import cz.loono.backend.schedule.ComingAndPassedExamNotificationTask
import cz.loono.backend.schedule.PlanNewExamReminderTask
import cz.loono.backend.schedule.PreventionReminderTask
import cz.loono.backend.schedule.SelfExamReminderTask
import cz.loono.backend.schedule.UserSubscribeNewsletter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(headers = ["app-version"])
class TriggerController(
    private val userSubscribeNewsletter: UserSubscribeNewsletter,
    private val comingAndPassedExamNotificationTask: ComingAndPassedExamNotificationTask,
    private val planNewExamReminderTask: PlanNewExamReminderTask,
    private val preventionReminderTask: PreventionReminderTask,
    private val selfExamReminderTask: SelfExamReminderTask,
    private val accountService: AccountService
) {

    @GetMapping("v1/notify")
    fun triggerNotifications() {
        userSubscribeNewsletter.run()
//        comingAndPassedExamNotificationTask.run()
//        planNewExamReminderTask.run()
//        preventionReminderTask.run()
//        selfExamReminderTask.run()
    }

    @GetMapping("v1/FBandDBDiff")
    fun checkDifferenceFBandDB(): List<String> {
        return accountService.checkDifferenceFBandDB()
    }
}
