package test1
package logging

// import zio._
// import zio.console._

// type Logging = Has[Service]
// trait Service:
//     def warn(msg: String): Task[Unit]
//     def error(msg: String): Task[Unit]
// val live = new Service:
//     def warn(msg: String) = putStrLn("Warn: " + msg).provideLayer(Console.live)
//     def error(msg: String) = putStrLnErr("Error: " + msg).provideLayer(Console.live)

// def warn(msg: String): RIO[Logging, Unit] = ZIO.accessM(_.get.warn(msg))
// def error(msg: String): RIO[Logging, Unit] = ZIO.accessM(_.get.error(msg))
