scalaVersion := "2.11.12"

enablePlugins(ScalaNativePlugin)

libraryDependencies ++= (
  "com.github.tbje" %%% "sn-shell" % "0.2-SNAPSHOT" ::
    Nil
)
