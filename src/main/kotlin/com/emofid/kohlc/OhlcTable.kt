package com.emofid.kohlc

import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

class OhlcTable {
    val table = HashMap<String, OhlcHistory>()

    companion object {
        fun parse(stream:InputStream): OhlcTable {
            val ohlcTable = OhlcTable()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            stream.bufferedReader().lines().forEach { line ->
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
            return ohlcTable
        }

    }
    fun addNewTrade(symbol: String, price: Double, volume: Double, timestamp: Int) {
        table.getOrPut(symbol) { OhlcHistory(symbol) }.addNewTrade(price, volume, timestamp)
    }

    fun getHistory(id: String): OhlcHistory? = table[id]
}