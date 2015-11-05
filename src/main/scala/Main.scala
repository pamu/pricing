import java.io.{FileInputStream, PrintWriter, File}
import play.api.Logger

import scala.concurrent.{Future, Await}
import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pnagarjuna on 18/09/15.
 */
object Main {
  def main(args: Array[String]): Unit = {
    println("Carwale pricing engine")

    if (args.length != 1) {
      println(s"Please provide the file name")
      sys.exit()
    }

    //val months = (1 to 12).toList

    val months = List(1, 4, 7, 10)

    val monthsMap = Map(
      1 -> "Jan",
      2 -> "Feb",
      3 -> "Mar",
      4 -> "Apr",
      5 -> "May",
      6 -> "Jun",
      7 -> "Jul",
      8 -> "Aug",
      9 -> "Sep",
      10 -> "Oct",
      11 -> "Nov",
      12 -> "Dec"
    )

    val years = (2004 to 2015).toList

    val kms = List(1000) ++ (10000 to 160000 by 10000).toList

    //val cities = List(2, 176, 273, 225, 246, 105, 1, 10, 13, 12, 40, 224, -1, 3, 31).distinct

    val cities = List(2)

    val citiesMap = Map(
      2 -> "Bangalore",
      176 -> "Chennai",
      273 -> "Faridabad",
      225 -> "Ghaziabad",
      246 -> "Gurgoan",
      105 -> "Hyberabad",
      1 -> "Mumbai",
      10 -> "New Delhi",
      13 -> "Navi Mumbai",
      12 -> "Pune",
      40 -> "Thane",
      224 -> "Noida",
      -1 -> "Other",
      3 -> "Nagpur",
      31 -> "Nashik")

    val filename = args(0)


    write(filename) { (writer, errors) =>
      fetch(filename) { datom =>
        cities.foreach { city =>
          val bigF = Future.sequence {
          kms.flatMap { km =>
              months.map { month =>
                val f = Utils.evaluate(city, datom.versionId.toInt, datom.year.toInt, month, km).flatMap { res =>
                  val html = res.body.toString
                  Utils.parsePage(html)
                }.map { prices =>
                  Right(km, month, prices)
                }
                val g = f.recover { case th => Left(km, month) }
                g
              }
          }}

          Await.ready(bigF, 5 minutes)

          bigF onComplete {
            case Success(list) =>
              list.foreach { eitherItem =>
                eitherItem match {
                  case Right(item) =>
                    val km = item._1
                    val month = item._2
                    val price = item._3
                    val fair = price._1
                    val good = price._2
                    val excellent = price._3
                    writer.println(s"${datom.year}    ${datom.make}    ${datom.model}    ${datom.version}    ${citiesMap(city)}    ${monthsMap(month)}    $km    $fair    $good    $excellent")
                    writer.flush()
                  case Left(km, month) =>
                    errors.println(s"${datom.year}    ${datom.make}    ${datom.model}    ${datom.version}    ${citiesMap(city)}    ${monthsMap(month)}    $km")
                    errors.flush()
                }
              }
            case Failure(th) =>
              errors.println(s"${datom.year}    ${datom.make}    ${datom.model}    ${datom.version}    ${citiesMap(city)}    ${th.getMessage}")
              errors.flush()
          }
        }
      }
    }
  }

  case class Datom(year: String, make: String, model: String, version: String, versionId: String)

  def fetch(filename: String)(f: Datom => Unit): Unit = {
    import java.util.Scanner
    val scan = new Scanner(new FileInputStream((new File(s"${System.getProperty("user.home")}/Desktop/pricing/src/main/scala/values.txt"))))
    while (scan.hasNext) {
      val line = scan.nextLine()
      val lines = line.split("    ").map(_.trim)
      if (lines.length == 5) {
        val year = lines(0)
        val make = lines(1)
        val model = lines(2)
        val version = lines(3)
        val versionId = lines(4)
        val datom = Datom(year, make, model, version, versionId)
        f(datom)
      } else {
        Logger.error("Skipping row. Row in unknown format")
      }
    }
  }

  def write(filename: String)(f: (PrintWriter, PrintWriter) => Unit): Unit = {
    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/${filename.trim}.csv"))
    val errors = new PrintWriter(new File(s"${System.getProperty("user.home")}/errors.csv"))
    f(writer, errors)
    writer flush()
    writer close()
  }
}
