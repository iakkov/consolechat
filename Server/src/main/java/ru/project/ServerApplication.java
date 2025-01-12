package ru.project;

import ru.project.database.DatabaseManager;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8189, new DatabaseManager()).start();
    }
}
