package ru.project;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ChatClientGUI extends Application {
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @Override
    public void start(Stage primaryStage) {
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        TextField inputField = new TextField();
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
        if (!message.isEmpty()) {
            chatArea.appendText("Вы: " + message + "\n");
            inputField.clear();
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
                        Platform.runLater(() -> chatArea.appendText(message + "\n"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> chatArea.appendText(e.getMessage() + "\n"));
                }
            }).start();
        } catch (Exception e) {
            chatArea.appendText(e.getMessage() + "\n");
        }
    }
}
