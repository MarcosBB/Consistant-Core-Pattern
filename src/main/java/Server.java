import protocols.ComunicationProtocol;
import protocols.HeartBeat;
import utils.ProtocolUtils;
import utils.ReplicatedLog;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP), server port and gateway port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        int gatewayPort = Integer.parseInt(args[2]);
        ReplicatedLog replicatedLog = new ReplicatedLog();

        protocol.listen(port, message -> {
            if (message.startsWith("COMMIT:")) {
                String id = message.replace("COMMIT:", "");
                replicatedLog.commitEntry(id);
                return true;
            }

            // if (message.startsWith("SYNC_REQUEST:")) {
            // String[] parts = message.replace("SYNC_REQUEST:", "").split(":");
            // Map<String, String> entry = new HashMap<>();
            // entry.put("id", parts[0]);
            // entry.put("state", parts[1]);
            // entry.put("message", parts[2]);
            // replicatedLog.getEntries().add(entry);
            // return true;
            // }

            String[] parts = message.split(":", 3);
            Map<String, String> entry = new HashMap<>();
            entry.put("id", parts[0]);
            entry.put("state", parts[1]);
            entry.put("message", parts[2]);
            replicatedLog.getEntries().add(entry);
            return true;
        });

        HeartBeat heartBeat = new HeartBeat(protocol);
        heartBeat.startSendingHeartBeats(port, gatewayPort);

        // protocol.send(gatewayPort, "SYNC_REQUEST:" + port);

        while (true) {
            System.out.println("Replicated Log:");
            System.out.println(replicatedLog.getLog(10));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.print("\033[H\033[2J");
            System.out.flush();
        }

    }
}
