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
    val status: Int = js.native

// traditional way until 0.28/3.0+
// @js.native
// @JSImport("node-fetch", JSImport.Default)
// object fetch extends js.Object:
//     def apply(url: String, options: js.UndefOr[js.Dynamic]=js.undefined): js.Promise[Response] = js.native

@js.native
@JSImport("node-fetch", JSImport.Default)
def fetch(url: String, options: js.UndefOr[js.Dynamic]=js.undefined): js.Promise[Response] = js.native

// use all native javascript promises, could convert to scala's Future
def fetchAndPrint(url: String) =
    fetch(url)
    .flatMap:
        response =>
            if(!response.ok) js.Promise.reject(new Exception("Not 200"))
            else response.text()
    .recover:
        case ex => println(s"Fetch error: ${ex}"); "fetch error"
    .tapValue(text => println(s"fetch result text content: ${text.take(100)} ..."))