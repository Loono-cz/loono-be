package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.SelfExaminationRecord
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SelfExaminationRecordRepository : CrudRepository<SelfExaminationRecord, Long> {
    fun findByUuid(uuid: String): SelfExaminationRecord?
    fun findAllByAccount(account: Account): Set<SelfExaminationRecord>
    fun findByUuidAndAccount(uuid: String, account: Account): SelfExaminationRecord
}
