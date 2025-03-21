package ru.project.checktime;

import ru.project.*;

import java.util.ArrayList;

public class CheckTime extends Thread {
    private static final long TIMEOUT = 20 * 60 * 1000;
    private Server server;
    private ClientHandler clientHandler;

    public CheckTime(Server server) {
        this.server = server;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(60*1000);
                long currentTime = System.currentTimeMillis();

                for (ClientHandler client : server.getClients()) {
                    if (currentTime - client.getLastActiveTime() > TIMEOUT) {
                        System.out.println("Отключение неактивного клиента: " + client.getUsername());
                        client.sendMsg("/exitok");
                        server.unsubscribe(client);
                        client.disconnect();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
