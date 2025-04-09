import protocols.ComunicationProtocol;
import utils.ProtocolUtils;
import protocols.HeartBeat;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Gateway {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP) and a port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        HeartBeat heartBeat = new HeartBeat(protocol);
        List<String> requestsLog = new ArrayList<>();

        protocol.listen(3500, message -> {
            requestsLog.add("Request received: " + message);
            heartBeat.getServerList().forEach(server -> {
                int serverPort = (int) server.get("port");
                protocol.send(serverPort, message);
                requestsLog.add("Forwarded to servers " + serverPort + ": " + message);
            });
        });

        heartBeat.listen(port);
        while (true) {
            displayInformation(heartBeat.getServerList(), requestsLog);
        }
    }

    private static void displayInformation(
            List<Map<String, Object>> serversUp,
            List<String> requestsLog) {

        System.out.println("Servers Up:");
        serversUp.forEach(server -> {
            long lastBeat = (long) server.get("lastBeat");
            double secondsSinceLastBeat = (System.currentTimeMillis() - lastBeat) / 1000.0;
            System.out.println(server.get("port") + " - " + secondsSinceLastBeat + " seconds ago");
        });

        System.out.println("\n\n----------------------");
        System.out.println("Requests log:");
        requestsLog.forEach(request -> {
            System.out.println(request);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
