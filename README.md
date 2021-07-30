# dotty scalajs

You need a working nodejs.

To run the example and continuously monitor the output to run directly in node:

```sh
npm install
```

Then either start up vscode or your favorite editor. In a terminal or have your editor/IDE run:

```sh
sbt ~fastOptJS
```

and separately:

```sh
npx nodemon -x "node --enable-source-maps" main/target/scala-3.0.0-M1/main-fastopt.js
```

If you are using the nightly build which uses "the next" version, use:

```sh
npx nodemon -x "node --enable-source-maps" main/target/scala-3.0.0-M1/main-fastopt.js
```

The main initializer is set to true in build.sbt so it will run
the dotty scalajs javascript like a script.

Feel free to add js packages via `npm install <package>` and use a facade to include
them in your scala program.

## More dotty examples

More dotty examples are at the scala example project: https://github.com/lampepfl/dotty-example-project

## License

MIT License.

See LICENSE.
