package com.emofid.kohlc

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

class OhlcTableTest {
    @Test
    fun `adding the same trades in random order and sorted order should be equal`() {
        fun parse(file: String): OhlcTable {
            return OhlcTable.parse(GZIPInputStream(OhlcTableTest::class.java.getResourceAsStream(file)))
        }

        val sorted = parse("/sorted.csv.gz")
        val random = parse("/random.csv.gz")
        sorted.table.forEach { (key, value) ->
            Assertions.assertThat(value.getCandles()).isEqualTo(random.table[key]!!.getCandles())
        }

    }


}