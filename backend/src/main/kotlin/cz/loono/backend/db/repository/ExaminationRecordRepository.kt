package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExaminationRecordRepository : CrudRepository<ExaminationRecord, Long> {
    fun findByUuid(uuid: String): ExaminationRecord?
    fun findAllByAccountOrderByPlannedDateDesc(account: Account): Set<ExaminationRecord>
    fun findByUuidAndAccount(uuid: String, account: Account): ExaminationRecord
}
