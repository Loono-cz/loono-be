package cz.loono.backend.schedule

import cz.loono.backend.api.dto.BadgeTypeDto
import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.service.AccountService
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.FirebaseAuthService
import cz.loono.backend.api.service.PreventionService
import cz.loono.backend.configuration.ClockConfiguration
import cz.loono.backend.createAccount
import cz.loono.backend.db.model.Badge
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.db.repository.AccountRepository
import cz.loono.backend.utils.SequenceResetExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

@SpringBootTest(properties = ["spring.profiles.active=test"])
@Import(
    BadgeDowngradeTask::class,
    PreventionService::class,
    ClockConfiguration::class,
    AccountService::class,
    FirebaseAuthService::class,
    ExaminationRecordService::class
)
@Transactional
@ExtendWith(SequenceResetExtension::class)
class BadgeDowngradeTaskTest(
    private val badgeDowngradeTask: BadgeDowngradeTask,
    private val accountRepository: AccountRepository
) {

    @AfterEach
    fun setUp() {
        accountRepository.deleteAll()
    }

    @Test
    fun `Should correctly downgrade badges`() {
        val account = createAccount("uuid1")
        val badges = setOf(
            Badge(
                BadgeTypeDto.GLASSES.toString(),
                1,
                3,
                account,
                LocalDateTime.parse("2020-02-13T17:14:06.00")
            ),
            Badge(
                BadgeTypeDto.HEADBAND.toString(),
                1,
                3,
                account,
                LocalDateTime.parse("2022-02-12T17:14:06.00")
            ),
            Badge(
                BadgeTypeDto.COAT.toString(),
                1,
                3,
                account,
                LocalDateTime.parse("2020-02-12T17:14:06.00")
            )
        )
        accountRepository.save(account)
        val withBadgesAndRecords = accountRepository.findByUid("uuid1")!!.copy(
            badges = badges,
            examinationRecords = listOf(
                ExaminationRecord(
                    type = ExaminationTypeDto.OPHTHALMOLOGIST,
                    plannedDate = LocalDateTime.parse("2020-02-11T17:14:06.00"),
                    account = account,
                    status = ExaminationStatusDto.CONFIRMED
                )
            )
        )
        accountRepository.save(withBadgesAndRecords)

        // Simulate scheduler executed 3 times
        repeat(3) {
            badgeDowngradeTask.run()
        }

        val actual = accountRepository.findByUid("uuid1")!!.badges
        val expected = setOf(
            Badge(
                BadgeTypeDto.GLASSES.toString(),
                1,
                2,
                account,
                LocalDateTime.parse("2024-02-12T17:14:06.00")
            ),
            Badge(
                BadgeTypeDto.HEADBAND.toString(),
                1,
                3,
                account,
                LocalDateTime.parse("2022-02-12T17:14:06.00")
            ),
            Badge(
                BadgeTypeDto.COAT.toString(),
                1,
                2,
                account,
                LocalDateTime.parse("2024-02-12T17:14:06.00")
            )
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Should remove badge when level downgraded to zero`() {
        val account = createAccount("uuid1")
        val badges = setOf(
            Badge(
                BadgeTypeDto.GLASSES.toString(),
                1,
                1,
                account,
                LocalDateTime.parse("2020-02-13T17:14:06.00")
            )
        )
        accountRepository.save(account)
        val withBadgesAndRecords = accountRepository.findByUid("uuid1")!!.copy(
            badges = badges,
            examinationRecords = listOf(
                ExaminationRecord(
                    type = ExaminationTypeDto.OPHTHALMOLOGIST,
                    plannedDate = LocalDateTime.parse("2020-02-11T17:14:06.00"),
                    account = account,
                    status = ExaminationStatusDto.CONFIRMED
                )
            )
        )
        accountRepository.save(withBadgesAndRecords)

        badgeDowngradeTask.run()

        val actual = accountRepository.findByUid("uuid1")!!.badges

        assertThat(actual).isEmpty()
    }

    @TestConfiguration
    class MockedClockConfig {
        @Bean
        @Primary
        fun mockedClock() = mock<Clock>().apply {
            whenever(instant()).thenReturn(Instant.parse("2022-02-12T17:14:06.00Z"))
        }
    }
}
