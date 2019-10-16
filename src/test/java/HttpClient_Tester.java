
import com.makesystem.mwc.http.client.HttpClient;
import com.makesystem.mwc.http.client.properties.GetProperties;
import com.makesystem.mwc.http.client.properties.PostProperties;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.oneentity.services.OneServices.Commons.*;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.lang.Average;
import com.makesystem.pidgey.monitor.Monitor;
import com.makesystem.pidgey.monitor.MonitorResult;
import com.makesystem.pidgey.tester.AbstractTester;
import com.makesystem.pidgey.thread.ThreadPool;
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

    void close() {
        try {
            httpClient.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    protected void preExecution() {
        httpClient = new HttpClient();
    }

    @Override
    protected void execution() {
        postTest();
        getTest();
        //performanceTest();
        close();
    }

    protected void postTest() {

        final NameValuePair data = new BasicNameValuePair(PostEcho.Attributes.DATA, "echo");

        final PostProperties properties = new PostProperties(PROTOCOL, HOST, PORT, PostEcho.CONSUMER);
        properties.setAttributes(data);

        try {
            final String result = httpClient.doPost(properties);
            Console.log("post result: {s}", result);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Console.log("Sync post was called");
    }

    protected void getTest() {

        final NameValuePair data = new BasicNameValuePair(GetEcho.Attributes.DATA, "echo");

        final GetProperties properties = new GetProperties(PROTOCOL, HOST, PORT, GetEcho.CONSUMER);
        properties.setAttributes(data);

        try {
            final String result = httpClient.doGet(properties);
            Console.log("post result: {s}", result);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        Console.log("Sync get was called");
    }

    @SuppressWarnings({"CallToPrintStackTrace", "UseSpecificCatch"})
    protected void performanceTest() {

        final NameValuePair data = new BasicNameValuePair(PostEcho.Attributes.DATA, "echo");

        final PostProperties properties = new PostProperties(PROTOCOL, HOST, PORT, PostEcho.CONSUMER);
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
    }

    @Override
    protected void posExecution() {
    }

}
