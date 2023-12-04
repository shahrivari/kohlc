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
    val openTs = IntArrayList()
    val closeTs = IntArrayList()
    val volumes = DoubleArrayList()
    private var lastCandle: CandleStick? = null


    companion object {
        const val MIN_CANDLE_SPAN = 60
    }

    private fun Int.roundToCandle(): Int = (this / MIN_CANDLE_SPAN) * MIN_CANDLE_SPAN

    fun size(): Int = timestamps.size()

    @Synchronized
    fun addNewTrade(price: Double, volume: Double, timestamp: Int) {
        if (lastCandle == null) { // The first candle in the history
            addNewCandle(price, volume, timestamp)
        } else if (timestamp >= lastCandle!!.timestamp) { // trade is in the future
            val candle = requireNotNull(lastCandle)
            if (timestamp - candle.timestamp >= MIN_CANDLE_SPAN) { // trade is in the current last candle
                addNewCandle(price, volume, timestamp)
            } else { // trade is in the future and the candle is not closed
                if (timestamp > candle.closeTimeStamp) {
                    candle.close = price
                    candle.closeTimeStamp = timestamp
                }
                if (timestamp < candle.openTimeStamp) {
                    candle.open = price
                    candle.openTimeStamp = timestamp
                }
                candle.volume += volume
                if (price > candle.high) candle.high = price
                if (price < candle.low) candle.low = price
            }
        } else { // trade is in the past
            var idx = timestamps.buffer.binarySearch(timestamp.roundToCandle(), 0, timestamps.elementsCount)
            if (idx >= 0) { // trade is in the past and the candle is available
                if (timestamp > closeTs[idx]) {
                    closes[idx] = price
                    closeTs[idx] = timestamp
                }
                if (timestamp < openTs[idx]) {
                    opens[idx] = price
                    openTs[idx] = timestamp
                }
                volumes[idx] += volume
                if (price > highs[idx]) highs[idx] = price
                if (price < lows[idx]) lows[idx] = price
            } else { // a new candle should be added in the past
                idx = -idx - 1
                timestamps.insert(idx, timestamp.roundToCandle())
                openTs.insert(idx, timestamp)
                closeTs.insert(idx, timestamp)
                opens.insert(idx, price)
                closes.insert(idx, price)
                highs.insert(idx, price)
                lows.insert(idx, price)
                volumes.insert(idx, volume)
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
            openTs.add(it.openTimeStamp)
            closeTs.add(it.closeTimeStamp)
        }
        lastCandle = CandleStick(open, open, open, open, volume, timestamp.roundToCandle(), timestamp, timestamp)
    }

    fun getFirstCandleBefore(timestamp: Int): CandleStick? {
        if (size() == 0) return null
        var idx = timestamps.buffer.binarySearch(timestamp, 0, timestamps.elementsCount)
        if (idx < 0) idx = -idx - 1
        return getCandleAt(idx)
    }

    fun getCandles() = toList()

    fun getLastCandles(n:Int) = toList().takeLast(n)

    fun getCandleAt(i: Int) =
        CandleStick(opens[i], highs[i], lows[i], closes[i], volumes[i], timestamps[i], openTs[i], closeTs[i], i)

    override fun iterator(): Iterator<CandleStick> {
        return object : Iterator<CandleStick> {
            var i = 0
            override fun hasNext(): Boolean = size() > 0 && i <= opens.size()
            override fun next(): CandleStick {
                if (size() == 0) throw NoSuchElementException()
                val candle = if (i == opens.size())
                    lastCandle!!.copy()
                else
                    getCandleAt(i)
                i++
                return candle
            }
        }
    }
}

