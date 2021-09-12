package cz.loono.backend.data.repository

import cz.loono.backend.data.model.Account
import cz.loono.backend.data.model.ExaminationRecord
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExaminationRecordRepository : CrudRepository<ExaminationRecord, Long> {
    fun findAllByAccount(account: Account): List<ExaminationRecord>
}
