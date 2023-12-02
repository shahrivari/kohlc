package com.emofid.kohlc

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.measureTime

fun main() {
    println("Reading...")
    val ohlcTable = OhlcTable()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    File("/tmp/ohlc.csv").forEachLine { line ->
        val parts = line.split(",")
        val symbol = parts[0]
        val close = parts[1].toDouble()
        val open = parts[2].toDouble()
        val low = parts[3].toDouble()
        val high = parts[4].toDouble()
        val volume = parts[5].toDouble()
        val date = LocalDateTime.parse(parts[6], formatter)
        ohlcTable.addNewTrade(symbol, open, volume / 4, date.toEpochSecond(ZONE_OFFSET).toInt())
        ohlcTable.addNewTrade(symbol, high, volume / 4, date.toEpochSecond(ZONE_OFFSET).toInt())
        ohlcTable.addNewTrade(symbol, low, volume / 4, date.toEpochSecond(ZONE_OFFSET).toInt())
        ohlcTable.addNewTrade(symbol, close, volume / 4, date.toEpochSecond(ZONE_OFFSET).toInt())
    }

    val ohlcHistory = ohlcTable.table.maxByOrNull { it.value.size() }!!.value
    println("Finding...")
    val time = measureTime {
        for (i in 0..100000) {
            ohlcHistory.getFirstCandleBefore(LocalDateTime.now().toEpochSecond())
        }
    }
    println(time)
    ohlcHistory.addNewTrade(24.0, 1.0, (System.currentTimeMillis() / 1000).toInt())

}