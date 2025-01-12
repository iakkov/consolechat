package ru.project;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class inMemoryAuthenticator implements Authenticator {
    public class User {
        private String username;
        private String login;
        private String password;

        public User(String username, String login, String password) {
            this.username = username;
            this.login = login;
            this.password = password;
        }
    }
    private List<User> users;
    private Server server;
    public inMemoryAuthenticator(Server server) {
        users = new CopyOnWriteArrayList<>();
        this.server = server;
        users.add(new User("admin", "admin", "admin"));
    }
    @Override
    public void initialize() {
        System.out.println("Инициализация");
    }
    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String username = server.getDatabaseManager().getUsernameByLoginAndPassword(login, password);
        if (username == null) {
            clientHandler.sendMsg("Неверный логин и/или пароль");
            return false;
        }
        if (server.isUserLoggedIn(username)) {
            clientHandler.sendMsg("Данный пользователь уже в сети");
            return false;
        }
        clientHandler.setUsername(server.getDatabaseManager().authenticate(login, password));
        if (server.getDatabaseManager().getUserRole(clientHandler.getUsername()).equals(Role.ADMIN)) {
            clientHandler.setRole(Role.ADMIN);
        } else {
            clientHandler.setRole(Role.USER);
        }
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/authOK " + username);
        return true;
    }
    @Override
    public boolean registration(ClientHandler clientHandler, String username, String login, String password) {
        if (login.length() < 3 || password.length() < 3 || username.length() < 3) {
            clientHandler.sendMsg("Логин 3+ символа,  пароль 3+ символа, имя пользователя 3+ символа");
            return false;
        }
        if (server.getDatabaseManager().isLoginAlreadyExist(login)) {
            clientHandler.sendMsg("Такой логин уже используется");
            return false;
        }
        if (server.getDatabaseManager().isUsernameAlreadyExist(username)) {
            clientHandler.sendMsg("Такое имя пользователя уже используется");
            return false;
        }
        server.getDatabaseManager().registerUser(username, login, password, Role.USER);
        users.add(new User(username, login, password));
        clientHandler.setUsername(username);
        if (clientHandler.getUsername().equals("admin")) {
            clientHandler.setRole(Role.ADMIN);
        } else {
            clientHandler.setRole(Role.USER);
        }
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/regOK " + username);
        return true;
    }
}
