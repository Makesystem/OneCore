
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makesystem.mwc.WebClient;
import com.makesystem.mwc.exceptions.RequestException;
import com.makesystem.mwc.http.client.HttpClient;
import com.makesystem.mwc.types.Protocol;
import com.makesystem.mwc.websocket.client.WebsocketClient;
import com.makesystem.onecore.services.websocket.Message;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.console.ConsoleColor;
import com.makesystem.pidgey.json.JsonConverter;
import com.makesystem.pidgey.monitor.MonitorHelper;
import com.makesystem.pidgey.tester.AbstractTester;
import com.makesystem.pidgey.thread.ThreadsHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Richeli.vargas
 */
public class Client_Tester extends AbstractTester {

    public static void main(String[] args) {
        new Client_Tester().run();
    }

    private HttpClient httpClient;
    private WebsocketClient websocketClient;

    @Override
    protected void preExecution() {

        final Protocol protocol = Protocol.HTTPS;
        final String host;
        final int port;

        switch (protocol) {
            case HTTPS:
                host = "app2.makesystem.com.br";
                port = 443;
                break;
            case HTTP:
            default:
                host = "192.168.2.7";
                port = 7004;
                break;
        }

        httpClient = new HttpClient(protocol, host, port);
        websocketClient = new WebsocketClient(protocol, host, port, "one/one_door");
    }

    @Override
    protected void execution() {

        MonitorHelper.execute(() -> System.out.println(WebClient.discoveryProtocol("vendas.makesystem.com.br"))).print();
        MonitorHelper.execute(() -> System.out.println(WebClient.discoveryProtocol("app2.makesystem.com.br"))).print();

        try {
            System.out.println(httpClient.doPost("/one/commons/post_ping"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RequestException ex) {
            ex.printStackTrace();
        }

        websocketClient.addOnOpenHandler(() -> {

            Console.println("On Open", ConsoleColor.PURPLE);

            final Message message = new Message();
            message.setId(69);
            message.setData("Isso é as informações da mensagem");

            try {
                websocketClient.sendMessage(JsonConverter.write(message));

                new Thread(() -> {

                    ThreadsHelper.sleep(15000);
                    try {
                        message.setData("Esse foi depois de 15 segundos... Mantendo a conexão aberta só pelo ping pong");
                        websocketClient.sendMessage(JsonConverter.write(message));
                    } catch (JsonProcessingException ex) {
                        ex.printStackTrace();
                    }
                }).start();

            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }

        });
        websocketClient.addOnCloseHandler(() -> Console.println("On close", ConsoleColor.PURPLE));
        websocketClient.addOnMessageHandler(message -> {
            Console.println("On message received: " + message, ConsoleColor.CYAN);
            //websocketClient.close();
        });
        websocketClient.addOnErrorHandler(exception -> Console.println("On error: " + exception, ConsoleColor.RED));

        try {
            websocketClient.connect("meu_login", "minha_senha", 10);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (KeyManagementException ex) {
            ex.printStackTrace();
        }

        /*        
        try {
            System.out.println(httpClient.doPost("/one/commons/for_test", HttpClient.attr("param_for_test", 10000.0d)));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RequestExecption ex) {
            ex.printStackTrace();
        }
         */

 /*
        try {
            System.out.println(httpClient.doPost("/one/commons/ping__error"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RequestExecption ex) {
            ex.printStackTrace();
        }
         */
    }

    @Override
    protected void posExecution() {
    }

}
