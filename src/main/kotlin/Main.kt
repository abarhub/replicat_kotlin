package org.testspringboot3

import io.javalin.Javalin
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import mu.KotlinLogging
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import java.nio.file.Files as Files2

private val logger = KotlinLogging.logger {}

@Serializable
data class ListFiles(val filename: String, val size: Int)

@Serializable
data class Files(val filename: String, val size: Long, val hash: String, val type:String)

@Serializable
data class ListFiles2(val liste: List<Files>, val code: String)

data class Config(val rep: String)

private val REQUEST_READ_EXTERNAL_STORAGE = 1

fun main() {
    println("Hello World!")
    val config= getConfig()
    var app = Javalin.create(/*config*/)
        .get("/") { ctx -> ctx.result("Hello World!") }
        .post("/request1") { ctx ->
            run {
                val body = ctx.body()
                //val tmp=Json.decodeFromString<ListFiles>(body)
                //logger.info(tmp.filename)
                logger.info("request1 is $body")
                if (body != null && body.contains("=")) {
                    val s = body.substring(body.indexOf("=") + 1)
//                val s3= URLDecoder.decode(s, "UTF-8")
//                val s2=Json.decodeFromString<ListFiles>(s3)
//                logger.info("s2 is $s2")
                    val s4 = ctx.formParam("data")
                    if (s4 != null) {
                        val s2 = Json.decodeFromString<ListFiles>(s4)
                        logger.info("s2 is $s2")
                    }
                    logger.info("s4 is $s4")
                }
                ctx.result("Hello World2!")
            }
        }
        .post("/request2") { ctx ->
            run {

                val body = ctx.body()
                logger.info("request2 is $body")

                if (body != null && body.contains("=")) {
                    val s = body.substring(body.indexOf("=") + 1)

                    val s4 = ctx.formParam("listFiles")
                    if (s4 != null) {
                        val s2 = Json.decodeFromString<ListFiles2>(s4)
                        logger.info("s2 is $s2")
                    }
                }

            }
        }
        .post("/request3") { ctx ->
            run {
                val body = ctx.body()
                logger.info("request3 is $body")
                var res=""

                if (body != null && body.contains("=")) {
                    val s = body.substring(body.indexOf("=") + 1)

                    val s4 = ctx.formParam("data")
                    if (s4 != null) {
                        logger.info("s4 is '$s4'")
                        val s2 = Json.decodeFromString<ListFiles2>(s4)
                        logger.info("s2 is $s2")

                        val liste=ArrayList<Files>()

                        for(file in s2.liste){
                            val f= Paths.get(config.rep,file.filename)
                            if(Files2.exists(f)){
                                logger.info("$f is exists")
                            } else {
                                logger.info("$f is not exists")
                                liste.add(Files(file.filename, 0, "", ""))
                            }
                        }

                        val liste2=ListFiles2(liste,"")
                        val s5=Json.encodeToString(liste2)
                        res=s5
                    }
                }
                ctx.result(res)
            }
        }
        .post("/upload") { ctx ->
            run {
                val body = ctx.body()
                logger.info("upload is $body")
                var res = ""

                if (body != null && body.contains("=")) {
                    val s = body.substring(body.indexOf("=") + 1)

                    val s4 = ctx.formParam("file")
                    if (s4 != null) {

                        val s5 = ctx.formParam("filename")
                        if (s5 != null) {

                            val s04=Base64.getDecoder().decode(s4)

                            logger.info("file $s5 size: ${s4.length}")
                            val f=Paths.get(config.rep,s5)
                            Files2.write(f,s04)
                            logger.info("write $f")
                            res="OK"
                        }


                    }
                }

                ctx.result(res)
            }
        }

//        .get("/", ctx -> {
//            ctx.result("Hello World")
//        })
        .start(7070);
}

fun getConfig(): Config {
    val props = Properties()
    val f = Paths.get("data/config.properties")
    val input = f.toFile().inputStream()
    props.load(input)
    val rep = props.getProperty("rep", "");
    val config = Config(rep)
    return config
}

