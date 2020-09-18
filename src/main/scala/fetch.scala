package test1

import scala.scalajs.js
import js.annotation._
import js.JSConverters._
import concurrent._
import concurrent.ExecutionContext.Implicits.global

@js.native
trait Response extends js.Object:
    val ok: Boolean = js.native
    def text(): js.Promise[String] = js.native

// This does not compile, yet...
// @js.native
// @JSImport("node-fetch", JSImport.Default)
// def fetch2(url: String, arg: js.UndefOr[js.Dynamic]=js.undefined): js.Promise[Response] = js.native

// Traditional way.
@js.native
@JSImport("node-fetch", JSImport.Default)
object fetch extends js.Any:
    def apply(url: String, options: js.UndefOr[js.Dynamic]=js.undefined): js.Promise[Response] = js.native

def fetchAndPrint(url: String) =
    fetch(url).toFuture
    .flatMap:
        response =>
            if(!response.ok) Future.failed(new Exception("Not 200"))
            else response.text().toFuture
    .recover:
        case ex => println(s"Fetch error: ${ex}"); "fetch error"
    .foreach(text => println(text.take(100) + "..."))