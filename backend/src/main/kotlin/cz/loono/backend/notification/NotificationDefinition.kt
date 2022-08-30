package cz.loono.backend.notification

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.SexDto
import cz.loono.backend.api.service.PushNotificationService.Companion.ONESIGNAL_APP_ID
import cz.loono.backend.db.model.Account

object NotificationDefinition {

    private const val MORNING_TIME_TO_NOTIFY = "10:00AM"
    private const val EVENING_TIME_TO_NOTIFY = "6:00PM"
    private const val URL_TO_NOTIFICATION = "https://app.devel.loono.cz/notification/"
    private val notificationTextManager = NotificationTextManager()

    fun getPreventionNotification(accounts: Set<Account>): PushNotification {
        val name = "Prevention notification"
        val title = notificationTextManager.getText("prevention.title")
        val text = notificationTextManager.getText("prevention.text")
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = MORNING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "main")
        )
    }

    fun getCompletionNotification(
        accounts: Set<Account>,
        time: String,
        examinationTypeDto: ExaminationTypeDto
    ): PushNotification {
        val name = "Complete checkup notification"
        val title = notificationTextManager.getText("completion.title")
        val text = notificationTextManager.getText("completion.text")
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = time, // time of the past exam - reminder after 24h
            data = NotificationData(screen = "checkup", examinationType = examinationTypeDto)
        )
    }

    fun getOrderNewExam2MonthsAheadNotification(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        interval: Int
    ): PushNotification {
        val name = "Order reminder 2 months ahead notification"
        val title = notificationTextManager.getText("order.2months.ahead.title", examinationTypeDto)
        val text = notificationTextManager.getText("order.2months.ahead.text", interval)
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = MORNING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "checkup", examinationType = examinationTypeDto)
        )
    }

    fun getOrderNewExamMonthAheadNotification(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        interval: Int,
        badgeTypeDto: BadgeTypeDto
    ): PushNotification {
        val name = "Order reminder 1 month ahead notification"
        val title = notificationTextManager.getText("order.month.ahead.title", examinationTypeDto)
        val text = notificationTextManager.getText("order.month.ahead.text", interval, badgeTypeDto)
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = MORNING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "checkup", examinationType = examinationTypeDto)
        )
    }

    fun getComingVisitNotification(
        accounts: Set<Account>,
        examinationTypeDto: ExaminationTypeDto,
        time: String
    ): PushNotification {
        val name = "Coming visit notification"
        val title = notificationTextManager.getText("coming.visit.title")
        val text = notificationTextManager.getText("coming.visit.text", examinationTypeDto)
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = time, // time of the coming exam - reminder 24h ahead
            data = NotificationData(screen = "checkup", examinationType = examinationTypeDto)
        )
    }

    fun getFirstSelfExamNotification(accounts: Set<Account>): PushNotification {
        val name = "First self-exam notification"
        val title = notificationTextManager.getText("self.first.title")
        val text = notificationTextManager.getText("self.first.text")
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = EVENING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "self"),
        )
    }

    fun getSelfExamNotification(accounts: Set<Account>): PushNotification {
        val name = "Self-exam notification"
        val title = notificationTextManager.getText("self.common.title")
        val text = notificationTextManager.getText("self.common.text")
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = EVENING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "self"),
        )
    }

    fun getSelfExamIssueResultNotification(accounts: Set<Account>): PushNotification {
        val name = "Issue result of self-exam notification"
        val title = notificationTextManager.getText("self.result.title")
        val text = notificationTextManager.getText("self.result.text")
        return PushNotification(
            appId = ONESIGNAL_APP_ID,
            name = name,
            headings = MultipleLangString(cs = title, en = title),
            contents = MultipleLangString(cs = text, en = text),
            includeExternalUserIds = accounts.map { it.uid },
            scheduleTimeOfDay = EVENING_TIME_TO_NOTIFY,
            data = NotificationData(screen = "self")
        )
    }
}
