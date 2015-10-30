import java.io.PrintWriter
import java.io.File
import _root_.Utils.Atomic

import scala.concurrent.{Await, Future}
import scala.util.Success
import scala.util.Failure

import scala.concurrent.duration._

/**
 * Created by pnagarjuna on 18/09/15.
 */
object Main {
  def main(args: Array[String]): Unit = {
    println("Carwale pricing engine")


    val months = (1 to 12).toList

    val years = (1990 to 2015).toList

    val kms = List(1000) ++ (10000 to 160000 by 10000).toList

    val cities = List(2, 176, 273, 225, 246, 105, 1, 10, 13, 12, 40, 224, -1, 3, 31).distinct

    //val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/Desktop/Cars.csv"))

    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/Desktop/data.csv"))

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
                g(year, makeAtomic.Value) onComplete {
                  case Success(someModelsAtomicList) =>
                    someModelsAtomicList match {
                      case Some(modelsAtomicList: List[Atomic]) =>
                        modelsAtomicList.foreach { modelAtomic: Atomic =>
                          h(year, modelAtomic.Value) onComplete  {
                            case Success(someVersionsAtomicList: Option[List[Atomic]]) =>
                              someVersionsAtomicList match {
                                case Some(versionsAtomicList) =>
                                  versionsAtomicList.foreach { versionAtomic =>
                                    println(s"$year   ${makeAtomic.Text}    ${modelAtomic.Text}    ${versionAtomic.Text}")
                                    //writer.println(s"$year    ${makeAtomic.Text}    ${modelAtomic.Text}    ${versionAtomic.Text}")
                                    cities.foreach { city =>
                                      kms.foreach { km =>
                                        writer.println(s"$city $km $year ${makeAtomic.Value} ${modelAtomic.Value} ${versionAtomic.Value}")
                                        writer.flush()
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
