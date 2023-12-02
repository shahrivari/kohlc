package com.emofid.kohlc

import java.time.LocalDateTime

data class CandleStick(
    val open: Double,
    var high: Double,
    var low: Double,
    var close: Double,
    var volume: Double,
    val timestamp: Int,
    val index: Int? = null
) {


    override fun toString(): String {
        val ts: LocalDateTime = LocalDateTime.ofEpochSecond(timestamp.toLong(), 0, ZONE_OFFSET)
        return "CandleStick(open=$open, high=$high, low=$low, close=$close, volume=$volume, timestamp=$timestamp, ts=$ts)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CandleStick
        if (open != other.open) return false
        if (high != other.high) return false
        if (low != other.low) return false
        if (close != other.close) return false
        if (volume != other.volume) return false
        if (timestamp != other.timestamp) return false
        return true
    }

    override fun hashCode(): Int {
        var result = open.hashCode()
        result = 31 * result + high.hashCode()
        result = 31 * result + low.hashCode()
        result = 31 * result + close.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + timestamp
        return result
    }
}