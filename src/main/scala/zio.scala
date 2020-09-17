package test1

import zio._
import zio.console._

val effectToRun = putStrLn("ping")

// runs 3 times
val testSchedule =
    putStrLn("testSchedule") *>
    effectToRun.repeat(Schedule.recurs(3))

// stops after 1
val testSchedule2 =
    putStrLn("testSchedule2") *>
    effectToRun *>
    UIO(10).repeat(Schedule.recurUntil(_ == 10))
