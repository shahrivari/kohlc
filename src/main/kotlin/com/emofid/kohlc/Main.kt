package com.emofid.kohlc

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.javalin.json.JavalinJackson
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.zip.GZIPInputStream

fun main(args: Array<String>) {
    val ohlcTable = OhlcTable.parse(GZIPInputStream(OhlcTable::class.java.getResourceAsStream("/sorted.csv.gz")))
    val app = Javalin.create { conf ->
        conf.jsonMapper(JavalinJackson().updateMapper { mapper ->
            mapper.registerModule(KotlinModule.Builder().build())
            mapper.registerModule(Jdk8Module())
            mapper.registerModule(JavaTimeModule())
            mapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        })
    }.get("/history/{id}") { ctx ->
        val id = ctx.pathParam("id")
        val from = ctx.queryParam("from")
        val to = ctx.queryParam("to")
        val history = ohlcTable.getHistory(id)
        if (history == null) {
            ctx.status(HttpStatus.NOT_FOUND)
            ctx.result()
        } else {
            ctx.json(history.getLastCandles(100))
        }
    }.exception(Exception::class.java) { e, ctx ->
        ctx.status(500)
        ctx.result(e.message ?: "Unknown error")
    }.start(8000)

}