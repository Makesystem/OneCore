
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

        httpClient = new HttpClient(protocol, host, port) {

            @Override
            protected String doGet(final String methodPath, final String contentType, final NameValuePair... attributes) throws IOException, RequestException {

                final String _url = getBaseUrl();
                final String _path = methodPath;
                final String _params = attributesToUrl(attributes);

                final String url = HttpHelper.buildUrl(_url, _path, _params);

                try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {

                    if (getConnectionTimeout() != null) {
                        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, getConnectionTimeout());
                        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, getConnectionTimeout());
                    }

                    final HttpGet httpGet = new HttpGet(url);
                    if (ObjectsHelper.isNotNullAndNotEmpty(contentType)) {
                        httpGet.setHeader("Content-type", contentType);
                    }

                    // Set session id
                    if (ObjectsHelper.isNotNullAndNotEmpty(getSessionId())) {
                        httpGet.addHeader(HEADER__SESSION_ID, getSessionId());
                    }

                    try (final CloseableHttpResponse response = httpClient.execute(httpGet)) {

                        // Status code
                        // 2xx: Successful
                        // 3xx: Redirection
                        // 4xx: Client Error
                        // 5xx: Server Error
                        final int statusCode = response.getStatusLine().getStatusCode();
                        final int statusGroup = statusCode / 100;

                        // Get response data
                        final HttpEntity entity = response.getEntity();

                        if (entity == null) {
                            System.out.println("AQUI: " + statusGroup);
                            // If status code will be 2xx return TRUE, or else FALSE
                            // Status code 2xx is Successful
                            // https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                            return statusGroup == 2
                                    ? Boolean.TRUE.toString()
                                    : Boolean.FALSE.toString();
                        }

                        // Convert response to string
                        final String result = EntityUtils.toString(entity, getChartset());
                        EntityUtils.consume(entity);

                        // Client error or Server error
                        if (statusGroup == 4 || statusGroup == 5) {
                            if (result.startsWith("{") && result.endsWith("}")) {
                                final RequestError error = ObjectMapperJRE.read(result, RequestError.class);
                                throw new RequestException(
                                        statusCode,
                                        error.getMessage(),
                                        new Exception(error.getThrowable()));
                            } else {
                                throw new RequestException(statusCode,
                                        URLDecoder.decode(result, getChartset()));
                            }
                        }

                        System.out.println("result: " + result);
                        return result;
                    }
                }
            }

        };
        websocketClient = new WebSocketJRE(protocol, host, port, "one/one_door");
    }

    @Override
    protected void execution() {

        MonitorHelper.execute(() -> System.out.println(HttpHelper.discoveryProtocol("vendas.makesystem.com.br"))).print();
        MonitorHelper.execute(() -> System.out.println(HttpHelper.discoveryProtocol("app2.makesystem.com.br"))).print();

        try {
            final NameValuePair data = new BasicNameValuePair(OneServices.Commons.GetEcho.Attributes.DATA, URLEncoder.encode("echo test", Charset.UTF_8.getName()));
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
