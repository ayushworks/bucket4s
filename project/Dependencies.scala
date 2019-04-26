import sbt._

object Dependencies {

  /** Core dependencies */
  val CatsVersion          = "1.4.0"
  val CatsEffectVersion    = "1.0.0"

  /** Platform dependencies */
  val Http4sVersion        = "0.19.0"
  val DoobieVersion        = "0.5.3"
  val CirceVersion         = "0.9.3"
  val PureConfigVersion    = "0.9.2"
  val LogbackVersion       = "1.2.3"
  val SlickVersion         = "3.2.3"

  /** Test dependencies */
  val ScalaTestVersion     = "3.0.5"
  val H2Version            = "1.4.197"
  val FlywayVersion        = "5.2.0"

  /** Build dependencies */
  val KindProjectorVersion = "0.9.7"

  val coreDependencies = Seq(
    "org.typelevel"         %% "cats-core"            % CatsVersion,
    "org.typelevel"         %% "cats-macros"          % CatsVersion,
    "org.typelevel"         %% "cats-effect"          % CatsEffectVersion,
  )

  val platformDependencies = Seq(
    "org.http4s"            %% "http4s-blaze-server"  % Http4sVersion,
    "org.http4s"            %% "http4s-circe"         % Http4sVersion,
    "org.http4s"            %% "http4s-dsl"           % Http4sVersion,
    "org.tpolecat"          %% "doobie-core"          % DoobieVersion,
    "org.tpolecat"          %% "doobie-h2"            % DoobieVersion,
    "org.tpolecat"          %% "doobie-hikari"        % DoobieVersion,
    "org.flywaydb"          %  "flyway-core"          % FlywayVersion,
    "io.circe"              %% "circe-generic"        % CirceVersion,
    "com.github.pureconfig" %% "pureconfig"           % PureConfigVersion,
    "ch.qos.logback"        %  "logback-classic"      % LogbackVersion,
    "com.typesafe.slick"    %% "slick"                % SlickVersion,
    "com.typesafe.slick"    %% "slick-hikaricp"       % SlickVersion,
    "co.fs2"                %% "fs2-reactive-streams" % "1.0.0"
  )

  val testDependencies = Seq(
    "org.scalatest"         %% "scalatest"            % ScalaTestVersion  % "it,test",
    "org.http4s"            %% "http4s-blaze-client"  % Http4sVersion     % "it,test",
    "com.h2database"        %  "h2"                   % H2Version         % "it,test",
    "io.circe"              %% "circe-literal"        % CirceVersion      % "it,test",
    "io.circe"              %% "circe-optics"         % CirceVersion      % "it,test"
  )

  /** Build dependencies */
  val kindProjector =
    "org.spire-math"        %% "kind-projector"       % KindProjectorVersion
}
