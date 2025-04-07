package protocols;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import java.util.function.Consumer;

public class UDP implements ComunicationProtocol {

    @Override
    public void listen(int port, Consumer<String> processPayload) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Listening on port " + port);

            executor.execute(() -> {
                try {
                    while (true) {
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        executor.execute(() -> {
                            String message = new String(packet.getData(), 0, packet.getLength());
                            processPayload.accept(message);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(int port, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), port);
            socket.send(packet);
            System.out.println("UDP message sent to port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
