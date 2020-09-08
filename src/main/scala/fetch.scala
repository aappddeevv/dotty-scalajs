package test1

import scala.scalajs.js
import js.annotation._
import js.JSConverters._
import concurrent._
import concurrent.ExecutionContext.Implicits.global

@js.native
trait Response extends js.Object {
    val ok: Boolean = js.native
    def text(): js.Promise[String] = js.native
}

// This seems to given an error but should not, error reported
// @js.native
// @JSImport("node-fetch", JSImport.Default)
//def fetch2(url: String, arg: js.Dynamic): js.Promise[Response] = js.native

// This seems to compile ok, traditional way.
@js.native
@JSImport("node-fetch", JSImport.Default)
object fetch extends js.Any:
    def apply(url: String, options: js.UndefOr[js.Object|js.Dynamic]=js.undefined): js.Promise[Response] = js.native

def fetchAndPrint(url: String) =
    fetch(url).toFuture
    .flatMap:
        response =>
            if(!response.ok) Future.failed(new Exception("Not 200"))
            else response.text().toFuture    
    .recover:
        case ex => println(s"Fetch error: ${ex}")
    .foreach(println)