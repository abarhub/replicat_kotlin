package org.testspringboot3

import io.javalin.Javalin

fun main() {
    println("Hello World!")
    var app = Javalin.create(/*config*/)
        .get("/") { ctx -> ctx.result("Hello World!") }
//        .get("/", ctx -> {
//            ctx.result("Hello World")
//        })
    .start(7070);
}