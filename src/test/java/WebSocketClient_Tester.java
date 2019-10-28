
import com.makesystem.mwc.websocket.client.WebSocketJRE;
import com.makesystem.mwi.types.Protocol;
import com.makesystem.pidgey.console.Console;
import com.makesystem.pidgey.console.ConsoleColor;
import com.makesystem.pidgey.security.MD5;
import com.makesystem.pidgey.tester.AbstractTester;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Richeli.vargas
 */
public class WebSocketClient_Tester extends AbstractTester {

    static final Protocol PROTOCOL = Protocol.HTTP;
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
        new WebSocketClient_Tester().run();
    }

    private WebSocketJRE websocketClient;

    @Override
    protected void preExecution() {

        websocketClient = new WebSocketJRE(PROTOCOL, HOST, PORT, "one/access");
    }

    @Override
    protected void execution() {

        websocketClient.addOnOpenHandler(() -> {

            Console.log("{cc}On Open", ConsoleColor.PURPLE);
            websocketClient.sendMessage((new Date()) + ": Hello");

        });
        websocketClient.addOnCloseHandler(closeReason -> Console.log("{cc}On close: " + closeReason.getCloseCode().getCode(), ConsoleColor.PURPLE));
        websocketClient.addOnMessageHandler(message -> {
            Console.log("{cc}On message received: " + message, ConsoleColor.CYAN);
            //websocketClient.close();
        });
        websocketClient.addOnErrorHandler(exception -> Console.log("{cc}On error: " + exception, ConsoleColor.RED));

        websocketClient.connect("admin", MD5.toMD5("admin"));
        

    }

    @Override
    protected void posExecution() {
    }

}
