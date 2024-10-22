package com.kulishov.tracker

import org.junit.Assert.*

import org.junit.Test

class DateAndTimeSTest {

    @Test
    fun convertUnixTimeToDate() {
    }

    @Test
    fun convertUnixTimeToDate12() {
        val datares = DateAndTimeS(2024,9,20,18,49,42)
        var datastart = DateAndTimeS(0,0,0,0,0,0)
        val millis = 1726858182L
        datastart.convertUnixTimeToDate1(millis)
        assertEquals(datastart.day, datares.day)

    }
}