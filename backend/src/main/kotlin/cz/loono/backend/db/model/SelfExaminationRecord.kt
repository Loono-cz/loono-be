package cz.loono.backend.db.model

import cz.loono.backend.api.dto.SelfExaminationResultDto
import cz.loono.backend.api.dto.SelfExaminationStatusDto
import cz.loono.backend.api.dto.SelfExaminationTypeDto
import org.hibernate.envers.Audited
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "selfexamination_record")
@Audited
data class SelfExaminationRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val type: SelfExaminationTypeDto = SelfExaminationTypeDto.BREAST,

    @Column
    val dueDate: LocalDate? = null,

    @ManyToOne(optional = false)
    val account: Account = Account(),

    @Column(nullable = true, columnDefinition = "TEXT")
    val result: SelfExaminationResultDto? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var status: SelfExaminationStatusDto = SelfExaminationStatusDto.PLANNED,

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    val uuid: String = UUID.randomUUID().toString()
)
