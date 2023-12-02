package com.emofid.kohlc

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import java.text.SimpleDateFormat

fun main(args: Array<String>) {

    val app = Javalin.create { conf ->
        conf.jsonMapper(JavalinJackson().updateMapper { mapper ->
            mapper.registerModule(KotlinModule.Builder().build())
            mapper.registerModule(Jdk8Module())
            mapper.registerModule(JavaTimeModule())
            mapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        })
    }.get("/hi") { ctx ->
        ctx.json("Hello World!")
    }.exception(Exception::class.java) { e, ctx ->
        ctx.status(500)
        ctx.result(e.message ?: "Unknown error")
    }.start(8000)
}