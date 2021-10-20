package cz.loono.backend.data

import org.junit.jupiter.api.Test

class HealthcareCSVParserTest {

    private val healthcareCSVParser = HealthcareCSVParser()

    @Test
    fun `empty input to parse`() {
        val emptyInputStream = "".byteInputStream()

        val providers = healthcareCSVParser.parse(emptyInputStream)

        assert(providers.isEmpty())
    }

    @Test
    fun `header is OK and not stored`() {
        val header = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
            """
            .trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(header)

        assert(providers.isEmpty())
    }

    @Test
    fun `header is NOT OK`() {
        val header = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice
            """
            .trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(header)

        assert(providers.isEmpty())
    }

    @Test
    fun `tests invalid line columns`() {
        val input = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
239530,09856030000000,"Ibáček.cz, s.r.o., Lékárna",Lékárna,Plzeň,30100,Husova,1509/18,Plzeňský kraj,CZ032,Plzeň-město,CZ0323,,,,,,09856030,2,112,CZ032,CZ0323,Plzeň,31200,Moravská,854/2,,,lékárenská péče,LENKA KREJČOVÁ,49.745640102442,13.369817991301
            """
            .trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(input)

        assert(providers.isEmpty())
    }

    @Test
    fun `tests more columns in line`() {
        val input = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
239530,09856030000000,09856030000000,09856030000000,"Ibáček.cz, s.r.o., Lékárna",Lékárna,Plzeň,30100,Husova,1509/18,Plzeňský kraj,CZ032,Plzeň-město,CZ0323,,,,,,09856030,2,112,CZ032,CZ0323,Plzeň,31200,Moravská,854/2,,,lékárenská péče,LENKA KREJČOVÁ,49.745640102442,13.369817991301
            """
            .trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(input)

        assert(providers.isEmpty())
    }

    @Test
    fun `happy case with single record`() {
        val input = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
239519,161084,03685080000000,Mgr. DOMINIKA MACHTOVÁ,Samostatné zařízení fyzioterapeuta,Husinec,38421,Prokopovo náměstí,1,Jihočeský kraj,CZ031,Prachatice,CZ0315,,,,,,03685080,1,,CZ031,CZ0315,Husinec,38421,Prokopovo náměstí,1,Fyzioterapeut,ambulantní péče,,,49.053069339386,13.986452702303
        """.trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(input)

        assert(providers.size == 1)
        println(providers[0].toString())
        assert(providers[0].toString() == "239519,161084,03685080000000,Mgr. DOMINIKA MACHTOVÁ,Samostatné zařízení fyzioterapeuta,Husinec,38421,Prokopovo náměstí,1,Jihočeský kraj,CZ031,Prachatice,CZ0315,,,,,,03685080,1,,,,Jihočeský kraj,CZ031,Prachatice,CZ0315,Husinec,38421,Prokopovo náměstí,1,Fyzioterapeut,[HealthcareCategory(id=0, value=Fyzioterapie, healthcareProviders=[])],ambulantní péče,,,49.053069339386,13.986452702303")
    }

    @Test
    fun `record with comma`() {
        val input = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
239530,161093,09856030000000,"Ibacek.cz, s.r.o., Lekarna",Lékárna,Plzeň,30100,Husova,1509/18,Plzeňský kraj,CZ032,Plzeň-město,CZ0323,,,,,,09856030,2,112,CZ032,CZ0323,Plzeň,31200,Moravská,854/2,,,lékárenská péče,LENKA KREJČOVÁ,49.745640102442,13.369817991301
        """.trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(input)

        assert(providers.size == 1)
        assert(providers[0].title == "Ibacek.cz, s.r.o., Lekarna")
    }

    @Test
    fun `record with quotes`() {
        val input = """
MistoPoskytovaniId,ZdravotnickeZarizeniId,Kod,NazevZarizeni,DruhZarizeni,Obec,Psc,Ulice,CisloDomovniOrientacni,Kraj,KrajCode,Okres,OkresCode,SpravniObvod,PoskytovatelTelefon,PoskytovatelFax,PoskytovatelEmail,PoskytovatelWeb,Ico,TypOsoby,PravniFormaKod,KrajCodeSidlo,OkresCodeSidlo,ObecSidlo,PscSidlo,UliceSidlo,CisloDomovniOrientacniSidlo,OborPece,FormaPece,DruhPece,OdbornyZastupce,Lat,Lng
239177,160302,70829560004000,""${'"'}HVEZDA z.u.""${'"'},Domácí zdravotní péče,Praha 10,10100,Na Královce,508/3,Hlavní město Praha,CZ010,Hlavní město Praha,CZ0100,,+420606722666,,sekretariat@hvezdazu.cz,http://www.hvezdazu.cz,70829560,2,161,CZ072,CZ0724,Zlín,76302,Masarykova,443,Všeobecná sestra,zdrav. péče poskytovaná ve vlastním soc. prostředí pacienta - domácí péče - ošetřovatelská,ošetřovatelská péče,Miroslava Kalivodová,50.069775936268,14.448871816298
        """.trimIndent().byteInputStream()

        val providers = healthcareCSVParser.parse(input)

        assert(providers.size == 1)
        assert(providers[0].title == "\"HVEZDA z.u.\"")
    }
}
