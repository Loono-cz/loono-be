package cz.loono.backend.db.repository

import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import cz.loono.backend.db.model.Account
import cz.loono.backend.db.model.SelfExaminationRecord
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SelfExaminationRecordRepository : CrudRepository<SelfExaminationRecord, Long> {
    fun findAllByStatus(status: SelfExaminationStatusDto): Set<SelfExaminationRecord>
    fun findAllByAccount(account: Account): Set<SelfExaminationRecord>
    fun findAllByAccountAndTypeOrderByDueDateDesc(account: Account, type: SelfExaminationTypeDto): List<SelfExaminationRecord>
    fun deleteAllByAccount(account: Account)
    fun findFirstByAccountAndTypeOrderByDueDateDesc(account: Account, type: SelfExaminationTypeDto): SelfExaminationRecord
}
