import java.io.PrintWriter
import java.io.File
import org.jsoup.Jsoup
import play.api.Logger

import scala.concurrent.Await
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

    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/errors.csv"))
    write(filename) { writer =>
      fetch(filename) { datom =>
        cities.foreach { city =>
          kms.foreach { km =>
            months.foreach { month =>
              val f = Utils.evaluate(city, datom.versionId.toInt, datom.year.toInt, month, km)
              Await.result(f, 1 minute)
              f onComplete {
                case Success(res) =>
                  //println(s"body ${res.body.toString}")
                  val doc = Jsoup.parse(res.body.toString())
                  val fair = doc.getElementById("lblFair").text().split(",").map(_.trim).reduce(_ + _)
                  val good = doc.getElementById("lblGood").text().split(",").map(_.trim).reduce(_ + _)
                  val excellent = doc.getElementById("lblExcellent").text().split(",").map(_.trim).reduce(_ + _)
                  //println(s"fair $fair good $good excellent $excellent")
                  writer.println(s"${datom.year}    ${datom.make}    ${datom.model}    ${citiesMap(city)}    ${monthsMap(month)}    $km    $fair    $good    $excellent")
                  writer.flush()
                case Failure(th) =>
                  writer.println(s"error ${th.getMessage}")
                  writer.flush()
                  th.printStackTrace()
              }
              //Await.result(f, 30 minutes)
            }
          }
        }
      }
    }
  }

  case class Datom(year: String, make: String, model: String, version: String, versionId: String)

  def fetch(filename: String)(f: Datom => Unit): Unit = {
    import java.util.Scanner
    val scan = new Scanner(System.in)
    while (scan.hasNext) {
      val line = scan.nextLine()
      val lines = line.split("\\s+{4}").map(_.trim)
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

  def write(filename: String)(f: PrintWriter => Unit): Unit = {
    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/${filename.trim}.csv"))
    f(writer)
    writer flush()
    writer close()
  }
}
