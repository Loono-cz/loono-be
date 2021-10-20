package cz.loono.backend.data.constants

enum class CategoryValues(val value: String) {

    // Závislosti - adiktolog, návykové nemoci
    ADDICTIONS("Závislosti"),

    // Alergologie - alergologie a klinická imunologie
    ALLERGOLOGY("Alergologie"),

    // Imunologie - alergologie a klinická imunologie
    IMMUNOLOGY("Imunologie"),

    // Anesteziologie, ARO, intenzivní péče - anesteziologie a intenzivní medicína, intenzívní medicína, popáleninová medicína, urgentní medicína
    ANESTHESIOLOGY_ARO("Anesteziologie, ARO, intenzivní péče"),

    // Chirurgie -  cévní chirurgie, Chirurgie, Dětská chirurgie, plastická chirurgie, plastická chirurgie, orální a maxilofaciální chirurgie, hrudní chirurgie, onkochirurgie, kardiochirurgie, neurochirurgie
    SURGERY("Chirurgie"),

    // Angiologie, cévní - angiologie
    ANGIOLOGY("Angiologie, cévní"),

    // Endokrinologie, hormony - endokrinologie, dětská endokrinologie a diabetologie
    ENDOCRINOLOGY("Endokrinologie, hormony"),

    // Diabetologie - diabetologie
    DIABETOLOGY("Diabetologie"),

    // Dermatovenerologie, kožní - dětská dermatovenerologie, dermatovenerologie, korektivní dermatologie
    DERMATOVENEROLOGY("Dermatovenerologie, kožní"),

    // Dentální hygiena - Dentální hygienistka
    DENTAL_HYGIENE("Dentální hygiena"),

    // Ergoterapie -  Ergoterapeut
    OCCUPATIONAL_THERAPY("Ergoterapie"),

    // Fyzioterapie - Fyzioterapeut, Odborný fyzioterapeut
    PHYSIOTHERAPY("Fyzioterapie"),

    // Foniatrie - foniatrie
    PHONIATRICS("Foniatrie"),

    // Rehabilitace - rehabilitační a fyzikální medicína
    REHABILITATION("Rehabilitace"),

    // Gastroenterologie - dětská gastroenterologie a hepatologie, gastroenterologie
    GASTROENTEROLOGY("Gastroenterologie"),

    // Geriatrie, medicína stáří, senioři - geriatrie, medicína dlouhodobé péče
    GERIATRICS("Geriatrie, medicína stáří, senioři"),

    // Gynekologie a porodnictví - gynekologie a porodnictví, reprodukční medicína, Porodní asistentka
    GYNECOLOGY("Gynekologie a porodnictví"),

    // Reprodukční medicína - reprodukční mediína
    REPRODUCTIVE_MEDICINE("Reprodukční medicína"),

    // Hematologie, krevní - hematologie a transfúzní lékařství, dětská onkologie a hematologie
    HEMATOLOGY("Hematologie, krevní"),

    // Hygiena - hygiena a epidemiologie, epidemiologie, hygiena a epidemiologie, hygiena výživy a předmětů běžného užívání
    HYGIENE("Hygiena"),

    // Infekční lékařství - infekční lékařství
    INFECTIOUS_MEDICINE("Infekční lékařství"),

    // Interna, vnitřní lékařství - vnitřní lékařství
    INTERNAL_MEDICINE("Interna, vnitřní lékařství"),

    // Kardiologie - kardiologie, dětská kardiologie
    CARDIOLOGY("Kardiologie"),

    // Logopedie - Klinický logoped
    SPEECH_THERAPY("Logopedie"),

    // Letecká medicína - hyperbarická a letecká medicína
    AERO_MEDICINE("Letecká medicína"),

    // Onkologie -  klinická onkologie, radiační onkologie, dětská onkologie a hematologie
    ONCOLOGY("Onkologie"),

    // Biochemie - klinická biochemie
    BIOCHEMISTRY("Biochemie"),

    // Genetika - lékařská genetika
    GENETICS("Genetika"),

    // Mikrobiologie - lékařská mikrobiologie
    MICROBIOLOGY("Mikrobiologie"),

    // Nefrologie, ledviny - nefrologie, dětská nefrologie
    NEPHROLOGY("Nefrologie, ledviny"),

    // Neurologie - neurologie, dětská neurologie
    NEUROLOGY("Neurologie"),

    // Novorozenecké - neonatologie perinatologie a fetomaternální medicína, perinatologie a fetomaternální medicína
    NEONATAL("Novorozenecké"),

    // Oční - oftalmologie, Ortoptista, Optometrista, Zrakový terapeut
    OPHTHALMOLOGY("Oční"),

    // Ortopedie - ortopedie a traumatologie pohybového ústrojí, traumatologie, ortopedická protetika, traumatologie, Ortotik-protetik,  klinická osteologie
    ORTHOPEDICS("Ortopedie"),

