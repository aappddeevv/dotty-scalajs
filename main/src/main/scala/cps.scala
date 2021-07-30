package test1

import cps._
import scala.scalajs.js
// does this override scala3's "|" ???
import js.|

trait JSPromiseMonad extends CpsMonad[js.Promise]:
   def pure[T](t:T):js.Promise[T] = js.Promise.resolve[T](t)
   def map[A,B](fa:js.Promise[A])(f: A=>B):js.Promise[B] = fa.map(f)
   def flatMap[A,B](fa:js.Promise[A])(f: A=>js.Promise[B]):js.Promise[B] = fa.flatMap(f)

given JSPromiseMonad as JSPromiseMonad

trait JSPromiseTryMonad extends CpsTryMonad[js.Promise] with JSPromiseMonad:
   def error[A](e: Throwable): js.Promise[A] = js.Promise.resolve(e.asInstanceOf[RVAL[A]])
   def restore[A](fa: js.Promise[A])(fx:Throwable => js.Promise[A]): js.Promise[A] =
        fa.recoverWith:
            case ex: Throwable => fx(ex)
            case ex as _ => fx(new js.JavaScriptException(ex))
   
given JSPromiseTryMonad as JSPromiseTryMonad

type RVAL[A] = A | js.Thenable[A]
type RESOLVE[A, B] = js.Function1[A, RVAL[B]]
type REJECTED[A] = js.Function1[scala.Any, RVAL[A]]

extension[A,B](self: js.Promise[A]):
    def jsThen(f: A => B|js.Thenable[B]): js.Promise[B] =
        val onf: RESOLVE[A,B] = f
        self.`then`[B](onf, js.undefined).asInstanceOf[js.Promise[B]]

    def map(f: A => B): js.Promise[B] = jsThen(f)

    def flatMap(f: A => js.Promise[B]): js.Promise[B] = jsThen(f)

    // /** Sequence computation, then replace final values with `b`. */
    def as(b: B): js.Promise[B] = map(_ => b)

    /** co-flatMap */
    def coFlatMap(onFulfilled: A => js.Thenable[B], onRejected: scala.Any => js.Thenable[B]) =
        val onf = js.Any.fromFunction1(onFulfilled).asInstanceOf[RESOLVE[A, B]]
        val onr = js.Any.fromFunction1(onRejected).asInstanceOf[REJECTED[B]]
        self.`then`[B](onf, js.defined(onr))

    def flatten(implicit ev: A <:< js.Thenable[B]): js.Promise[B] =
            val onf = js.Any.fromFunction1(ev).asInstanceOf[RESOLVE[A, B]]
            self.`then`[B](onf, js.undefined).asInstanceOf[js.Promise[B]]

    // /** Map over the resolved value or the error. */
    def transform(s: A => B, f: scala.Any => scala.Any): js.Promise[B] =
        val onf = js.Any.fromFunction1(s).asInstanceOf[RESOLVE[A, B]]
        val onr = js.Any.fromFunction1((e: Any) => js.Promise.reject(f(e))).asInstanceOf[REJECTED[B]]
        self.`then`[B](onf, onr).asInstanceOf[js.Promise[B]]

    /** Unlike `Future.foreach`, return an effect after running `f`. It's like `tapValue`. */
    def foreach(f: A => B) =
        self.`then`[B]((a: A) => f(a).asInstanceOf[RVAL[B]], js.undefined)


