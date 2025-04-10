import protocols.ComunicationProtocol;
import utils.ProtocolUtils;
import protocols.HeartBeat;
import utils.Log;
import java.util.List;
import java.util.Map;
import utils.ReplicatedLog;
import java.util.UUID;

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

        ReplicatedLog replicatedLog = new ReplicatedLog();

        protocol.listen(3500, message -> {
            // if (message.startsWith("SYNC_REQUEST")) {
            // String[] parts = message.split(":");
            // int serverPort = Integer.parseInt(parts[1]);
            // requestsLog.add("SYNC_REQUEST: " + serverPort);
            // // protocol.send(serverPort, "SYNC_REQUEST:" + replicatedLog.getLog(0));
            // return true;
            // }

            String id = UUID.randomUUID().toString();
            replicatedLog.addEntry(id, message);
            requestsLog.add("Request received: " + message);

            List<Map<String, Object>> servers = heartBeat.getServerList();
            if (servers.isEmpty()) {
                requestsLog.add("ERROR: No servers available");
                return false;
            }

            int successCount = 0;
            for (Map<String, Object> server : servers) {
                int serverPort = (int) server.get("port");
                boolean success = protocol.send(serverPort, id + ":PENDING:" + message);
                if (success)
                    successCount++;
            }

            int quorum = (servers.size() / 2) + 1;
            if (successCount >= quorum) {
                replicatedLog.commitEntry(id);
                for (Map<String, Object> server : servers) {
                    int serverPort = (int) server.get("port");
                    protocol.send(serverPort, "COMMIT:" + id);
                }
                requestsLog.add("COMMIT: " + message);
                return true;
            } else {
                requestsLog.add("FAIL: quorum not reached for " + message);
                return false;
            }
        });

        heartBeat.listen(port);
        while (true) {
            displayInformation(
                    heartBeat.getServerList(),
                    requestsLog.getLog(10),
                    replicatedLog.getLog(10));
        }
    }

    private static void displayInformation(
            List<Map<String, Object>> serversUp,
            String requestsLog,
            String replicatedLog) {

        System.out.println("\n\n----------------------");
        System.out.println("Servers Up:");
        serversUp.forEach(server -> {
            long lastBeat = (long) server.get("lastBeat");
            double secondsSinceLastBeat = (System.currentTimeMillis() - lastBeat) / 1000.0;
            System.out.println(server.get("port") + " - " + secondsSinceLastBeat + " seconds ago");
        });

        System.out.println("\n\n----------------------");
        System.out.println("Requests log:");
        System.out.println(requestsLog);

        System.out.println("\n\n----------------------");
        System.out.println("Replicated Log:");
        System.out.println(replicatedLog);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
