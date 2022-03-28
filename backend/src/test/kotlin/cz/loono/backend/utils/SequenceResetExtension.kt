package cz.loono.backend.utils

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

open class SequenceResetExtension : BeforeEachCallback {

    companion object {
        private val SEQUENCES_TO_RESET = listOf(
            "account_seq",
            "examination_record_seq",
            "healthcare_category_seq",
            "selfexamination_record_seq",
        )
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun beforeEach(context: ExtensionContext) {
        logger.info("Resetting sequences $SEQUENCES_TO_RESET")
        val jdbc = jdbc(context)
        SEQUENCES_TO_RESET.forEach {
            jdbc.execute("ALTER SEQUENCE $it RESTART WITH 1")
        }
    }

    private fun jdbc(context: ExtensionContext) = SpringExtension.getApplicationContext(context)
        .getBean(JdbcTemplate::class.java)
}
