package protocols;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import java.util.function.Function;

public class UDP implements ComunicationProtocol {

    @Override
    public void listen(int port, Function<String, Boolean> processPayload, String successResponseMessage,
            String errorResponseMessage) {
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
                            try {
                                String message = new String(packet.getData(), 0, packet.getLength());
                                boolean processed = processPayload.apply(message);

                                String responseMessage;
                                if (processed) {
                                    responseMessage = successResponseMessage;
                                } else {
                                    responseMessage = errorResponseMessage;
                                }
                                byte[] responseBuffer = responseMessage.getBytes();
                                DatagramPacket responsePacket = new DatagramPacket(
                                        responseBuffer,
                                        responseBuffer.length,
                                        packet.getAddress(),
                                        packet.getPort());
                                socket.send(responsePacket);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
    public boolean send(int port, String message, String expectedResponseMessage) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(1000); // Set timeout to 1 second
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName("localhost"),
                    port);
            socket.send(packet);

            // Wait for a response
            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);
            String responseMessage = new String(responsePacket.getData(), 0, responsePacket.getLength());
            return responseMessage.equals(expectedResponseMessage);

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Response timed out.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getName() {
        return "UDP";
    }
}
