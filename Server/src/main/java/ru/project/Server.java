package ru.project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private Authenticator authenticator;

    public Server(int port) {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
        authenticator = new inMemoryAuthenticator(this);
    }

    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            authenticator.initialize();
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: "+ clientHandler.getUsername());
    }

    public void broadcastMessage(String message){
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }
    public void sendPrivateMessage(ClientHandler sender, String recipient, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMsg(sender.getUsername() + ": " + message);
                sender.sendMsg(recipient + ": " + message);
                return;
            }
        }
        sender.sendMsg("Ошибка: пользователь с ником " + recipient + " не найден.");
    }
    public boolean isUserLoggedIn(String username){
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    public void kickUser(String usernameToKick, ClientHandler adminHandler) {
        if (adminHandler.getRole().equals(Role.ADMIN)) {
            for (ClientHandler client : clients) {
                if (client.getUsername().equals(usernameToKick)) {
                    client.sendMsg("/exit");
                    client.disconnect();
                    broadcastMessage("Пользователь " + usernameToKick + " был отключён администратором");
                    return;
                }
            }
            adminHandler.sendMsg("Ошибка. Пользователь с ником " + usernameToKick + " не найден");
        } else {
            adminHandler.sendMsg("Недостаточно прав");
        }
    }
}
