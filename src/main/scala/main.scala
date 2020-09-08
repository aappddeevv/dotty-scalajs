package test1

import scala.scalajs.js
import js.annotation._
import concurrent._
import concurrent.ExecutionContext.Implicits.global

type Contextual[T] = Int ?=> T

/** Use new contextual abstractions */
def doit(i: Int): Contextual[Int] =
    val x = summon[Int]
    i + x

/** Easy extension syntax. */
extension [A](a: A)
    def asDyn = a.asInstanceOf[js.Dynamic]

/** Typesclass */
trait HasValue[F[_]]:
    def get[T](x: F[T]): T

/** New typeclass syntax. */
given HasValue[List]:
    def get[T](x: List[T]) = x(0)
     
/** Summon instance from context bounds. */
def tryF[F[_]: HasValue](i: F[Int]) = summon[HasValue[F]].get[Int](i) + 1

/** New union type. */
type MyCombo = Int | Unit

@main def app() =
    println("scalajs app")
    var z: Int|Unit = 10
    // Can't use MyCombo directlly until next dotty release with bug fix
    //var z: MyCombo = 10
    println(s"z (int): $z")
    z = ()
    println(s"z (undefinde): $z")
    var z1 = z match
        case i:Int => i+10
        case _ => 1
    implicit val myint = 10
    doit(10)
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
    fetchAndPrint("http://google.com")
    println("end")