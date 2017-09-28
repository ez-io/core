package io.ez.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * Created by eduardo on 28/09/2017.
 */
public class Core extends AbstractVerticle {

    private HttpServer httpServer;
    private Router     router;
    private JsonArray modules = new JsonArray();

    @Override
    public void start() throws Exception {
        super.start();
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options       = new BridgeOptions();
        EventBus      eb            = vertx.eventBus();


        this.httpServer = this.vertx.createHttpServer();

        this.router = Router.router(vertx);

        options.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
        options.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
        sockJSHandler.bridge(options);

        router.route("/eventbus/*").handler(sockJSHandler);

        eb.consumer("init", (m) ->
                eb.send("module.new", new JsonObject().put("name", "Novo Modulo"))
        );

        eb.consumer("module.1.load", (o) ->
                o.reply(new JsonObject().put("name", "Cenas").put("id", 99))
        );

        eb.consumer("system.devices.types", (o) ->
                o.reply(new JsonArray()
                        .add(new JsonObject().put("name", "Botão").put("type", "button"))
                        .add(new JsonObject().put("name", "Potenciometro").put("type", "pot"))
                        .add(new JsonObject().put("name", "Relé").put("type", "relay"))
                )
        );
        eb.consumer("modules.save", (o) -> {
            JsonObject module = (JsonObject) o.body();
            if (module.getInteger("id",0) == 0) {
                module.put("id", this.modules.size());
            }
            this.modules.add(module);
            o.reply(module);
        });
        eb.consumer("modules.all", (o) -> {
            o.reply(this.modules);
        });


        this.httpServer.requestHandler(router::accept).listen(8080);
    }
}
