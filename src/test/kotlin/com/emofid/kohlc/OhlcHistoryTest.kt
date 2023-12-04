package com.emofid.kohlc

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class OhlcHistoryTest {

    @Test
    fun `adding new trades should work`() {
        val ohlc = OhlcHistory("test")
        ohlc.addNewTrade(1.0, 1.0, 1)
        ohlc.addNewTrade(2.0, 1.0, 2)
        ohlc.addNewTrade(3.0, 1.0, 3)
        ohlc.addNewTrade(4.0, 1.0, 4)
        ohlc.addNewTrade(1.0, 1.0, 100)
        ohlc.addNewTrade(2.0, 1.0, 101)
        ohlc.addNewTrade(3.0, 1.0, 102)
        ohlc.addNewTrade(4.0, 1.0, 103)
        Assertions.assertThat(ohlc.getCandles()).containsExactly(
            CandleStick(1.0, 4.0, 1.0, 4.0, 4.0, 0, 1, 4),
            CandleStick(1.0, 4.0, 1.0, 4.0, 4.0, 60, 100, 103),
        )
    }

    @Test
    fun `adding trade in past should be flawless`() {
        val ohlc = OhlcHistory("test")
        ohlc.addNewTrade(1.0, 1.0, 100)
        ohlc.addNewTrade(3.0, 3.0, 300)
        ohlc.addNewTrade(2.0, 2.0, 200)
        Assertions.assertThat(ohlc.getCandles()).containsExactly(
            CandleStick(1.0, 1.0, 1.0, 1.0, 1.0, 60, 100, 100),
            CandleStick(2.0, 2.0, 2.0, 2.0, 2.0, 180, 200, 200),
            CandleStick(3.0, 3.0, 3.0, 3.0, 3.0, 300, 300, 300),
        )

    }

}