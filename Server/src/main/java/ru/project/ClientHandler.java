package ru.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;
    private static int userCount = 0;


    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        userCount++;
        username = "user" + userCount;

        new Thread(() -> {
            try {
                System.out.println("Клиент подключился " + socket.getPort());
                //Цикл логина
                while (true) {
                    sendMsg("Для начала работы надо пройти аутентификацию. Формат команды /log login password");

                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMsg("/exitOK");
                            break;
                        }
                        String[] tokens = message.split(" ", 3);
                        if (tokens[0].equals("/log")) {
                            if (tokens.length != 3) {
                                sendMsg("Ошибка авторизации");
                                continue;
                            }
                            if (server.getAuthenticator().authenticate(this, tokens[1], tokens[2])) {
                                break;
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
                        }
                    } else {
                        server.broadcastMessage(username + " : " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}
