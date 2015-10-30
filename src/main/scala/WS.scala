/**
 * Created by pnagarjuna on 30/10/15.
 */
object WS {
  val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
  val client = new play.api.libs.ws.ning.NingWSClient(builder.build())
}
