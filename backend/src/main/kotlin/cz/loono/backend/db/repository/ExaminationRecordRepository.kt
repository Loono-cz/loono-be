package cz.loono.backend.db.repository

import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.ExaminationRecord
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExaminationRecordRepository : CrudRepository<ExaminationRecord, Long> {
    fun findAllByAccount(account: Account): List<ExaminationRecord>
}
