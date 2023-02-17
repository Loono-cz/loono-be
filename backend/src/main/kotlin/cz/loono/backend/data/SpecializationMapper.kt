package cz.loono.backend.data

import cz.loono.backend.api.dto.HealthcareCategoryTypeDto
import org.slf4j.LoggerFactory

class SpecializationMapper {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun defineCategory(specialization: String): List<String> {

        val specializations = specialization.split(", ")
        val categories = mutableListOf<String>()

        specializations.forEach {
            when (it.lowercase()) {
                "adiktolog", "návykové nemoci" -> {
                    categories.add(HealthcareCategoryTypeDto.ADDICTIONS.value)
                }
                "alergologie a klinická imunologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ALLERGOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.IMMUNOLOGY.value)
                }
                "anesteziologie a intenzivní medicína", "intenzívní medicína",
                "popáleninová medicína", "urgentní medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.ANESTHESIOLOGY_ARO.value)
                }
                "cévní chirurgie", "chirurgie", "dětská chirurgie", "plastická chirurgie",
                "hrudní chirurgie", "onkochirurgie", "kardiochirurgie", "neurochirurgie",
                "maxilofaciální chirurgie", "spondylochirurgie" -> {
                    categories.add(HealthcareCategoryTypeDto.SURGERY.value)
                }
                "angiologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ANGIOLOGY.value)
                }
                "endokrinologie", "endokrinologie a diabetologie", "dětská endokrinologie a diabetologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ENDOCRINOLOGY.value)
                }
                "diabetologie" -> {
                    categories.add(HealthcareCategoryTypeDto.DIABETOLOGY.value)
                }
                "dětská dermatovenerologie", "dermatovenerologie", "korektivní dermatologie" -> {
                    categories.add(HealthcareCategoryTypeDto.DERMATOVENEROLOGY.value)
                }
                "dentální hygienistka" -> {
                    categories.add(HealthcareCategoryTypeDto.DENTAL_HYGIENE.value)
                }
                "ergoterapeut" -> {
                    categories.add(HealthcareCategoryTypeDto.OCCUPATIONAL_THERAPY.value)
                }
                "fyzioterapeut", "odborný fyzioterapeut" -> {
                    categories.add(HealthcareCategoryTypeDto.PHYSIOTHERAPY.value)
                }
                "foniatrie" -> {
                    categories.add(HealthcareCategoryTypeDto.PHONIATRICS.value)
                    categories.add(HealthcareCategoryTypeDto.ENT.value)
                }
                "rehabilitační a fyzikální medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.REHABILITATION.value)
                }
                "dětská gastroenterologie a hepatologie", "gastroenterologie", "koloproktologie" -> {
                    categories.add(HealthcareCategoryTypeDto.GASTROENTEROLOGY.value)
                }
                "geriatrie" -> {
                    categories.add(HealthcareCategoryTypeDto.GERIATRICS.value)
                }
                "gynekologie a porodnictví", "porodní asistentka" -> {
                    categories.add(HealthcareCategoryTypeDto.GYNECOLOGY.value)
                }
                "gynekologie dětí a dospívajících" -> {
                    categories.add(HealthcareCategoryTypeDto.GYNECOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.PEDIATRICIAN.value)
                }
                "onkogynekologie" -> {
                    categories.add(HealthcareCategoryTypeDto.GYNECOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.ONCOLOGY.value)
                }
                "urogynekologie" -> {
                    categories.add(HealthcareCategoryTypeDto.GYNECOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.UROLOGY.value)
                }
                "reprodukční medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.REPRODUCTIVE_MEDICINE.value)
                    categories.add(HealthcareCategoryTypeDto.GYNECOLOGY.value)
                }
                "hematologie a transfúzní lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.HEMATOLOGY.value)
                }
                "dětská onkologie a hematologie" -> {
                    categories.add(HealthcareCategoryTypeDto.HEMATOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.ONCOLOGY.value)
                }
                "hygiena a epidemiologie", "epidemiologie", "hygiena výživy a předmětů běžného užívání",
                "hygiena obecná a komunální", "hygiena dětí a dorostu" -> {
                    categories.add(HealthcareCategoryTypeDto.HYGIENE.value)
                }
                "infekční lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.INFECTIOUS_MEDICINE.value)
                }
                "vnitřní lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.INTERNAL_MEDICINE.value)
                }
                "kardiologie", "dětská kardiologie" -> {
                    categories.add(HealthcareCategoryTypeDto.CARDIOLOGY.value)
                }
                "klinický logoped" -> {
                    categories.add(HealthcareCategoryTypeDto.SPEECH_THERAPY.value)
                }
                "hyperbarická a letecká medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.AERO_MEDICINE.value)
                }
                "klinická onkologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ONCOLOGY.value)
                }
                "radiační onkologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ONCOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.RADIOLOGY.value)
                }
                "klinická biochemie" -> {
                    categories.add(HealthcareCategoryTypeDto.BIOCHEMISTRY.value)
                }
                "lékařská genetika" -> {
                    categories.add(HealthcareCategoryTypeDto.GENETICS.value)
                }
                "lékařská mikrobiologie" -> {
                    categories.add(HealthcareCategoryTypeDto.MICROBIOLOGY.value)
                }
                "nefrologie", "dětská nefrologie" -> {
                    categories.add(HealthcareCategoryTypeDto.NEPHROLOGY.value)
                }
                "neurologie", "dětská neurologie" -> {
                    categories.add(HealthcareCategoryTypeDto.NEUROLOGY.value)
                }
                "neonatologie", "neonatologie perinatologie a fetomaternální medicína",
                "perinatologie a fetomaternální medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.NEONATAL.value)
                }
                "oftalmologie", "ortoptista", "optometrista", "zrakový terapeut" -> {
                    categories.add(HealthcareCategoryTypeDto.OPHTHALMOLOGY.value)
                }
                "ortopedie a traumatologie pohybového ústrojí", "traumatologie", "ortopedická protetika",
                "ortotik-protetik", "klinická osteologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ORTHOPEDICS.value)
                }
                "otorinolaryngologie a chirurgie hlavy a krku", "dětská otorinolaryngologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ENT.value)
                }
                "algeziologie", "paliativní medicína" -> {
                    categories.add(HealthcareCategoryTypeDto.PALLIATIVE_MEDICINE.value)
                }
                "patologie" -> {
                    categories.add(HealthcareCategoryTypeDto.PATHOLOGY.value)
                }
                "pneumologie a ftizeologie", "dětská pneumologie" -> {
                    categories.add(HealthcareCategoryTypeDto.PNEUMOLOGY.value)
                }
                "všeobecné praktické lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.GENERAL_PRACTICAL_MEDICINE.value)
                }
                "dětské lékařství", "pediatrie", "praktické lékařství pro děti a dorost", "dorostové lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.PEDIATRICIAN.value)
                }
                "psychiatrie", "dětská a dorostová psychiatrie", "gerontopsychiatrie" -> {
                    categories.add(HealthcareCategoryTypeDto.PSYCHIATRY.value)
                }
                "klinický psycholog", "dětský klinický psycholog", "psycholog" -> {
                    categories.add(HealthcareCategoryTypeDto.PSYCHOLOGY.value)
                }
                "pracovní lékařství", "posudkové lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.OCCUPATIONAL_MEDICINE.value)
                }
                "radiologie a zobrazovací metody", "intervenční radiologie", "neuroradiologie",
                "dětská radiologie", "nukleární medicína", "radiologický asistent",
                "vaskulární intervenční radiologie", "vaskulární inter", "radiologický technik" -> {
                    categories.add(HealthcareCategoryTypeDto.RADIOLOGY.value)
                }
                "onkourologie" -> {
                    categories.add(HealthcareCategoryTypeDto.ONCOLOGY.value)
                    categories.add(HealthcareCategoryTypeDto.UROLOGY.value)
                }
                "revmatologie", "dětská revmatologie" -> {
                    categories.add(HealthcareCategoryTypeDto.RHEUMATOLOGY.value)
                }
                "urologie", "dětská urologie" -> {
                    categories.add(HealthcareCategoryTypeDto.UROLOGY.value)
                }
                "klinická výživa a intenzivní metabolická péče", "nutriční terapeut", "nutriční podpora" -> {
                    categories.add(HealthcareCategoryTypeDto.NUTRITION.value)
                }
                "sestra pro péči v interních oborech", "sestra pro péči v psychiatrii", "dětská sestra",
                "všeobecná sestra", "sestra pro intenzivní péči", "praktická sestra" -> {
                    categories.add(HealthcareCategoryTypeDto.NURSE.value)
                }
                "sexuologie" -> {
                    categories.add(HealthcareCategoryTypeDto.SEXOLOGY.value)
                }
                "soudní lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.FORENSIC_MEDICINE.value)
                }
                "tělovýchovné lékařství" -> {
                    categories.add(HealthcareCategoryTypeDto.SPORTS_MEDICINE.value)
                }
                "zdravotně-sociální pracovník" -> {
                    categories.add(HealthcareCategoryTypeDto.SOCIAL_WORKER.value)
                }
                "zubní lékařství", "klinická stomatologie" -> {
                    categories.add(HealthcareCategoryTypeDto.DENTIST.value)
                }
                "orální a maxilofaciální chirurgie" -> {
                    categories.add(HealthcareCategoryTypeDto.DENTIST.value)
                    categories.add(HealthcareCategoryTypeDto.SURGERY.value)
                }
                "ortodoncie" -> {
                    categories.add(HealthcareCategoryTypeDto.ORTHODONTICS.value)
                    categories.add(HealthcareCategoryTypeDto.DENTIST.value)
                }
                "zubní technik", "zubní technik pro fixní a snímatelné náhrady", "zubní technik pro ortodoncii" -> {
                    categories.add(HealthcareCategoryTypeDto.DENTAL_TECHNICIAN.value)
                }
                "biomedicínský technik", "biomedicínský inženýr" -> {
                    categories.add(HealthcareCategoryTypeDto.BIOMEDICAL_TECHNICIAN.value)
                }
                "zdravotní laborant pro klinickou biochemii",
                "zdravotní laborant pro klinickou hematologii a transfuzní službu",
                "zdravotní laborant pro toxikologii", "zdravotní laborant pro cytodiagnostiku",
                "laboratorní a vyšetřovací metody ve zdravotnictví",
                "zdravotní laborant pro alergologii a klinickou imunologii",
                "zdravotní laborant pro klinickou genetiku", "zdravotní laborant",
                "zdravotní laborant pro mikrobiologii" -> {
                    categories.add(HealthcareCategoryTypeDto.MEDICAL_LABORATORY_TECHNICIAN.value)
                }
                "zdravotnický záchranář" -> {
                    categories.add(HealthcareCategoryTypeDto.PARAMEDIC.value)
                }
                "odborný pracovník v laboratorních  metodách a přípravě léčivých přípravků",
                "odborný pracovník v ochraně a podpoře veřejného zdraví",
                "asistent ochrany a podpory veřejného zdraví",
                "odborný pracovník v ochraně a podpoře veřejného zdraví pro hygienu a epidemiologii",
                "bioanalytik pro mikrobiologii", "bioanalytik pro klinickou genetiku" -> {
                    categories.add(HealthcareCategoryTypeDto.SPECIALIST.value)
                }
                "farmaceutická technologie", "klinická farmacie", "praktické lékárenství", "nemocniční lékárenství",
                "klinická farmakologie", "farmaceutický asistent", "farmaceutická kontrola",
                "farmaceutický asistent pro zdravotnické prostředky", "onkologická farmacie",
                "radiofarmaka" -> {
                    categories.add(HealthcareCategoryTypeDto.PHARMACOLOGY.value)
                }
                "medicína dlouhodobé péče" -> {
                    categories.add(HealthcareCategoryTypeDto.LDN.value)
                    categories.add(HealthcareCategoryTypeDto.GERIATRICS.value)
                }
                "behaviorální analytik" -> {
                    categories.add(HealthcareCategoryTypeDto.BEHAVIORAL_ANALYST.value)
                }
                "veřejné zdravotnictví" -> {
                    categories.add(HealthcareCategoryTypeDto.PUBLIC_HEALTHCARE.value)
                }
                "radiologický fyzik" -> {
                    categories.add(HealthcareCategoryTypeDto.RADIOLOGICAL_PHYSICIST.value)
                }
                "psychosomatika" -> {
                    categories.add(HealthcareCategoryTypeDto.PSYCHOSOMATICS.value)
                }
                "", "oborpece" -> {
                    // No specialization
                }
                else -> {
                    logger.warn("Unknown category $it")
                }
            }
        }
        return categories
    }
}
