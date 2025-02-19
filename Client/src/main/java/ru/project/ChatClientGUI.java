package ru.project;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientGUI extends Application {
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isBanned = false;

    @Override
    public void start(Stage primaryStage) {
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Введите сообщение...");

        Button sendButton = new Button("Отправить");
        sendButton.setOnAction(e -> sendMessage());

        VBox root = new VBox(10, inputField, sendButton);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Чат-клиент");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && !isBanned) {
            try {
                out.writeUTF(message); // Отправляем на сервер
                inputField.clear(); // Очищаем поле
            } catch (IOException e) {
                chatArea.appendText("Ошибка отправки сообщения\n");
            }
        } else if (isBanned) {
            chatArea.appendText("Вы не можете писать сообщения, так как заблокированы.\n");
        }
    }
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            chatArea.appendText("Подключено к серверу\n");

            new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readUTF();
                        Platform.runLater(() -> processServerMessage(message));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> chatArea.appendText(e.getMessage() + "\n"));
                }
            }).start();
        } catch (Exception e) {
            chatArea.appendText(e.getMessage() + "\n");
        }
    }
    private void processServerMessage(String message) {
        if (message.startsWith("/")) {
            if (message.equalsIgnoreCase("/exitok")) {
                disconnect();
            } else if (message.startsWith("/authOK ")) {
                chatArea.appendText("Авторизация успешна! Ваш ник: " + message.split(" ")[1] + "\n");
            } else if (message.startsWith("/regOK ")) {
                chatArea.appendText("Регистрация успешна! Ваш ник: " + message.split(" ")[1] + "\n");
            } else if (message.equalsIgnoreCase("/banok")) {
                isBanned = true;
                chatArea.appendText("Вы были заблокированы администратором.\n");
            } else if (message.equalsIgnoreCase("/unbanok")) {
                isBanned = false;
                chatArea.appendText("Вы теперь разбанены и можете писать сообщения.\n");
            }
        } else {
            chatArea.appendText(message + "\n"); // Обычное сообщение
        }
    }
    private void disconnect() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            chatArea.appendText("Отключено от сервера\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
