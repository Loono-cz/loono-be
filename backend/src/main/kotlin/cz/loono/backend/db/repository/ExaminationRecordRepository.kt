package cz.loono.backend.db.repository

import cz.loono.backend.api.dto.ExaminationStatusDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExaminationRecordRepository : JpaRepository<ExaminationRecord, Long> {
    fun findByUuid(uuid: String): ExaminationRecord?
    fun findAllByAccountOrderByPlannedDateDesc(account: Account): List<ExaminationRecord>
    fun findByUuidAndAccount(uuid: String, account: Account): ExaminationRecord
    fun findAllByAccountAndTypeAndStatus(
        account: Account,
        type: ExaminationTypeDto,
        status: ExaminationStatusDto
    ): Set<ExaminationRecord>
}
