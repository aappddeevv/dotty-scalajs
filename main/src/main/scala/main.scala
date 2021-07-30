package test1

import scala.scalajs.js
import js.annotation._
import concurrent._
import concurrent.ExecutionContext.Implicits.global
import cps._

type Contextual[T] = Int ?=> T

/** Use new contextual abstractions */
def doit(i: Int): Contextual[Int] =
    val x = summon[Int]
    i + x

/** Easy extension syntax. */
extension [A](a: A)
    def asDyn = a.asInstanceOf[js.Dynamic]

/** Typeclass */
trait HasValue[F[_]]:
    def get[T](x: F[T]): T

/** New typeclass syntax. */
given HasValue[List]:
    def get[T](x: List[T]) = x(0)
     
/** Summon instance from context bounds. */
def tryF[F[_]: HasValue](i: F[Int]) = summon[HasValue[F]].get[Int](i) + 1

/** New union type. */
type MyCombo = Int | Unit

type With1[X] = Int | List[X]

type Bar = With1[?]

val noBracesVal =
    val y = 10
    val z = 20
    y + z


@main def app() =
    println("scalajs app")
    var z: Int|Unit = 10
    // Can't use MyCombo directly until next dotty release with bug fix (0.28)
    //var z: MyCombo = 10
    println(s"z (int): $z")
    z = ()
    println(s"z (undefined): $z")
    var z1 = z match
        case i:Int => i+10
        case _ => 1
    implicit val myint = 10
    val doitValue = doit(10)
    println(s"doit: $doitValue, should be 20")
    println(s"tryF: ${tryF(List(1,10,11))}")
    println(s"""testing truthiness
    true 1: ${js.DynamicImplicits.truthValue(true.asDyn)}
    true 2: ${js.DynamicImplicits.truthValue(1.asDyn)}
    false 3: ${js.DynamicImplicits.truthValue(false.asDyn)}
    false 4: ${js.DynamicImplicits.truthValue(null.asDyn)}
    true 5: ${js.DynamicImplicits.truthValue(js.Array().asDyn)}
    false 6: ${js.DynamicImplicits.truthValue("".asDyn)}
    false 7: ${js.DynamicImplicits.truthValue(js.undefined.asDyn)}
    false 8: ${js.DynamicImplicits.truthValue(().asDyn)}
    true 9: ${js.DynamicImplicits.truthValue("blah".asDyn)}
      """")
    
    //val effect = testSchedule *> testSchedule2
    //zio.Runtime.default.unsafeRunAsync_(effect)

    val ue = test1.useEffect(10){() => ()}    

    // constructor test
    val myopts = MyOptions(iopt=10, sopt="blah")
    println(s"myoptions: ${inspect(myopts)}")
    val f2 = new Foo2 { this.x = 10 }
    println(s"foo2: ${inspect(f2)}")

    // for-comprehension style
    for { 
        _ <- js.Promise.resolve(println("Testing fetch..."))
        _ <- fetchAndPrint("http://google.com")
        _ <- js.Promise.resolve(println("Forcing fetch error in flatMap after successful fetch..."))
        _ <- fetch("https://google.com")
                .tapValue(resp => println(s"Response status: ${resp.status}"))
                .flatMap(resp => js.Promise.reject(resp))
                .recoverWith:
                    case err: Any => 
                        js.Promise.resolve[Unit]:
                            println(s"Error encountered, handled with recoverWith: $err")
    } yield ()

    // for comprehension style
    val x = for { 
        r <- myFun().map(result => result + 10)
    } yield r
    x.tapValue(v => println(s"myFun() + 10: $v"))

    runtest()
    println("end - async funcs may print after this line!")


def promisfy(v: Int) = js.Promise.resolve[Int](v)

// cps style
def myFun() = async[js.Promise]:
    // ... here is possible to use await:
    val x = await(js.Promise.resolve[Int](1))
    val result = await(
        fetch("https://dotty.epfl.ch/versions/latest-nightly-base")
        .flatMap(r => r.text())
        .tapValue(t => println(s"scala compiler latest nightly base: $t")))
    val y = await(promisfy(10))
    val z = x + y + 1
    println(s"z=$z")
    z
