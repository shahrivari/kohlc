package com.emofid.kohlc

import com.carrotsearch.hppc.DoubleArrayList
import com.carrotsearch.hppc.IntArrayList
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

val ZONE_OFFSET: ZoneOffset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

fun LocalDateTime.toEpochSecond(): Int = this.toEpochSecond(ZONE_OFFSET).toInt()

class OhlcHistory(val name: String) : Iterable<CandleStick> {
    val opens = DoubleArrayList()
    val closes = DoubleArrayList()
    val highs = DoubleArrayList()
    val lows = DoubleArrayList()
    val timestamps = IntArrayList()
    val volumes = DoubleArrayList()
    private var lastCandle: CandleStick? = null


    companion object {
        const val MIN_CANDLE_SPAN = 60
    }

    private fun Int.roundToCandle(): Int = (this / MIN_CANDLE_SPAN) * MIN_CANDLE_SPAN

    fun size(): Int = timestamps.size()

    @Synchronized
    fun addNewTrade(price: Double, volume: Double, timestamp: Int) {
        if (lastCandle == null) {
            addNewCandle(price, volume, timestamp)
        } else {
            require(timestamp >= lastCandle!!.timestamp) { "Timestamp is less than last candle timestamp" }
            val candle = requireNotNull(lastCandle)
            if (timestamp - candle.timestamp >= MIN_CANDLE_SPAN) {
                addNewCandle(price, volume, timestamp)
            } else {
                candle.close = price
                candle.volume += volume
                if (price > candle.high) candle.high = price
                if (price < candle.low) candle.low = price
            }
        }
    }

    private fun addNewCandle(open: Double, volume: Double, timestamp: Int) {
        lastCandle?.let {
            opens.add(it.open)
            closes.add(it.close)
            highs.add(it.high)
            lows.add(it.low)
            volumes.add(it.volume)
            timestamps.add(it.timestamp)
        }
        lastCandle = CandleStick(open, open, open, open, volume, timestamp.roundToCandle())
    }

    fun getFirstCandleBefore(timestamp: Int): CandleStick? {
        if (size() == 0) return null
        var pos = timestamps.buffer.binarySearch(timestamp, 0, timestamps.elementsCount)
        if (pos < 0) pos = -pos - 1
        return CandleStick(opens[pos], highs[pos], lows[pos], closes[pos], volumes[pos], timestamps[pos], pos)
    }

    fun getCandles() = toList()

    override fun iterator(): Iterator<CandleStick> {
        return object : Iterator<CandleStick> {
            var i = 0
            override fun hasNext(): Boolean = size() > 0 && i <= opens.size()
            override fun next(): CandleStick {
                if (size() == 0) throw NoSuchElementException()
                val candle = if (i == opens.size())
                    lastCandle!!.copy()
                else
                    CandleStick(opens[i], highs[i], lows[i], closes[i], volumes[i], timestamps[i], i)
                i++
                return candle
            }
        }
    }
}

class OhlcTable {
    val table = HashMap<String, OhlcHistory>()
    fun addNewTrade(symbol: String, price: Double, volume: Double, timestamp: Int) {
        table.getOrPut(symbol) { OhlcHistory(symbol) }.addNewTrade(price, volume, timestamp)
    }
}

