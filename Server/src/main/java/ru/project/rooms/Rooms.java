package ru.project.rooms;

import ru.project.ClientHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Rooms {
    private final Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();

    public void createRoom(String roomName, ClientHandler owner) {
        if (rooms.containsKey(roomName)) {
            owner.sendMsg("Комната с таким именем уже существует.");
            return;
        }
        if (owner.getCurrentRoom() != null) {
            owner.sendMsg("Вы уже создали комнату: " + owner.getCurrentRoom());
            return;
        }
        rooms.put(roomName, new CopyOnWriteArrayList<>(List.of(owner)));
        owner.setCurrentRoom(roomName);
        owner.sendMsg("Вы создали комнату: " + roomName);
    }

    public void joinRoom(String roomName, ClientHandler client) {
        if (!rooms.containsKey(roomName)) {
            client.sendMsg("Комната " + roomName + " не существует.");
            return;
        }
        if (client.getCurrentRoom() != null) {
            client.sendMsg("Вы уже находитесь в комнате: " + client.getCurrentRoom());
            return;
        }
        rooms.get(roomName).add(client);
        client.setCurrentRoom(roomName);
        sendToRoom(roomName, client.getUsername() + " присоединился к комнате.");
    }

    public void exitRoom(ClientHandler client) {
        String roomName = client.getCurrentRoom();
        if (roomName == null) {
            client.sendMsg("Вы не находитесь в комнате.");
            return;
        }
        rooms.get(roomName).remove(client);
        client.setCurrentRoom(null);
        sendToRoom(roomName, client.getUsername() + " покинул комнату.");
        client.sendMsg("Вы вышли из комнаты.");
    }
    public void sendToRoom(String roomName, String message) {
        if (!rooms.containsKey(roomName)) return;
        for (ClientHandler client : rooms.get(roomName)) {
            client.sendMsg(message);
        }
    }

    public boolean isUserInRoom(ClientHandler client) {
        return client.getCurrentRoom() != null;
    }
}
