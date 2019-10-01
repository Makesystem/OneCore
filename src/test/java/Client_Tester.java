
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makesystem.mwc.ProtocolDiscovery;
import com.makesystem.mwc.http.client.HttpClient;
import com.makesystem.mwc.websocket.client.WebSocketJRE;
import com.makesystem.mwi.exceptions.RequestException;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.mwi.websocket.CloseReason;
import com.makesystem.oneentity.core.types.Action;
import com.makesystem.oneentity.core.types.OneCloseCodes;
import com.makesystem.oneentity.core.types.ServiceType;
import com.makesystem.oneentity.services.OneServices;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.console.ConsoleColor;
import com.makesystem.pidgey.json.ObjectMapperJRE;
import com.makesystem.pidgey.lang.NumberHelper;
import com.makesystem.pidgey.monitor.MonitorHelper;
import com.makesystem.pidgey.security.MD5;
import com.makesystem.pidgey.tester.AbstractTester;
import com.makesystem.pidgey.thread.ThreadPool;
import com.makesystem.pidgey.thread.ThreadsHelper;
import com.makesystem.xeonentity.core.types.MessageType;
import com.makesystem.xeonentity.core.websocket.Message;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

        httpClient = new HttpClient(protocol, host, port);
        websocketClient = new WebSocketJRE(protocol, host, port, "one/access");
    }

    @Override
    protected void execution() {
        // genericTest();
        echoTest(50);
    }

    public static interface EchoTest {

        public void execute();
    }

    protected void echoTest(final int interactions) {

        final ThreadPool pool = new ThreadPool(interactions);
        final AtomicLong total = new AtomicLong(0);
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger errors = new AtomicInteger(0);

        doScketTest(interactions, total, count, success, errors, pool);
        //doPostTest(interactions, total, count, success, errors, pool);
        //doGetTest(interactions, total, count, success, errors, pool);
        pool.waitFinish();
        new Thread(() -> {

            do {
                ThreadsHelper.sleep(5000);
            } while (count.get() < interactions);

            System.out.println("Interactions: " + interactions);
            System.out.println(
                    "Success: " + success.get() + " ~ " + NumberHelper.divide(total.get(), success.get()) + " ms");
            System.out.println("Errors: " + errors.get());

            System.exit(0);

        }).start();
    }

    protected void doGetTest(final int interactions,
            final AtomicLong total,
            final AtomicInteger count,
            final AtomicInteger success,
            final AtomicInteger errors,
            final ThreadPool pool) {

        new Thread(() -> IntStream.range(0, interactions).forEach(index -> pool.execute(() -> {

            final NameValuePair data = new BasicNameValuePair(OneServices.Commons.GetEcho.Attributes.DATA, "echo");
            
            final long start = System.currentTimeMillis();
            try {                
                httpClient.doGet(OneServices.Commons.GetEcho.CONSUMER, data);
                total.getAndAdd(System.currentTimeMillis() - start);
                success.getAndIncrement();
            } catch (Throwable ex) {
                errors.getAndIncrement();
            } finally {
                count.getAndIncrement();
            }

        }))).start();

    }
    
    protected void doPostTest(final int interactions,
            final AtomicLong total,
            final AtomicInteger count,
            final AtomicInteger success,
            final AtomicInteger errors,
            final ThreadPool pool) {

        new Thread(() -> IntStream.range(0, interactions).forEach(index -> pool.execute(() -> {

            final NameValuePair data = new BasicNameValuePair(OneServices.Commons.GetEcho.Attributes.DATA, "echo");
            
            final long start = System.currentTimeMillis();
            try {                
                httpClient.doPost(OneServices.Commons.PostEcho.CONSUMER, data);
                total.getAndAdd(System.currentTimeMillis() - start);
                success.getAndIncrement();
            } catch (Throwable ex) {
                errors.getAndIncrement();
            } finally {
                count.getAndIncrement();
            }

        }))).start();

    }

    protected void doScketTest(final int interactions,
            final AtomicLong total,
            final AtomicInteger count,
            final AtomicInteger success,
            final AtomicInteger errors,
            final ThreadPool pool) {

        final Map<String, Long> messages = new ConcurrentHashMap<>();

        websocketClient.addOnMessageHandler(data -> {
            try {
                final Message message = ObjectMapperJRE.read(data, Message.class);
                final Long start = messages.remove(message.getId());
                if (start == null) {
                } else {
                    final long end = System.currentTimeMillis();
                    count.incrementAndGet();
                    switch (message.getType()) {
                        case RESPONSE_ERROR:
                            errors.getAndIncrement();
                            break;
                        default:
                            success.getAndIncrement();
                            total.getAndAdd(end - start);
                            break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

        websocketClient.connect("admin", MD5.toMD5("admin"), "null", "127.0.0.1", "127.0.0.1");

        new Thread(() -> IntStream.range(0, interactions).forEach(index -> pool.execute(() -> callSocketEcho(messages)))).start();
    }

    protected String callSocketEcho(final Map<String, Long> messages) {

        final Message message = new Message();
        message.setAction(Action.ONE__ECHO.toString());
        message.setService(ServiceType.ONE.toString());
        message.setType(MessageType.COMMAND);
        message.setData("echo");

        try {
            messages.put(message.getId(), System.currentTimeMillis());
            websocketClient.sendMessage(ObjectMapperJRE.write(message));
        } catch (JsonProcessingException ex) {
            return null;
        }

        return message.getId();
    }

    protected void genericTest() {

        MonitorHelper.execute(() -> System.out.println(ProtocolDiscovery.discovery("vendas.makesystem.com.br"))).print();
        MonitorHelper.execute(() -> System.out.println(ProtocolDiscovery.discovery("app2.makesystem.com.br"))).print();

        try {
            final NameValuePair data = new BasicNameValuePair(OneServices.Commons.GetEcho.Attributes.DATA, "echo test");
            System.out.println("echo: " + httpClient.doGet(OneServices.Commons.GetEcho.CONSUMER, data));
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
                Console.println("On message received: " + mes, ConsoleColor.CYAN);
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

    }

    @Override
    protected void posExecution() {
    }

}
