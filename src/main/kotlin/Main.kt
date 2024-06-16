package org.testspringboot3

import io.javalin.Javalin
import io.javalin.http.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.IOException
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.nio.file.Files as Files2

private val logger = KotlinLogging.logger {}

@Serializable
data class ListFiles(val filename: String, val size: Int)

@Serializable
data class Files(val filename: String, val size: Long, val hash: String, val type: String)

@Serializable
data class ListFiles2(val liste: List<Files>, val code: String)

data class Config(val rep: String)

private val REQUEST_READ_EXTERNAL_STORAGE = 1

fun main() {
    println("Hello World!")
    val config = getConfig()
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
                var res = ""

                if (body != null && body.contains("=")) {
                    val s = body.substring(body.indexOf("=") + 1)

                    val s4 = ctx.formParam("data")
                    if (s4 != null) {
                        logger.info("s4 is '$s4'")
                        val s2 = Json.decodeFromString<ListFiles2>(s4)
                        logger.info("s2 is $s2")

                        val liste = ArrayList<Files>()

                        for (file in s2.liste) {
                            val f = Paths.get(config.rep, file.filename)
                            if (Files2.exists(f)) {
                                logger.info("$f is exists")
                            } else {
                                logger.info("$f is not exists")
                                liste.add(Files(file.filename, 0, "", ""))
                            }
                        }

                        val liste2 = ListFiles2(liste, "")
                        val s5 = Json.encodeToString(liste2)
                        res = s5
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

                            val s04 = Base64.getDecoder().decode(s4)

                            logger.info("file $s5 size: ${s4.length}")
                            val f = Paths.get(config.rep, s5)
                            Files2.write(f, s04)
                            logger.info("write $f")
                            res = "OK"
                        }


                    }
                }

                ctx.result(res)
            }
        }.post("/init") { ctx ->
            run {
                logger.info("init")
                val body = ctx.body()
                logger.info("upload is $body")
                val id = compteur.getAndIncrement()
                val gestion = GestionFichiers()
                gestion.id = id
                listeGestionFichiers.put(id, gestion)
                logger.info("creation de la session $id")
                ctx.result("$id")
            }
        }
        .post("/listeFichiers/{id}") { ctx ->
            run {
                val idStr = ctx.pathParam("id")
                logger.info("liste fichier $idStr ...")
                if (idStr != null && idStr.isNotEmpty()) {
                    val id = Integer.parseInt(idStr)
                    if (id > 0 && listeGestionFichiers.contains(id)) {
                        listeGestionFichiers.get(id)?.listeFichiers(ctx)
                        logger.info("listeFichiers $id OK")
                    } else {
                        logger.info("pas de traitement pour $id")
                    }
                }

            }
        }
        .post("/upload/{id}") { ctx ->
            run {
                val idStr = ctx.pathParam("id")
                logger.info("upload $idStr ...")
                if (idStr != null && idStr.isNotEmpty()) {
                    val id = Integer.parseInt(idStr)
                    if (id > 0 && listeGestionFichiers.contains(id)) {
                        listeGestionFichiers.get(id)?.upload(ctx)
                        logger.info("upload $id OK")
                    } else {
                        logger.info("pas de traitement pour $id")
                    }
                }
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

var compteur = AtomicInteger(1)
val listeGestionFichiers = ConcurrentHashMap<Int, GestionFichiers>();

class GestionFichiers {

    var id: Int = 0


    fun listeFichiers(ctx: Context) {
        val body = ctx.body()
        logger.info("request3 is $body")
        var res = ""

        if (body != null && body.contains("=")) {
            val s = body.substring(body.indexOf("=") + 1)

            val config = getConfig()

            val s4 = ctx.formParam("data")
            if (s4 != null) {
                logger.info("s4 is '$s4'")
                val s2 = Json.decodeFromString<ListFiles2>(s4)
                logger.info("s2 is $s2")

                val liste = ArrayList<Files>()

                for (file in s2.liste) {
                    val f = Paths.get(config.rep, file.filename)
                    if (Files2.exists(f)) {
                        logger.info("$f is exists")
                        if(Files2.isDirectory(f)){
                            throw IOException("$f is a directory")
                        }
                        val contenu=Files2.readAllBytes(f)
                        val hash=hashString(contenu,"SHA-256").toHex();
                        if(hash == s4) {
                            logger.info("$f est identique")
                        } else {
                            logger.info("$f est different => on l'importe")
                            liste.add(Files(file.filename, 0, "", "F"))
                        }
                    } else {
                        logger.info("$f is not exists")
                        liste.add(Files(file.filename, 0, "", "F"))
                    }
                }

                val liste2 = ListFiles2(liste, "")
                val s5 = Json.encodeToString(liste2)
                res = s5
            }
        }
        ctx.result(res)
    }


    fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    fun hashString(str: ByteArray, algorithm: String): ByteArray =
        MessageDigest.getInstance(algorithm).digest(str)

    fun upload(ctx: Context) {
        val body = ctx.body()
        logger.info("upload is $body")
        var res = "KO"

        if (body.contains("=")) {
            val config = getConfig()

            val s4 = ctx.formParam("file")
            if (s4 != null) {

                val s5 = ctx.formParam("filename")
                if (s5 != null) {

                    val s04 = Base64.getDecoder().decode(s4)

                    logger.info("file $s5 size: ${s4.length}")
                    val f = Paths.get(config.rep, s5)
                    if(Files2.notExists(f.parent)) {
                        logger.info("création du répertoire ${f.parent}")
                        Files2.createDirectories(f.parent)
                    }
                    Files2.write(f, s04)
                    logger.info("write $f")
                    res = "OK"
                }


            }
        }

        ctx.result(res)
    }
}