    // ORL - otorinolaryngologie a chirurgie hlavy a krku, dětská otorinolaryngologie, foniatrie
    ENT("ORL"),

    // Paliativní medicína a medicína bolesti - algeziologie, paliativní medicína
    PALLIATIVE_MEDICINE("Paliativní medicína a medicína bolesti"),

    // Patologie - patologie
    PATHOLOGY("Patologie"),

    // Pneumologie, plicní - pneumologie a ftizeologie, dětská pneumologie
    PNEUMOLOGY("Pneumologie, plicní"),

    // Praktik - všeobecné praktické lékařství
    GENERAL_PRACTICAL_MEDICINE("Praktik"),

    // Dětský lékař - dětské lékařství, pediatrie, praktické lékařství pro děti a dorost, dorostové lékařství
    PEDIATRICIAN("Dětský lékař"),

    // Psychiatrie - psychiatrie, dětská a dorostová psychiatrie, gerontopsychiatrie
    PSYCHIATRY("Psychiatrie"),

    // Psychologie - Klinický psycholog, Dětský klinický psycholog, psycholog
    PSYCHOLOGY("Psychologie"),

    // Pracovní lékařství - pracovní lékařství, posudkové lékařství
    OCCUPATIONAL_MEDICINE("Pracovní lékařství"),

    // Radiologie a zobrazovací metody - radiologie a zobrazovací metody, radiační onkologie, intervenční radiologie, dětská radiologie, nukleární medicína, Radiologický asistent
    RADIOLOGY("Radiologie a zobrazovací metody"),

    // Revmatologie - revmatologie
    RHEUMATOLOGY("Revmatologie"),

    // Urologie - urologie, dětská urologie
    UROLOGY("Urologie"),

    // Výživa, nutriční - klinická výživa a intenzivní metabolická péče, Nutriční terapeut
    NUTRITION("Výživa, nutriční"),

    // Sestra - Sestra pro péči v interních oborech, Sestra pro péči v psychiatrii, dětská sestra, Všeobecná sestra
    NURSE("Sestra"),

    // Sexuologie - sexuologie
    SEXOLOGY("Sexuologie"),

    // Soudní lékařství - soudní lékařství
    FORENSIC_MEDICINE("Soudní lékařství"),

    // Sportovní medicína -  tělovýchovné lékařství
    SPORTS_MEDICINE("Sportovní medicína"),

    // Sociální pracovník - Zdravotně-sociální pracovník
    SOCIAL_WORKER("Sociální pracovník"),

    // Zubař - zubní lékařství, orální a maxilofaciální chirurgie, ortodoncie, klinická stomatologie
    DENTIST("Zubař"),

    // Rovnátka - ortodoncie
    ORTHODONTICS("Rovnátka"),

    // Zubní technik - zubní technik, Zubní technik pro fixní a snímatelné náhrady, Zubní technik pro ortodoncii
    DENTAL_TECHNICIAN("Zubní technik"),

    // Zdravotní laborant - Zdravotní laborant pro klinickou biochemii, Zdravotní laborant pro klinickou hematologii a transfuzní službu, Zdravotní laborant pro toxikologii, Zdravotní laborant pro cytodiagnostiku, Laboratorní a vyšetřovací metody ve zdravotnictví
    MEDICAL_LABORATORY_TECHNICIAN("Zdravotní laborant"),

    // Zdravotnický záchranář - Zdravotnický záchranář
    PARAMEDIC("Zdravotnický záchranář"),

    // Odborný pracovník - Odborný pracovník v laboratorních  metodách a přípravě léčivých přípravků, Odborný pracovník v ochraně a podpoře veřejného zdraví, Asistent ochrany a podpory veřejného zdraví
    SPECIALIST("Odborný pracovník"),

    // Farmakologie a lékárna - farmaceutická technologie, klinická farmacie, praktické lékárenství, nemocniční lékárenství, klinická farmakologie, Farmaceutický asistent, Farmaceutický asistent pro zdravotnické prostředky
    PHARMACOLOGY("Farmakologie a lékárna"),

    // LDN - medicína dlouhodobé péče
    LDN("LDN"),

    COLOPROCTOLOGY("koloproktologie"),

    // Nezařazovat:
    // Behaviorální analytik
    BEHAVIORAL_ANALYST("Behaviorální analytik"),

    // Veřejné zdravotnictví
    PUBLIC_HEALTHCARE("Veřejné zdravotnictví"),

    // Radiologický fyzik
    RADIOLOGICAL_PHYSICIST("Radiologický fyzik"),

    // psychosomatika
    PSYCHOSOMATICS("Psychosomatika"),

    BIOMEDICAL_TECHNICIAN("Biomedicínský technik")
}
