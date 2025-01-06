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
        users.add(new User("element1", "element", "123456"));
        users.add(new User("yellowFish", "yellowFish", "fishing2"));
    }
    @Override
    public void initialize() {
        System.out.println("Инициализация");
    }
    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }

    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String username = getUsernameByLoginAndPassword(login, password);
        if (username == null) {
            clientHandler.sendMsg("Неверный логин и/или пароль");
            return false;
        }
        if (server.isUserLoggedIn(username)) {
            clientHandler.sendMsg("Данный пользователь уже в сети");
            return false;
        }
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/authOK " + username);
        return true;
    }
    private boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }
    private boolean isUsernameAlreadyExist(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean registration(ClientHandler clientHandler, String username, String login, String password) {
        if (login.length() < 3 || password.length() < 3 || username.length() < 3) {
            clientHandler.sendMsg("Логин 3+ символа,  пароль 3+ символа, имя пользователя 3+ символа");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMsg("Такой логин уже используется");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMsg("Такое имя пользователя уже используется");
            return false;
        }
        users.add(new User(username, login, password));
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/regOK " + username);
        return true;
    }
}
