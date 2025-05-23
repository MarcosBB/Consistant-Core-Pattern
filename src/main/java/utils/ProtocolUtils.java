package utils;

import protocols.UDP;
import protocols.TCP;
import protocols.ComunicationProtocol;
import protocols.HTTP;

public class ProtocolUtils {

    public static ComunicationProtocol setProtocol(String protocol) {
        switch (protocol.toUpperCase()) {
            case "UDP":
                System.out.println("Using UDP protocol");
                return new UDP();
            case "TCP":
                System.out.println("Using TCP protocol");
                return new TCP();
            case "HTTP":
                System.out.println("Using HTTP protocol");
                return new HTTP();
            default:
                System.out.println("Using default protocol: UDP");
                return new UDP();
        }
    }
}
