package tbje.snscript

import tbje.snShell.Shell._
import scala.scalanative.posix.termios
import scala.scalanative.posix.unistd
import scala.scalanative.posix.sys.ioctl
import scala.scalanative._, native._

object SnScript extends App {
  import Init._
  val initMessage = "Welcome to SN Script, please define a place to store your scripts in 'Init.binDir' and make sure it's on the path."
  def notOnPath(dir: String) = s"Could not find '$dir' on the path."
  val usage = "Use 'new' to create a new script."
  def removeTrailingSlash(path: String) = if (path.endsWith("/")) path.init else path
  def replaceHome(path: String) = path.replace(sys.env("HOME"), "~")
  binDir.fold(println(initMessage)){ bin =>
    import scalanative.native._, stdlib._, stdio._
    import scala.scalanative.posix.unistd._
    import scala.scalanative._, native._
    final case class Box[T](var value: Ptr[T])
    def withStringBuffer(x: (Box[CChar], CInt) => Unit): String = {
      Zone{ implicit c =>
        val size: CInt = 1048
        val buff: Box[CChar] = Box(native.alloc[CChar](size))
        x(buff, size)
        fromCString(buff.value)
      }
    }
    val str = withStringBuffer((b: Box[CChar], i: CInt) => readlink(c"/proc/self/exe", b.value, i))
    val linkPath = s"$bin/$binaryName"
    if(!new java.io.File(linkPath).exists) {
      runCommand(s"ln -s $str $linkPath")
      println(s"You should now be able to run '$binaryName' directly from the shell")
    }
    val path = sys.env("PATH")
    val home = sys.env("HOME")
    val pathElems = path.split(java.io.File.pathSeparator).toList
    val onPath = Set(bin, removeTrailingSlash(bin), replaceHome(bin), removeTrailingSlash(replaceHome(bin))).exists(pathElems.contains)
    def handleNew: PartialFunction[String, Unit] = {
      case "new" =>
        args.tail.headOption.fold(println(s"Please use $binaryName new [script name].")){ file =>
          val fullPathFile = bin + file
          val f = new java.io.File(fullPathFile)
          val s: Unit =
          if (f.exists) println(s"script $fullPathFile allready exists")
          else {
            val content = s"""|#!/bin/bash
                              |$binaryName $file $$@""".stripMargin
            val pw = new java.io.PrintWriter(f)
            pw.println(content)
            pw.flush()
            pw.close()
            runCommand(s"chmod u+x $f")
            println("In Init.scala: ")
            println(s"""case "$file" => ???""")
            srcDir.foreach(f => runCommand(s"${sys.env("EDITOR")} $f"))
          }
        }
    }
    if (!onPath) println(notOnPath(bin))
    else args.headOption.fold(println(usage))(handleNew.orElse(Init.handle).orElse {
      case x =>
        println(s"$x not recognized")
    })
  }
}
