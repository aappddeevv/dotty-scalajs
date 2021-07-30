// package test1

// import scala.annotation.alpha
// import zio._
// import zio.console._

// val effectToRun = putStrLn("ping")

// // runs 3 times
// val testSchedule =
//     putStrLn("testSchedule") *>
//     effectToRun.repeat(Schedule.recurs(3))

// // stops after 1
// val testSchedule2 =
//     putStrLn("testSchedule2") *>
//     effectToRun *>
//     UIO(10).repeat(Schedule.recurUntil(_ == 10))

// trait Aspect[-R, +E]:
//     def apply[R1 <: R, E1 >: E, A](zio: ZIO[R1,E1,A]): ZIO[R1,E1,A]

// // extension [-R,+E,+A](zio: ZIO[R,E,A]):
// //     def @@[R1 <: R, E1 >: E](aspect: Aspect[R1,E1]): ZIO[R1,E1,A] = aspect(zio)

// extension [R,E,A,R1,E1](zio: ZIO[R,E,A]):
//     //def @@(aspect: Aspect[R1,E1])(using ev: R1 <:< R, ev2: E <:< E1): ZIO[R1,E1,A] = aspect(zio)
//     @alpha("at") def @@(aspect: Aspect[R1,E1])(using ev: R1 <:< R, ev2: E <:< E1) = aspect(zio)
