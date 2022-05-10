// camel-k: language=java

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;

public class SimplePredictor extends RouteBuilder {

  @Override
  public void configure() throws Exception {

      from("knative:event/market.btc.usdt")
        .unmarshal().json()
        .transform().simple("${body[price]}")
        .log("Latest value for BTC/USDT is: ${body}")
        .to("seda:evaluate?waitForTaskToComplete=Never")
        .setBody().constant("");

      from("seda:evaluate")
        .bean("algorithm")
        .choice()
          .when(body().isNotNull())
            .log("Predicted action: ${body}")
            .to("direct:publish");

      from("direct:publish")
        .marshal().json()
        .removeHeaders("*")
        .setHeader("CE-Type", constant("predictor.simple"))
        .to("knative:event");

  }


  @BindToRegistry("algorithm")
  public static class SimpleAlgorithm {

    private double sensitivity = 0.0001;

    private Double previous;

    public Map<String, Object> predict(double value) {
      Double reference = previous;
      this.previous = value;

      if (reference != null && value < reference * (1 - sensitivity)) {
        Map<String, Object> res = new HashMap<>();
        res.put("value", value);
        res.put("operation", "buy");
        return res;
      } else if (reference != null && value > reference * (1 + sensitivity)) {
        Map<String, Object> res = new HashMap<>();
        res.put("value", value);
        res.put("operation", "sell");
        return res;
      }
      return null;
    }
  }

}
