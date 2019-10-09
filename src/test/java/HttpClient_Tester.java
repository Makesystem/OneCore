
import com.makesystem.mwc.http.client.HttpClient;
import com.makesystem.mwc.http.client.properties.GetProperties;
import com.makesystem.mwc.http.client.properties.PostProperties;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.lang.Average;
import com.makesystem.pidgey.monitor.Monitor;
import com.makesystem.pidgey.monitor.MonitorResult;
import com.makesystem.pidgey.tester.AbstractTester;
import com.makesystem.pidgey.thread.ThreadPool;
import java.io.IOException;
import java.util.stream.IntStream;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Richeli.vargas
 */
public class HttpClient_Tester extends AbstractTester {

    static final Protocol PROTOCOL = Protocol.HTTPS;
    static final String HOST;
    static final int PORT;

    static {
        switch (PROTOCOL) {
            case HTTPS:
                HOST = "app2.makesystem.com.br";
                PORT = 443;
                break;
            case HTTP:
            default:
                HOST = "app2.makesystem.com.br";
                PORT = 80;
                break;
        }
    }

    public static void main(String[] args) {
        new HttpClient_Tester().run();
    }

    private HttpClient httpClient;

    @Override
    protected void preExecution() {
        httpClient = new HttpClient();
    }

    @Override
    protected void execution() {
        //postTest();
        //getTest();
        performanceTest();
    }

    protected void postTest() {

        final NameValuePair data = new BasicNameValuePair("data", "echo");

        final PostProperties properties = new PostProperties(PROTOCOL, HOST, PORT, "/one/commons/post_echo");
        properties.setAttributes(data);

        try {
            final String result = httpClient.doPost(properties);
            Console.log("post result: {s}", result);
            httpClient.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Console.log("Async post was called");
    }

    protected void getTest() {

        final NameValuePair data = new BasicNameValuePair("data", "echo");

        final GetProperties properties = new GetProperties(PROTOCOL, HOST, PORT, "/one/commons/get_echo");
        properties.setAttributes(data);

        try {
            final String result = httpClient.doGet(properties);
            Console.log("post result: {s}", result);
            httpClient.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Console.log("Async get was called");
    }

    @SuppressWarnings({"CallToPrintStackTrace", "UseSpecificCatch"})
    protected void performanceTest() {

        final NameValuePair data = new BasicNameValuePair("data", "echo");

        final PostProperties properties = new PostProperties(PROTOCOL, HOST, PORT, "/one/commons/post_echo");
        properties.setAttributes(data);

        final Average<String> average = new Average<>("Post Test");
        final ThreadPool pool = new ThreadPool(100);

        IntStream.range(0, 10000).forEach(index
                -> pool.execute(() -> {
                    final MonitorResult result = Monitor.exec(() -> {
                        try {
                            httpClient.doPost(properties);
                        } catch (final Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                    average.increase(result.getDuration());
                }));

        pool.waitFinish();
        Console.log("Calls: {i} ~ {i}ms / call", average.getCount(), average.getAverage());
        try {
            httpClient.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void posExecution() {
    }

}
