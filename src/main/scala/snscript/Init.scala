package tbje.snscript

object Init {
  val binDir: Option[String] = None
  val srcDir: Option[String] = None
  val binaryName = "snscript"

  def handle: PartialFunction[String, Unit] = {
    case "test4" => println("hello")
  }
}
