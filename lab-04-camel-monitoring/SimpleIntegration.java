// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class SimpleIntegration extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      from("timer:java?period=1000")
        .setBody()
          .simple("Hellofrom Camel K")
        .to("log:info");
  }
}
