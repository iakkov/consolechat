package ru.project;

import ru.project.checktime.CheckTime;
import ru.project.database.DatabaseManager;
import ru.project.rooms.Rooms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private Authenticator authenticator;
    private final DatabaseManager databaseManager;
    private final Rooms rooms = new Rooms();

    public Server(int port, DatabaseManager databaseManager) {
        this.port = port;
        this.databaseManager = databaseManager;
        clients = new CopyOnWriteArrayList<>();
        authenticator = new inMemoryAuthenticator(this);

        new CheckTime(this).start();
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
        broadcastMessage("В чат зашел: " + clientHandler.getUsername());
        System.out.println("[SERVER] Пользователь " + clientHandler.getUsername() + " подключился.");
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: "+ clientHandler.getUsername());
        System.out.println("[SERVER] Клиент " + clientHandler.getUsername() + " отключён.");
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
                    client.sendMsg("/exitok");
                    broadcastMessage("Пользователь " + usernameToKick + " был отключён администратором");
                    return;
                }
            }
            adminHandler.sendMsg("Ошибка. Пользователь с ником " + usernameToKick + " не найден");
        } else {
            adminHandler.sendMsg("Недостаточно прав");
        }
    }
    public void banUser(String usernameToBan, ClientHandler adminHandler) {
        if (adminHandler.getRole().equals(Role.ADMIN)) {
            for (ClientHandler client : clients) {
                if (client.getUsername().equals(usernameToBan)) {
                    client.sendMsg("/banok");
                    broadcastMessage("Пользователь " + usernameToBan + " был забанен администратором");
                    return;
                }
            }
            adminHandler.sendMsg("Ошибка. Пользователь с ником " + usernameToBan + " не найден");
        } else {
            adminHandler.sendMsg("Недостаточно прав");
        }
    }
    public void unbanUser(String usernameToUnban, ClientHandler adminHandler) {
        if (adminHandler.getRole().equals(Role.ADMIN)) {
            for (ClientHandler client : clients) {
                if (client.getUsername().equals(usernameToUnban)) {
                    client.sendMsg("/unbanok");
                    broadcastMessage("Пользователь " + usernameToUnban + " был разбанен администратором");
                    return;
                }
            }
            adminHandler.sendMsg("Ошибка. Пользователь с ником " + usernameToUnban + " не найден");
        } else {
            adminHandler.sendMsg("Недостаточно прав");
        }
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public String getOnlineUsers() {
        StringBuilder sb = new StringBuilder("Сейчас онлайн:\n");
        for (ClientHandler client : clients) {
            sb.append(client.getUsername()).append("\n");
        }
        return sb.toString();
    }
    public List<ClientHandler> getClients() {
        return this.clients;
    }
    public void createRoom(String roomName, ClientHandler owner) {
        rooms.createRoom(roomName, owner);
    }

    public void joinRoom(String roomName, ClientHandler client) {
        rooms.joinRoom(roomName, client);
    }

    public void exitRoom(ClientHandler client) {
        rooms.exitRoom(client);
    }

    public void sendToRoom(String roomName, String message) {
        rooms.sendToRoom(roomName, message);
    }

    public boolean isUserInRoom(ClientHandler client) {
        return rooms.isUserInRoom(client);
    }
}
