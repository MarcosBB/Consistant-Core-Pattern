import protocols.ComunicationProtocol;
import protocols.HeartBeat;
import utils.MessageUtils;
import utils.ProtocolUtils;
import utils.ReplicatedLog;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static void main(String[] args) {
        String expectedResponseMessage = "Process done successfully";
        String errorResponseMessage = "Process failed";

        if (args.length != 3) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP), server port and gateway port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        int gatewayPort = Integer.parseInt(args[2]);
        ReplicatedLog replicatedLog = new ReplicatedLog();
        HeartBeat heartBeat = new HeartBeat(protocol);

        protocol.listen(port, message -> {
            message = MessageUtils.extract(message, args[0]);

            if (message.startsWith("COMMIT:")) {
                String id = message.replace("COMMIT:", "");
                replicatedLog.commitEntry(id);
                return true;
            }

            if (message.startsWith("SYNC_REQUEST:")) {
                String allLog = message.replace("SYNC_REQUEST:", "");
                replicatedLog.importLog(allLog);
                return true;
            }

            String[] parts = message.split(":", 3);
            Map<String, String> entry = new HashMap<>();
            entry.put("id", parts[0]);
            entry.put("state", parts[1]);
            entry.put("message", parts[2]);

            if (entry.get("message").startsWith("!!!IMPORTANT!!!")) {
                String messageToSend = port + " : " + entry.get("message").replace("!!!IMPORTANT!!!", "");
                boolean status = protocol.send(4000, messageToSend, expectedResponseMessage);
                if (status == false) {
                    return false;
                }
            }

            replicatedLog.getEntries().add(entry);
            return true;
        }, expectedResponseMessage, errorResponseMessage);

        heartBeat.startSendingHeartBeats(port, gatewayPort);

        protocol.send(3500, "SYNC_REQUEST:" + port, expectedResponseMessage);

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
