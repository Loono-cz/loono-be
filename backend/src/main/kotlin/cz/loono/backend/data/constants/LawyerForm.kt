package cz.loono.backend.data.constants

import java.time.LocalDate

data class LawyerForm(
    val code: Int,
    val name: String,
    val personType: PersonType,
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null
) {
    companion object {
        fun ofCode(code: Int): LawyerForm =
            lawyerFormsByCodes[code] ?: throw NullPointerException("Code of Lawyer form cannot by null.")

        private val lawyerFormsByCodes: Map<Int, LawyerForm> by lazy {
            mapOf(
                100 to LawyerForm(100, "Podnikající fyzická osoba tuzemská", PersonType.NATURAL_PERSON),
                111 to LawyerForm(111, "Veřejná obchodní společnost", PersonType.LEGAL_PERSON),
                112 to LawyerForm(112, "Společnost s ručením omezeným", PersonType.LEGAL_PERSON),
                113 to LawyerForm(113, "Společnost komanditní", PersonType.LEGAL_PERSON),
                114 to LawyerForm(
                    114, "Společnost komanditní na akcie", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                115 to LawyerForm(
                    115, "Společný podnik", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                116 to LawyerForm(
                    116, "Zájmové sdružení", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                117 to LawyerForm(117, "Nadace", PersonType.LEGAL_PERSON),
                118 to LawyerForm(118, "Nadační fond", PersonType.LEGAL_PERSON),
                121 to LawyerForm(121, "Akciová společnost", PersonType.LEGAL_PERSON),
                131 to LawyerForm(
                    131, "Svépomocné zemědělské družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                141 to LawyerForm(141, "Obecně prospěšná společnost", PersonType.LEGAL_PERSON),
                145 to LawyerForm(145, "Společenství vlastníků jednotek", PersonType.LEGAL_PERSON),
                151 to LawyerForm(151, "Komoditní burza", PersonType.LEGAL_PERSON),
                152 to LawyerForm(152, "Garanční fond obchodníků s cennými papíry", PersonType.LEGAL_PERSON),
                161 to LawyerForm(
                    161, "Ústav", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                201 to LawyerForm(
                    201, "Zemědělské družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                205 to LawyerForm(205, "Družstvo", PersonType.LEGAL_PERSON),
                211 to LawyerForm(
                    211, "Družstevní podnik zemědělský", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                231 to LawyerForm(
                    231, "Výrobní družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                232 to LawyerForm(
                    232, "Spotřební družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                233 to LawyerForm(
                    233, "Bytové družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                234 to LawyerForm(
                    234, "Jiné družstvo", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                241 to LawyerForm(
                    241, "Družstevní podnik (s 1 zakladatelem)", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                242 to LawyerForm(
                    242, "Společný podnik (s více zakladateli)", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                251 to LawyerForm(
                    251, "Zájmová organizace družstev", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                261 to LawyerForm(
                    261, "Společná zájmová organizace družstev", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                301 to LawyerForm(301, "Státní podnik", PersonType.LEGAL_PERSON),
                302 to LawyerForm(302, "Národní podnik", PersonType.LEGAL_PERSON),
                311 to LawyerForm(
                    311, "Státní banka československá", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                312 to LawyerForm(
                    312, "Banka-státní peněžní ústav", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                313 to LawyerForm(313, "Česká národní banka", PersonType.LEGAL_PERSON),
                314 to LawyerForm(
                    314, "Česká konsolidační agentura", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                325 to LawyerForm(325, "Organizační složka státu", PersonType.LEGAL_PERSON),
                326 to LawyerForm(326, "Stálý rozhodčí soud", PersonType.LEGAL_PERSON),
                331 to LawyerForm(
                    331,
                    "Příspěvková organizace zřízená územním samosprávným celkem",
                    PersonType.LEGAL_PERSON
                ),
                332 to LawyerForm(332, "Státní příspěvková organizace", PersonType.LEGAL_PERSON),
                333 to LawyerForm(
                    333, "Státní příspěvková organizace ostatní", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2016, 12, 31)
                ),
                341 to LawyerForm(
                    341, "Státní hospodářská organizace řízená okresním úřadem", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                343 to LawyerForm(
                    343, "Obecní podnik", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                351 to LawyerForm(
                    351, "Československé státní dráhy-státní organizace", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                352 to LawyerForm(352, "Státní organizace Správa železnic", PersonType.LEGAL_PERSON),
                353 to LawyerForm(353, "Rada pro veřejný dohled nad auditem", PersonType.LEGAL_PERSON),
                361 to LawyerForm(361, "Veřejnoprávní instituce", PersonType.LEGAL_PERSON),
                362 to LawyerForm(362, "Česká tisková kancelář", PersonType.LEGAL_PERSON),
                381 to LawyerForm(381, "Státní fond ze zákona", PersonType.LEGAL_PERSON),
                382 to LawyerForm(
                    382,
                    "Státní fond ze zákona nezapisující se do obchodního rejstříku",
                    PersonType.LEGAL_PERSON
                ),
                391 to LawyerForm(391, "Zdravotní pojišťovna (mimo VZP)", PersonType.LEGAL_PERSON),
                392 to LawyerForm(392, "Všeobecná zdravotní pojišťovna", PersonType.LEGAL_PERSON),
                401 to LawyerForm(
                    401, "Sdružení mezinárodního obchodu", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                411 to LawyerForm(
                    411, "Podnik se zahraniční majetkovou účastí", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                421 to LawyerForm(421, "Odštěpný závod zahraniční právnické osoby", PersonType.LEGAL_PERSON),
                422 to LawyerForm(422, "Organizační složka zahraničního nadačního fondu", PersonType.LEGAL_PERSON),
                423 to LawyerForm(423, "Organizační složka zahraniční nadace", PersonType.LEGAL_PERSON),
                424 to LawyerForm(424, "Zahraniční fyzická osoba", PersonType.NATURAL_PERSON),
                425 to LawyerForm(
                    425, "Odštěpný závod zahraniční fyzické osoby fyzická osoba", PersonType.NATURAL_PERSON,
                    validFrom = LocalDate.of(2013, 10, 1)
                ),
                426 to LawyerForm(
                    426, "Zastoupení zahraniční banky", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2017, 9, 1)
                ),
                441 to LawyerForm(
                    441, "Podnik zahraničního obchodu", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                442 to LawyerForm(
                    442, "Účelová zahraničně obchodní organizace", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                501 to LawyerForm(501, "Odštěpný závod", PersonType.LEGAL_PERSON),
                521 to LawyerForm(521, "Samostatná drobná provozovna (obecního úřadu)", PersonType.LEGAL_PERSON),
                525 to LawyerForm(
                    525, "Vnitřní organizační jednotka organizační složky státu", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2017, 1, 1)
                ),
                601 to LawyerForm(601, "Vysoká škola (veřejná, státní)", PersonType.LEGAL_PERSON),
                641 to LawyerForm(641, "Školská právnická osoba", PersonType.LEGAL_PERSON),
                661 to LawyerForm(661, "Veřejná výzkumná instituce", PersonType.LEGAL_PERSON),
                671 to LawyerForm(
                    671, "Veřejné neziskové ústavní zdravotnické zařízení", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2011, 12, 30)
                ),
                701 to LawyerForm(
                    701, "Občanské sdružení", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2013, 12, 31)
                ),
                703 to LawyerForm(
                    703, "Odborová organizace a organizacezaměstnavatelů", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2017, 12, 31)
                ),
                704 to LawyerForm(
                    704,
                    "Zvláštní organizace pro zastoupení českých zájmů v mezinárodních nevládních organizacích",
                    PersonType.LEGAL_PERSON
                ),
                705 to LawyerForm(
                    705, "Podnik nebo hospodářské zařízení sdružení", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                706 to LawyerForm(
                    706, "Spolek", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                707 to LawyerForm(
                    707, "Odborová organizace", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2017, 11, 1)
                ),
                708 to LawyerForm(
                    708, "Organizace zaměstnavatelů", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2017, 11, 1)
                ),
                711 to LawyerForm(711, "Politická strana, politické hnutí", PersonType.LEGAL_PERSON),
                715 to LawyerForm(
                    715, "Podnik nebo hospodářské zařízení politické strany", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2012, 6, 30)
                ),
                721 to LawyerForm(721, "Církve a náboženské společnosti", PersonType.LEGAL_PERSON),
                722 to LawyerForm(722, "Evidované církevní právnické osoby", PersonType.LEGAL_PERSON),
                723 to LawyerForm(723, "Svazy církví a náboženských společností", PersonType.LEGAL_PERSON),
                731 to LawyerForm(
                    731, "Organizační jednotka občanského sdružení", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2013, 12, 31)
                ),
                733 to LawyerForm(
                    733, "Pobočná odborová organizace a organizace zaměstnavatelů", PersonType.LEGAL_PERSON,
                    validTo = LocalDate.of(2017, 12, 31)
                ),
                734 to LawyerForm(
                    734,
                    "Organizační jednotka zvláštní organizace pro zastoupení českých zájmů v mezinárodních nevládních organizacích",
                    PersonType.LEGAL_PERSON
                ),
                736 to LawyerForm(
                    736, "Pobočný spolek", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                741 to LawyerForm(741, "Samosprávná stavovská organizace (profesní komora)", PersonType.LEGAL_PERSON),
                745 to LawyerForm(745, "Komora (hospodářská, agrární)", PersonType.LEGAL_PERSON),
                751 to LawyerForm(751, "Zájmové sdružení právnických osob", PersonType.LEGAL_PERSON),
                761 to LawyerForm(761, "Honební společenstvo", PersonType.LEGAL_PERSON),
                771 to LawyerForm(771, "Dobrovolný svazek obcí", PersonType.LEGAL_PERSON),
                801 to LawyerForm(801, "Obec", PersonType.LEGAL_PERSON),
                804 to LawyerForm(804, "Kraj", PersonType.LEGAL_PERSON),
                805 to LawyerForm(805, "Regionální rada regionu soudržnosti", PersonType.LEGAL_PERSON),
                811 to LawyerForm(811, "Městská část, městský obvod", PersonType.LEGAL_PERSON),
                906 to LawyerForm(
                    906, "Zahraniční spolek", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                907 to LawyerForm(
                    907, "Mezinárodní odborová organizace", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                908 to LawyerForm(
                    908, "Mezinárodní organizace zaměstnavatelů", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                921 to LawyerForm(921, "Mezinárodní nevládní organizace", PersonType.LEGAL_PERSON),
                922 to LawyerForm(922, "Organizační jednotka mezinárodní nevládní organizace", PersonType.LEGAL_PERSON),
                931 to LawyerForm(931, "Evropské hospodářské zájmové sdružení", PersonType.LEGAL_PERSON),
                932 to LawyerForm(932, "Evropská společnost", PersonType.LEGAL_PERSON),
                933 to LawyerForm(933, "Evropská družstevní společnost", PersonType.LEGAL_PERSON),
                936 to LawyerForm(
                    936, "Zahraniční pobočný spolek", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1)
                ),
                937 to LawyerForm(
                    937, "Pobočná mezinárodní odborová organizace", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1),
                    validTo = LocalDate.of(2017, 12, 31)
                ),
                938 to LawyerForm(
                    938, "Pobočná mezinárodní organizace zaměstnavatelů", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2014, 1, 1),
                    validTo = LocalDate.of(2017, 12, 31)
                ),
                941 to LawyerForm(941, "Evropské seskupení pro územní spolupráci", PersonType.LEGAL_PERSON),
                951 to LawyerForm(
                    951,
                    "Mezinárodní vojenská organizace vzniklá na základě mezinárodní smlouvy",
                    PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2018, 1, 1),
                ),
                952 to LawyerForm(
                    952, "Konsorcium evropské výzkumné infrastruktury", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2020, 1, 1),
                ),
                960 to LawyerForm(
                    960,
                    "Právnická osoba zřízená zvláštním zákonem zapisovaná do veřejného rejstříku",
                    PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2016, 6, 1),
                ),
                961 to LawyerForm(
                    961, "Svěřenský fond", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2018, 1, 1),
                ),
                962 to LawyerForm(
                    962, "Zahraniční svěřenský fond", PersonType.LEGAL_PERSON,
                    validFrom = LocalDate.of(2018, 1, 1),
                )
            )
        }
    }
}
