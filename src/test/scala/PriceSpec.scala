import java.util

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.scalatest._
import org.scalatest.time.{Span, Seconds}
import selenium._
import org.openqa.selenium._
import scala.collection.JavaConversions._
import com.google.common.base.Function

class PriceSpec extends FlatSpec with Matchers with WebBrowser {

  System.setProperty("webdriver.chrome.driver", "/Users/pnagarjuna/Desktop/chromedriver")

  implicit val webDriver: WebDriver = new ChromeDriver()

  val webDriverWait = new WebDriverWait(webDriver, 10)

  val host = "http://www.carwale.com/used/carvaluation/"

  "title of the page" should "be" in {

    go to (host)

    click on radioButton("rdoValType_0")

    click on singleSel("cmbMonth")

    for( monthTag <- singleSel("cmbMonth").underlying.findElements(By.tagName("option")).toList) {
      println(s"for Month ${monthTag.getAttribute("Value")}")
    }

    singleSel("cmbMonth").value = "1"

    singleSel("cmbYear").value = "2015"

    click on singleSel("cmbMake")

    webDriverWait.until(new Function[WebDriver, Boolean] {
      override def apply(f: WebDriver): Boolean = {
        singleSel("cmbMake").underlying.findElements(By.tagName("option")).size() > 1
      }
    })

    //implicitlyWait(Span(2, Seconds))

    singleSel("cmbMake").value = "53"

    click on singleSel("cmbModel")

    singleSel("cmbModel").value = "435"

    implicitlyWait(Span(2, Seconds))

    click on singleSel("cmbVersion")

    implicitlyWait(Span(2, Seconds))

    singleSel("cmbVersion").value = "3121"

    textField("txtKms").value = "1000"

    click on singleSel("cmbValuationCity")

    implicitlyWait(Span(2, Seconds))

    singleSel("cmbValuationCity").value = "1"

    click on "btnSave"

    implicitlyWait(Span(2, Seconds))

    println(find("lblGood").get.text.split(",").map(_.trim).reduce(_ + _))

    (pageTitle.length == 0) should be(false)
  }

}