// all of these had def X[B :> A](...) = ???
extension[A,B >: A](self: js.Promise[A]):    
    /** Tap into the result. */
    def tapValueF(f: B => js.Thenable[Any]): js.Promise[B] =
        val onf =
            js.Any.fromFunction1((a: B) => f(a).`then`((_ => a): RESOLVE[Any, B], js.undefined)).asInstanceOf[RESOLVE[B,B]]
        self.`then`[B](onf, js.undefined).asInstanceOf[js.Promise[B]]

    /** co-map */
    def coMap(onFulfilled: A => B, onRejected: scala.Any => B) =
            val onf = js.Any.fromFunction1(onFulfilled).asInstanceOf[RESOLVE[A, B]]
            val onr = js.Any.fromFunction1(onRejected).asInstanceOf[REJECTED[B]]
            self.`then`[B](onf, onr)
    /** Recover from the error using a partial function. If the partial function is
     * defined at the value, then the function is applied. Otherwise the original
     * value or error remains. recover = catch.
     */
    def recover(pf: PartialFunction[scala.Any, B]): js.Promise[B] =
        val onf = ().asInstanceOf[RESOLVE[A,B]]
        val onr =
            js.Any.fromFunction1((any: Any) => if (pf.isDefinedAt(any)) pf.apply(any) else js.Promise.reject(any)).asInstanceOf[REJECTED[B]]
        self.`then`[B](onf, onr).asInstanceOf[js.Promise[B]]

    /** Like `recover` but the partial function returns a `js.Thenable`. recover = catch. */
    def recoverWith(pf: PartialFunction[scala.Any, js.Thenable[B]]): js.Promise[B] =
        val onf = ().asInstanceOf[RESOLVE[A, B]]
        val onr: REJECTED[B] = (any: Any) => if (pf.isDefinedAt(any)) pf.apply(any) else js.Promise.reject(any)
        self.`then`[B](onf, onr).asInstanceOf[js.Promise[B]]

    /** Return this if it succeeds otherwise return that. If that fails,
     * return failure from this.
     */
    def fallbackTo(that: js.Promise[B]): js.Promise[B] =
        if self == that then self.asInstanceOf[js.Promise[B]]
        else
            val onf: RESOLVE[A, B] = (a: A) => a
            val onr: REJECTED[B] = 
                (erra: scala.Any) =>
                    val onur: REJECTED[B] = (erru: scala.Any) => erra.asInstanceOf[RVAL[B]]
                    that.`then`((u: B) => u.asInstanceOf[RVAL[B]], js.defined(onur))
            self.`then`[B](onf, onr).asInstanceOf[js.Promise[B]]

    /** Return this value if successful, else that's value. */
    def orElse(that: => js.Thenable[B]) =
        val onf = js.Any.fromFunction1((a: A) => a.asInstanceOf[B]).asInstanceOf[RESOLVE[A, B]]
        val onr = js.Any.fromFunction1((e: Any) => that).asInstanceOf[REJECTED[B]]
        self.`then`[B](onf, onr)

