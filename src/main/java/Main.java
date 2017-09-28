import io.ez.core.Core;
import io.vertx.core.Vertx;


/**
 * Created by eduardo on 28/09/2017.
 */
public class Main {

    public static void main(String[] args) {
        Vertx v = Vertx.vertx();
        v.deployVerticle(new Core());
    }
}
