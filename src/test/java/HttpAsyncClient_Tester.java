
import com.makesystem.mwc.http.client.HttpAsyncClient;
import com.makesystem.mwc.http.client.properties.AsyncGetProperties;
import com.makesystem.mwc.http.client.properties.AsyncPostProperties;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.interfaces.AsyncCallback;
import com.makesystem.pidgey.tester.AbstractTester;
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
public class HttpAsyncClient_Tester extends AbstractTester {

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
        new HttpAsyncClient_Tester().run();
    }

    private HttpAsyncClient httpAsyncClient;

    void close() {
        try {
            httpAsyncClient.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    protected void preExecution() {
        httpAsyncClient = new HttpAsyncClient();
    }

    @Override
    protected void execution() {
        postTest();
        getTest();
    }

    protected void postTest() {

        final NameValuePair data = new BasicNameValuePair("data", "echo");

        final AsyncPostProperties properties = new AsyncPostProperties(PROTOCOL, HOST, PORT, "/one/commons/post_echo");
        properties.setAttributes(data);
        properties.setAsyncCallback(new AsyncCallback<String>() {

            @Override
            public void onSuccess(final String result) {
                close();
                Console.log("post result: {s}", result);
            }

            @Override
            public void onFailure(final Throwable caught) {
                close();
                caught.printStackTrace();
            }
        });

        httpAsyncClient.doPost(properties);

        Console.log(
                "Async post was called");
    }

    protected void getTest() {

        final NameValuePair data = new BasicNameValuePair("data", "echo");

        final AsyncGetProperties properties = new AsyncGetProperties(PROTOCOL, HOST, PORT, "/one/commons/get_echo");
        properties.setAttributes(data);
        properties.setAsyncCallback(new AsyncCallback<String>() {

            @Override
            public void onSuccess(final String result) {
                close();
                Console.log("get result: {s}", result);
            }

            @Override
            public void onFailure(final Throwable caught) {
                close();
                caught.printStackTrace();
            }
        });

        httpAsyncClient.doGet(properties);
        Console.log("Async get was called");
    }

    @Override
    protected void posExecution() {
    }

}