// from my jshelpers lib
extension [A](self: js.Promise[A]): 
    /** Map on error. */
    def mapError(f: scala.Any => scala.Any) =
        val onf: REJECTED[A] = (err: scala.Any) => js.Promise.reject(f(err)).asInstanceOf[RVAL[A]]
        self.`then`[A]((), onf).asInstanceOf[js.Promise[A]]

    /** Map on error. Use the value in the effect and redirect to the error in self. */
    def flatMapError(f: scala.Any => js.Thenable[scala.Any]) =
        val onf: REJECTED[A] = (err: scala.Any) => f(err).asInstanceOf[RVAL[A]]
            //f(err).`then`(nerrIsPromise => nerrIsPromise.asInstanceOf[RVAL[A]], js.undefined).asInstanceOf[RVAL[A]]
            //f(err).`then`(nerrIsPromise => nerrIsPromise.asInstanceOf[RVAL[A]], js.undefined).asInstanceOf[RVAL[A]]
        self.`then`[A]((), onf).asInstanceOf[js.Promise[A]]

    // /** If this fails, push the error into the value position of the promise. */
    def failed: js.Promise[scala.Any] =
        val onf: REJECTED[scala.Any] = (err: scala.Any) => js.Promise.resolve[scala.Any](err)
        self.`then`[scala.Any]((), js.defined(onf)).asInstanceOf[js.Promise[scala.Any]]

    /** Tap into the result. */
    def tapValue(f: A => Any): js.Promise[A] =
        val onf: RESOLVE[A,A] = (a: A) => { f(a); a }
        self.`then`[A](onf, js.undefined).asInstanceOf[js.Promise[A]]

    /** Tap into the error. */
    def tapError(f: scala.Any => Any): js.Promise[A] =
        val onr: REJECTED[A] = (e: Any) => { f(e); js.Promise.reject(e) }
        self.`then`[A]((), onr).asInstanceOf[js.Promise[A]]

    def tapErrorF(f: scala.Any => js.Promise[Any]): js.Promise[A] =
        val onr: REJECTED[A] = (e: Any) => 
            f(e).`then`[A](().asInstanceOf[RESOLVE[Any,A]], js.defined({(_: Any) => js.Promise.reject(e)}:REJECTED[A]))
        self.`then`[A]((), onr).asInstanceOf[js.Promise[A]]

    /** Map the resolved value to unit. Otherwise the error falls through. */
    def unit =
        val onf = js.Any.fromFunction1((_: A) => ()).asInstanceOf[RESOLVE[A, Unit]]
        self.`then`[Unit](onf, js.undefined).asInstanceOf[js.Promise[Unit]]

    /** Filter on the value. Return failed Thenable with
     * NoSuchElementException if p => false.
     */
    def filter(p: A => Boolean): js.Promise[A] =
        val onf = js.Any.fromFunction1 { (a: A) =>
            val result = p(a)
            if (result) js.Promise.resolve[A](a)
            else js.Promise.reject(new NoSuchElementException()).asInstanceOf[js.Promise[A]]
        }.asInstanceOf[RESOLVE[A, A]]
        self.`then`[A](onf, js.undefined).asInstanceOf[js.Promise[A]]

    /** for-comprehension support. */
    def withFilter(p: A => Boolean) = filter(p)

    // /** Not sure this is semantically right... */
    // def transform[S](f: scala.util.Try[A] => scala.util.Try[S]): js.Promise[S] =
    //     val onf: RESOLVE[A, S] = (a: A) =>
    //         f(scala.util.Success(a)) match {
    //         case scala.util.Success(s) => js.Promise.resolve(s.asInstanceOf[RVAL[S]])
    //         case scala.util.Failure(t) => js.Promise.reject(t.asInstanceOf[RVAL[S]])
    //         }
    //     val onr: REJECTED[S] = (err: scala.Any) => {
    //         val trya = err match {
    //         case th: Throwable => scala.util.Failure(th)
    //         case _             => scala.util.Failure(js.JavaScriptException(err))
    //         }
    //         f(trya) match {
    //         case scala.util.Success(v)  => js.Promise.resolve(v.asInstanceOf[RVAL[S]])
    //         case scala.util.Failure(th) => js.Promise.reject(th.asInstanceOf[RVAL[S]])
    //         }
    //     }
    //     self.`then`[S](onf, onr).asInstanceOf[js.Promise[S]]

    /** While the result is still wrapped in a promise effect, the error has been
     * pushed into `Either`. It would be nice to enhance the types for Left.
     * This is less interesting in scala3 though as there are union types.
     */
    def either =
        val onf: RESOLVE[A, Either[scala.Any, A]] = (a: A) => Right(a)
        val onr: REJECTED[Either[scala.Any, A]] = (err: scala.Any) => Left(err)
        self.`then`[Either[scala.Any, A]](onf, onr).asInstanceOf[js.Promise[Either[scala.Any, A]]]

    /** Push the value into `Option` and the error into None. The result
     * is still wrapped in an effect.
     */
    def opt =
        val onf: RESOLVE[A, Option[A]] = (a: A) => Option(a)
        val onr: REJECTED[Option[A]] = (err: scala.Any) => Option.empty[A]
        self.`then`[Option[A]](onf, onr).asInstanceOf[js.Promise[Option[A]]]
