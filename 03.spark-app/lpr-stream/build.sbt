name := "lpr-stream"

version := "1.0"

scalaVersion := "2.11.10"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.1.1",
  "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.1.1",
  "org.apache.spark" % "spark-streaming_2.11" % "2.1.1",
  "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "5.4.0"
)


mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList("META-INF", "ECLIPSEF.RSA")         => MergeStrategy.discard
  case PathList("META-INF", "mailcap")         => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF")         => MergeStrategy.discard
  case x => MergeStrategy.last
}
}