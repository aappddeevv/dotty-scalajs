package test1

import scala.scalajs.js
// no import js.|
import js.annotation._

type XX = Int | Unit
type XXN = Int | Unit | Null
type XXNA = js.Array[XXN]
type ReactNodePrimitive = Int|Long|Float|Double|String
type ReactNodeUndefined = js.UndefOr[Int]|js.UndefOr[Long]|js.UndefOr[Float]|js.UndefOr[Double]|js.UndefOr[String]
type ReactNodeUndefinedNull = js.UndefOr[Int|Null]|js.UndefOr[Long|Null]|js.UndefOr[Float|Null]|js.UndefOr[Double|Null]|js.UndefOr[String|Null]
type ReactNonClassType = Null|Unit
type ReactClassType = ReactNodePrimitive|ReactNodeUndefined|ReactNodeUndefinedNull
// something that can be rendered i.e. used in "children" position of react.createElement
type ReactNode = ReactClassType | ReactNonClassType
// something that can be rendered but is created from createElement
type ReactElement = ReactNode
type ReactFunction = js.Function1[js.Object|js.Dynamic, ReactElement]

type ReactFunctionComponent0 = js.Function0[ReactNode]
type ReactFunctionComponent1 = js.Function1[? <: js.Object, ReactNode]
type ReactFunctionComponent1WithRef = js.Function2[? <: js.Object, ? <: js.Any, ReactNode]
type ReactFunctionComponent = ReactFunctionComponent0 | ReactFunctionComponent1 | ReactFunctionComponent1WithRef
// imported components can, if desired, use this type to distinguish the import
trait ReactJSComponent extends js.Object

// a type that can be used in arg1 of createElement
type ReactType = ReactNodePrimitive |  ReactFunctionComponent | ReactJSComponent

val x1: XX = 10
val x2: XX = ()
val x3: XXN = null
val x4: XXN = 10
val x5: XXN = ()
val x6: XXNA = js.Array(10, (), null)
val foo1 = new Foo2 { }
val foo2 = new Foo2 { x = 10 }

def runtest() =
    println(s"x1=$x1")
    println(s"x2=$x2")
    println(s"x3=$x3")
    println(s"x4=$x4")
    println(s"x5=$x5")
    println(s"x6=${inspect(x6)}")
    println(s"foo1.x=${foo1.x}")
    println(s"foo2.x=${foo2.x}")
    println(s"foo2.y=${foo2.y}")

trait InspectOptions extends js.Object:
    val depth: js.UndefOr[Int | Null] = js.undefined

@js.native
@JSImport("util", "inspect")
def inspect(o: js.Any, options: js.UndefOr[InspectOptions] = ()): String = js.native

@js.native
@JSImport("react", JSImport.Namespace)
object react extends js.Object:
    def createElement(rt: ReactType, props: js.Object|js.Dynamic, children: ReactNode*): ReactElement = js.native

@js.native
trait Foo extends js.Object:
    var x: js.UndefOr[Int] = js.native

trait Foo2 extends js.Object:
    var x: js.UndefOr[Int] = js.undefined
    var y: js.UndefOr[Int] = js.undefined
    // does not work
    //var y1: Int | Unit = js.undefined
    // does not work
    //var y2: Int | Unit = ()

class MyOptions(
    val sopt: js.UndefOr[String] = js.undefined,
    val iopt: js.UndefOr[Int] = js.undefined,
    val dopt: js.UndefOr[Double] = 100.0D
) extends js.Object

type RequestHandler[T] = js.Function1[T, Unit]

// this is just a test, there is no addHandlers in react
@js.native
@JSImport("react", JSImport.Namespace)
object react2 extends js.Object:
    def addHandlers(handlers: RequestHandler[?]): js.Array[RequestHandler[?]] = js.native
    // plain ? will not work here, so just use Any, its equivalent since UndefOr is covariant
    def addHandlers2(handlers: js.UndefOr[Any]*): js.Array[RequestHandler[?]] = js.native


@js.native
trait EffectArg extends js.Object

object EffectArg:
  @inline def convertEffectCallbackArg[A](arg: () => (() => A)): js.Any = { () =>
    val rthunk = arg()
    js.Any.fromFunction0 { () => rthunk(); () }
  }: js.Function0[js.Function0[Unit]]

  @inline implicit def fromThunkJS[U](f: js.Function0[U]): EffectArg =
    js.Any.fromFunction0[Unit] { () => f(); () }.asInstanceOf[EffectArg]

  @inline implicit def fromThunk[U](f: () => U): EffectArg =
    js.Any.fromFunction0[Unit] { () => f(); () }.asInstanceOf[EffectArg]

  @inline implicit def fromThunkCbA[A](f: () => (() => A)): EffectArg =
    convertEffectCallbackArg(f).asInstanceOf[EffectArg]

  @inline implicit def fromThunkCbJS[A](f: () => js.Function0[A]): EffectArg =
    ((() => { val rthunk = f(); js.Any.fromFunction0 { () => rthunk(); () } }): js.Function0[js.Function0[Unit]])
      .asInstanceOf[EffectArg]

def useEffect(deps: Any*)(eff: EffectArg) = null