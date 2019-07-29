
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makesystem.mwc.HttpHelper;
import com.makesystem.mwc.http.client.HttpClient;
import com.makesystem.mwc.websocket.client.WebSocketJRE;
import static com.makesystem.mwi.WebClient.HEADER__SESSION_ID;
import com.makesystem.mwi.exceptions.RequestException;
import com.makesystem.mwi.http.RequestError;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.services.OneServices;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.console.ConsoleColor;
import com.makesystem.pidgey.io.file.Charset;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.ObjectsHelper;
import com.makesystem.pidgey.monitor.MonitorHelper;
import com.makesystem.pidgey.tester.AbstractTester;
import com.makesystem.pidgey.thread.ThreadsHelper;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.java_websocket.exceptions.InvalidDataException;

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

    public static void main(String[] args) throws InvalidDataException, JsonProcessingException {
        new Client_Tester().run();
    }

    private HttpClient httpClient;
    private WebSocketJRE websocketClient;

    @Override
    protected void preExecution() {

        for (OneCloseCodes codes : OneCloseCodes.values()) {
            CloseReason.CloseCodes.register(codes.getCode(), codes.toString());
        }

        final Protocol protocol = Protocol.HTTP;
        final String host;
        final int port;
        // viacep.com.br/ws/01001000/json/   
        switch (protocol) {
            case HTTPS:
                host = "app2.makesystem.com.br";
                port = 443;
                break;
            case HTTP:
            default:
                //host = "viacep.com.br/ws/01001000/json";
                //port = 80;
                host = "192.168.2.7";
                port = 7004;
                break;
        }

        httpClient = new HttpClient(protocol, host, port);
        websocketClient = new WebSocketJRE(protocol, host, port, "one/access");
    }

    @Override
    protected void execution() {

        MonitorHelper.execute(() -> System.out.println(HttpHelper.discoveryProtocol("vendas.makesystem.com.br"))).print();
        MonitorHelper.execute(() -> System.out.println(HttpHelper.discoveryProtocol("app2.makesystem.com.br"))).print();

        try {
            final NameValuePair data = new BasicNameValuePair(OneServices.Commons.GetEcho.Attributes.DATA, "echo test");
            //System.out.println("echo: " + URLDecoder.decode(httpClient.doGet(""), Charset.UTF_8.getName()));
            System.out.println("echo: " + URLDecoder.decode(httpClient.doGet(OneServices.Commons.GetEcho.CONSUMER, data), Charset.UTF_8.getName()));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RequestException ex) {
            ex.printStackTrace();
        }

        if (true) {
            return;
        }

        websocketClient.addOnOpenHandler(() -> {
            Console.println("On Open", ConsoleColor.PURPLE);
        });
        websocketClient.addOnCloseHandler(reason -> Console.println("On close: " + reason.getCloseCode() + " | " + reason.getReasonPhrase(), ConsoleColor.PURPLE));
        websocketClient.addOnMessageHandler(mes -> {
            try {

                //final Message message = JsonConverter.read(mes, Message.class);
                // Console.println("On message received: " + message.getData(), ConsoleColor.CYAN);
                Console.println("On message received: " + mes, ConsoleColor.CYAN);
                //websocketClient.close();

            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        websocketClient.addOnErrorHandler(exception -> Console.println("On error: " + exception, ConsoleColor.RED));

        websocketClient.connect("meu_login", "minha_senha", 10);

        do {

            System.out.println("ping delay: " + websocketClient.getLatency() + " ms");
            ThreadsHelper.sleep(1000);
        } while (websocketClient.isOpen());

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
