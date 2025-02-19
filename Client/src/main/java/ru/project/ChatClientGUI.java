package ru.project;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatClientGUI extends Application {
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;

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
}
