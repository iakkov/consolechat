package ru.project;

public interface Authenticator {
    void initialize();
    boolean authenticate(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String username, String login, String password);
}
