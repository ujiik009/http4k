import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.CorsPolicy
import org.http4k.filter.CorsPolicy.Companion.UnsafeGlobalPermissive
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.io.File
import java.io.FileInputStream


fun main() {

//    val app: HttpHandler = routeFunction()

    val handler = { _: Request -> Response(OK) }

    val myFilter = Filter { next: HttpHandler ->
        { request: Request ->
            val start = System.currentTimeMillis()
            val response = next(request)
            val latency = System.currentTimeMillis() - start
            println("I took $latency ms")
            response
        }
    }

    val routingHttpHandler = routes(
        "/download" bind routes(
            "/file" bind Method.POST to { request: Request ->
                val initialFile = File("pic.jpg")
                val targetStream = FileInputStream(initialFile)
                Response(OK).body(targetStream,initialFile.length())
            }
        ),

        "page" bind routes(
            "1" bind Method.GET to lamda@{
                Response(OK).body("PAGE 1")
            },
            "2" bind Method.GET to lamda@{
                Response(OK).body("PAGE 2")
            }
        )
    )

    val latencyAndBasicAuth: HttpHandler = ServerFilters.Cors(UnsafeGlobalPermissive).then(myFilter).then(routingHttpHandler)

    latencyAndBasicAuth.asServer(Jetty(9000)).start()

}
