package ru.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private Role role;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                System.out.println("Клиент подключился " + socket.getPort());
                //Цикл логина
                while (true) {
                    sendMsg("Для начала работы надо пройти аутентификацию или регистрацию\n" +
                            "Формат команды для аутентификации: /log\n" +
                            "Формат команды для регистрации: /reg\n" +
                            "Для выхода используйте комманду /exit");
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMsg("/exitOK");
                            break;
                        }
                        if (message.equalsIgnoreCase("/log")) {
                            String[] loginData = new String[2];
                            sendMsg("Введите логин: ");
                            loginData[0] = in.readUTF();
                            sendMsg("Введите пароль: ");
                            loginData[1] = in.readUTF();
                            if (server.getAuthenticator()
                                    .authenticate(this, loginData[0], loginData[1])) {
                                break;
                            } else {
                                sendMsg("Ошибка авторизации!");
                                continue;
                            }
                        }
                        if (message.equalsIgnoreCase("/reg")) {
                            String[] regData = new String[3];
                            sendMsg("Введите никнейм: ");
                            regData[0] = in.readUTF();
                            sendMsg("Введите логин: ");
                            regData[1] = in.readUTF();
                            sendMsg("Введите пароль: ");
                            regData[2] = in.readUTF();
                            if (server.getAuthenticator()
                                    .registration(this, regData[0], regData[1], regData[2])) {
                                break;
                            } else {
                                sendMsg("Ошибка регистрации!");
                            }
                        }
                    }
                    else {
                        sendMsg("Неверная команда");
                    }
                }
                //Цикл работы
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equalsIgnoreCase("/exit")) {
                            sendMsg("/exitok");
                            break;
                        } else if (message.startsWith("/w ")) {
                            String[] tokens = message.split(" ", 3);
                            if (tokens.length == 3) {
                                String recipient = tokens[1];
                                String privateMessage = tokens[2];
                                server.sendPrivateMessage(this, recipient, privateMessage);
                            } else {
                                sendMsg("Неверный формат комманды. Используйте /w <никнейм> <сообщение>");
                            }
                        } else if (message.startsWith("/kick ")) {
                            String[] tokens = message.split(" ", 2);
                            if (tokens.length != 2) {
                                sendMsg("Неверный формат команды /kick");
                                continue;
                            }
                            server.kickUser(tokens[1], this);
                        }
                    } else {
                        server.broadcastMessage(username + " : " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Сокет закрыт, клиент на порту " + socket.getPort() + " отключен.");
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMsg(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public Role getRole() {
        return role;
    }
}
