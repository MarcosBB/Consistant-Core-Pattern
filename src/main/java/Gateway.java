
import protocols.ComunicationProtocol;
import utils.ProtocolUtils;
import protocols.HeartBeat;

public class Gateway {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP) and a port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        HeartBeat heartBeat = new HeartBeat(protocol);

        heartBeat.listen(port);
        while (true) {
            heartBeat.getServerList().forEach(server -> {
                long lastBeat = (long) server.get("lastBeat");
                double secondsSinceLastBeat = (System.currentTimeMillis() - lastBeat) / 1000.0;
                System.out.println(
                        "Server " + server.get("port") + " is alive - last heartbeat was " + secondsSinceLastBeat
                                + " seconds ago");
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
}
