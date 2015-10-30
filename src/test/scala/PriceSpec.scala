import java.io.{PrintWriter, FileInputStream, File}
import java.util.Scanner

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.scalatest._
import selenium._
import org.openqa.selenium._
import com.google.common.base.Function
import scala.collection.JavaConversions._

class PriceSpec extends FlatSpec with Matchers with WebBrowser {

  System.setProperty("webdriver.chrome.driver", "/Users/pnagarjuna/Desktop/chromedriver")

  implicit val webDriver: WebDriver = new ChromeDriver()

  val webDriverWait = new WebDriverWait(webDriver, 30)

  val host = "http://www.carwale.com/used/carvaluation/"

  "title of the page" should "be" in {

    go to (host)

    val scanner = new Scanner(new FileInputStream(new File("/Users/pnagarjuna/Desktop/data.csv")))

    val writer = new PrintWriter(new File("/Users/pnagarjuna/Desktop/pricing.csv"))

    var lineNumber = 0

    while (scanner.hasNext) {

      lineNumber += 1

      println(s"Line Number: $lineNumber")

      val list = scanner.nextLine().split("\\s+").map(_.trim)

      println(s"list ${list.mkString(" ")}")

      val buyerPrice = getPrices(true, list).asInstanceOf[BuyerPrice]

      goBack()
      reloadPage()

      val sellerPrice = getPrices(false, list).asInstanceOf[SellerPrice]

      writer.println(s"""${buyerPrice.make}    ${buyerPrice.model}    ${buyerPrice.version}    ${buyerPrice.year}    ${buyerPrice.city}    ${buyerPrice.kms}    ${buyerPrice.fp}    ${buyerPrice.gp}    ${buyerPrice.ep}    ${sellerPrice.fp}    ${sellerPrice.gp}    ${sellerPrice.ep}""")

      writer.flush()


      goBack()
      reloadPage()
    }

    quit()

    (pageTitle.length == 0) should be(false)
  }


  trait Price

  case class BuyerPrice(make: String,
                       model: String,
                       version: String,
                       year: String,
                       city: String,
                       kms: String,
                       fp: String,
                       gp: String,
                       ep: String) extends Price

  case class SellerPrice(make: String,
                        model: String,
                        version: String,
                        year: String,
                        city: String,
                        kms: String,
                        fp: String,
                        gp: String,
                        ep: String) extends Price

  def getPrices(buyer: Boolean, list: Array[String]): Price = {
    val city = list(0)
    val kms = list(1)
    val year = list(2)
    val make = list(3)
    val model = list(4)
    val version = list(5)

    if (buyer)
      click on radioButton("rdoValType_0")
    else
      click on radioButton("rdoValType_1")

    click on singleSel("cmbMonth")

    singleSel("cmbMonth").value = "6"

    singleSel("cmbYear").value = year

    click on singleSel("cmbMake")

    webDriverWait.until(new Function[WebDriver, Boolean] {
      override def apply(f: WebDriver): Boolean = {
        singleSel("cmbMake").underlying.findElements(By.tagName("option")).size() > 1
      }
    })

    singleSel("cmbMake").value = make

    val makeText = singleSel("cmbMake").underlying.findElements(By.tagName("option")).toList.filter(_.getAttribute("Value") == make).map(_.getText).head

    click on singleSel("cmbModel")

    webDriverWait.until(new Function[WebDriver, Boolean] {
      override def apply(f: WebDriver): Boolean = {
        singleSel("cmbModel").underlying.findElements(By.tagName("option")).size() > 1
      }
    })

    singleSel("cmbModel").value = model

    val modelText = singleSel("cmbModel").underlying.findElements(By.tagName("option")).toList.filter(_.getAttribute("Value") == model).map(_.getText).head

    click on singleSel("cmbVersion")

    webDriverWait.until(new Function[WebDriver, Boolean] {
      override def apply(f: WebDriver): Boolean = {
        singleSel("cmbVersion").underlying.findElements(By.tagName("option")).size() > 1
      }
    })

    singleSel("cmbVersion").value = version

    val versionText = singleSel("cmbVersion").underlying.findElements(By.tagName("option")).toList.filter(_.getAttribute("Value") == version).map(_.getText).head

    click on textField("txtKms")

    textField("txtKms").value = kms

    click on singleSel("cmbValuationCity")

    webDriverWait.until(new Function[WebDriver, Boolean] {
      override def apply(f: WebDriver): Boolean = {
        singleSel("cmbValuationCity").underlying.findElements(By.tagName("option")).size() > 1
      }
    })

    singleSel("cmbValuationCity").value = city

    val cityText = singleSel("cmbValuationCity").underlying.findElements(By.tagName("option")).toList.filter(_.getAttribute("Value") == city).map(_.getText).head

    click on "btnSave"

    webDriverWait.until(ExpectedConditions.textToBePresentInElement(By.cssSelector("h1.font30.text-black"), "Used Car Valuation Report"))

    val fairPrice = find("lblFair").get.text.split(",").map(_.trim).reduce(_ + _)

    val goodPrice = find("lblGood").get.text.split(",").map(_.trim).reduce(_ + _)

    val excellentPrice = find("lblExcellent").get.text.split(",").map(_.trim).reduce(_ + _)

    if (buyer)
      println("Buyer Price")
    else
    println("Seller Price")

    println(
      s"""
         |fairPrice => $fairPrice
         |goodPrice => $goodPrice
         |excellentPrice => $excellentPrice
         """.stripMargin)

    if (buyer)
      BuyerPrice(makeText, modelText, versionText, year, cityText, kms, fairPrice, goodPrice, excellentPrice)
    else
      SellerPrice(makeText, modelText, versionText, year, cityText, kms, fairPrice, goodPrice, excellentPrice)
  }
}
