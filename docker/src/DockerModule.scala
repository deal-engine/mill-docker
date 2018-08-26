package mill.docker

import mill._
import ammonite.ops._
import ImplicitWd._
import pprint._

trait DockerModule extends Module {

  def dockerMain: T[String]
  def dockerTag: T[String]
  def assembly: T[PathRef]

  def dockerBaseImage: T[String] = "gcr.io/distroless/java:latest"

  def dockerFile: T[String] = s"""
    |FROM ${dockerBaseImage()}
    |COPY app.jar /app.jar
    |ENTRYPOINT ["java", "-cp", "/app.jar", "${dockerMain()}"]
    |CMD []
    """.stripMargin

  def dockerBuild = T {
    val dest = T.ctx().dest
    val tag = dockerTag()

    val file:Path = dest / "Dockerfile"
    write(file, dockerFile())

    val singleJar:PathRef = assembly()
    cp(singleJar.path, dest/"app.jar")

    %('docker, 'build, "-f", file, "-t", tag, dest)
    (singleJar, file, tag)
  }

  def dockerPush = T {
    val build = dockerBuild()
    %('docker, 'push, build._3)
  }

}
