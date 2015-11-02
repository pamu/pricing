import java.io.PrintWriter
import java.io.File
import org.jsoup.Jsoup

import scala.concurrent.{Await, Future}
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

    val monthsMap = Map (
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

    val cities = List(10)

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

    //val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/Desktop/Cars.csv"))

    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/${args(0).trim}.csv"))

    years.foreach { year => {

      def f(year: Int) = for (
        makesRes <- Utils.getMakes(year);
        someMakesAtomicList <- Future(Utils.parse(makesRes.body))) yield someMakesAtomicList

      def g(year: Int, makeId: Int) = for (
        modelsRes <- Utils.getModels(year, makeId);
        someMakesAtomicList <- Future(Utils.parse(modelsRes.body))
      ) yield someMakesAtomicList

      def h(year: Int, modelId: Int) = for (
        versionsRes <- Utils.getVersions(year, modelId);
        someVersionsAtomicList <- Future(Utils.parse(versionsRes.body))
      ) yield someVersionsAtomicList

      val x = f(year)
      x onComplete {
        case Success(someMakesAtomicList) =>
          someMakesAtomicList match {
            case Some(makesAtomicList) =>
              makesAtomicList.foreach { makeAtomic =>
                val gf = g(year, makeAtomic.Value)
                Await.result(gf, 1 minute)
                gf onComplete {
                  case Success(someModelsAtomicList) =>
                    someModelsAtomicList match {
                      case Some(modelsAtomicList: List[Atomic]) =>
                        modelsAtomicList.foreach { modelAtomic: Atomic =>
                          val hf = h(year, modelAtomic.Value)
                          Await.result(hf, 1 minute)
                          hf onComplete  {
                            case Success(someVersionsAtomicList: Option[List[Atomic]]) =>
                              someVersionsAtomicList match {
                                case Some(versionsAtomicList) =>
                                  versionsAtomicList.foreach { versionAtomic =>
                                    //println(s"$year   ${makeAtomic.Text}    ${modelAtomic.Text}    ${versionAtomic.Text}")
                                    //writer.println(s"$year    ${makeAtomic.Text}    ${modelAtomic.Text}    ${versionAtomic.Text}")
                                    cities.foreach { city =>
                                      kms.foreach { km =>
                                        months.foreach { month =>
                                          val f = Utils.evaluate(city, versionAtomic.Value, year, month, km)
                                          Await.result(f , 1 minute)
                                          f onComplete {
                                            case Success(res) =>
                                              //println(s"body ${res.body.toString}")
                                              val doc = Jsoup.parse(res.body.toString())
                                              val fair = doc.getElementById("lblFair").text().split(",").map(_.trim).reduce(_ + _)
                                              val good = doc.getElementById("lblGood").text().split(",").map(_.trim).reduce(_ + _)
                                              val excellent = doc.getElementById("lblExcellent").text().split(",").map(_.trim).reduce(_ + _)
                                              //println(s"fair $fair good $good excellent $excellent")
                                              writer.println(s"${makeAtomic.Text}    ${modelAtomic.Text}    ${versionAtomic.Text}    ${citiesMap(city)}    $year    ${monthsMap(month)}     $km    $fair    $good    $excellent")
                                              writer.flush()
                                            case Failure(th) =>
                                              th.printStackTrace()
                                          }
                                          Await.result(f, 30 minutes)
                                        }
                                      }
                                    }
                                  }
                                case None =>
                                  println("None")
                                  println(s"$year   ${makeAtomic.Text}    ${modelAtomic.Text}")
                                //writer.println(s"$year    ${makeAtomic.Text}    ${modelAtomic.Text}")
                              }
                            case Failure(th) =>
                              th.printStackTrace()
                          }
                        }
                      case None =>
                        println("None")
                        println(s"$year   ${makeAtomic.Text}")
                      //writer.println(s"$year    ${makeAtomic.Text}")
                    }
                  case Failure(th) =>
                    th.printStackTrace()
                }
              }
            case None =>
              println("None")
          }
        case Failure(th) =>
          th.printStackTrace()
      }

      Await.result(x, 30 minutes)
    }
    }


  }
}
