import protocols.ComunicationProtocol;
import utils.ProtocolUtils;
import protocols.HeartBeat;
import utils.Log;

import java.util.List;
import java.util.Map;

public class Gateway {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP) and a port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        HeartBeat heartBeat = new HeartBeat(protocol);
        Log requestsLog = new Log();

        protocol.listen(3500, message -> {
            requestsLog.add("Request received: " + message);

            if (heartBeat.getServerList().isEmpty()) {
                requestsLog.add("ERROR: No servers available");
                return false;
            }

            boolean allSuccess = true;
            for (Map<String, Object> server : heartBeat.getServerList()) {
                int serverPort = (int) server.get("port");
                requestsLog.add("Forwarding to server " + serverPort + ": " + message);
                boolean success = protocol.send(serverPort, message);
                if (success) {
                    requestsLog.add("SUCCESS: Server " + serverPort + " responded successfully");
                } else {
                    requestsLog.add("ERROR: Server " + serverPort + " did not respond");
                    allSuccess = false;
                }
            }
            return allSuccess;
        });

        heartBeat.listen(port);

        while (true) {
            displayInformation(heartBeat.getServerList(), requestsLog.getLog(10));
        }
    }

    private static void displayInformation(
            List<Map<String, Object>> serversUp,
            String log) {

        System.out.println("Servers Up:");
        serversUp.forEach(server -> {
            long lastBeat = (long) server.get("lastBeat");
            double secondsSinceLastBeat = (System.currentTimeMillis() - lastBeat) / 1000.0;
            System.out.println(server.get("port") + " - " + secondsSinceLastBeat + " seconds ago");
        });

        System.out.println("\n\n----------------------");
        System.out.println("Requests log:");
        System.out.println(log);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
