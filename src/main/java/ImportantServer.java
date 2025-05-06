import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import protocols.ComunicationProtocol;
import utils.Log;
import utils.MessageUtils;
import utils.ProtocolUtils;

public class ImportantServer {
    public static void main(String[] args) {
        String expectedResponseMessage = "Process done successfully";
        String errorResponseMessage = "Process failed";

        if (args.length != 1) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP)");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        Log importantMessages = new Log();

        protocol.listen(4000, message -> {
            message = MessageUtils.extract(message, args[0]);
            importantMessages.add(message);
            return true;
        }, expectedResponseMessage, errorResponseMessage);

        while (true) {
            displayInformation(importantMessages.getLog(10));
        }
    }

    private static void displayInformation(String messages) {
        System.out.println("Important messages: ");
        System.out.println(messages);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}