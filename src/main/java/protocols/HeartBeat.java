package protocols;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import utils.MessageUtils;

import java.util.HashMap;

public class HeartBeat {
    private ComunicationProtocol protocol;
    private List<Map<String, Object>> serverList;
    private final String SUCCESS_RESPONSE_MESSAGE = "Heartbeat received";
    private final String ERROR_RESPONSE_MESSAGE = "Heartbeat failed";

    public HeartBeat(ComunicationProtocol protocol) {
        this.protocol = protocol;
        this.serverList = new ArrayList<>();
    }

    private void sendHeartBeat(int senderPort, int receiverPort) {
        String message = "heartbeat:" + senderPort;
        protocol.send(receiverPort, message, SUCCESS_RESPONSE_MESSAGE);
    }

    public void startSendingHeartBeats(int senderPort, int receiverPort) {
        new Thread(() -> {
            while (true) {
                sendHeartBeat(senderPort, receiverPort);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void listen(int port) {
        protocol.listen(port, message -> {
            message = MessageUtils.extract(message, protocol.getName());
            if (message.startsWith("heartbeat:")) {
                int serverPort = Integer.parseInt(message.split(":")[1]);
                if (!checkServerStatus(serverPort)) {
                    Map<String, Object> serverInfo = new HashMap<>();
                    serverInfo.put("port", serverPort);
                    serverInfo.put("lastBeat", System.currentTimeMillis());
                    serverList.add(serverInfo);
                } else {
                    serverList.forEach(server -> {
                        if (server.get("port").equals(serverPort)) {
                            server.put("lastBeat", System.currentTimeMillis());
                        }
                    });
                }
                return true;
            }
            return false;
        }, SUCCESS_RESPONSE_MESSAGE, ERROR_RESPONSE_MESSAGE);
        this.startTimeoutMonitoring();
    }

    private void startTimeoutMonitoring() {
        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();
                serverList.removeIf(server -> currentTime - (long) server.get("lastBeat") > 5000);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public List<Map<String, Object>> getServerList() {
        return serverList;
    }

    public boolean checkServerStatus(int port) {
        return serverList.stream().anyMatch(server -> server.get("port").equals(port));
    }
}