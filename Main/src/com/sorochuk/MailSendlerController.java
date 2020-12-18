package com.sorochuk;

import com.sorochuk.parser.Parser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailSendlerController implements Initializable {
    @FXML private TextField recipientField;
    @FXML private TextField themeField;
    @FXML private TextArea bodyMailField;
    @FXML private TextField sendlerField;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> cb_parse;
    private String emailsList;

    @FXML private void openFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file.getAbsolutePath()), StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null) {
                recipientField.appendText(line + " ");
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Возникла ошибка!");
            alert.setContentText("Ошибка чтения!");
            alert.showAndWait();
        }
    }

    private void sendEmail(String recepient, String account, String pass, String sub, String text) throws MessagingException {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        final String myAccountEmail = account;
        final String myPassword = pass;

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, myPassword);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recepient, sub, text);

        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipient, String sub, String text) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(sub);
            message.setText(text);
            return message;
        }
        catch (Exception ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @FXML private void BtnSend() throws MessagingException {
        String[] emails = recipientField.getText().split(" ");

        if (recipientField.getText().equals("") || sendlerField.getText().equals("") || passField.getText().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Возникла ошибка!");
            alert.setContentText("Заполните поля 'Получатель', 'Тема письма', 'Отправитель'!");
            alert.showAndWait();
        }
        else
        {
            for (String email : emails)
                sendEmail(email, sendlerField.getText(), passField.getText(), themeField.getText(), bodyMailField.getText());
            //"snadatel@gmail.com", "72bc3G389Aalol"
        }
    }

    public void setEmailsList(String otherList) {
        emailsList = otherList;
        recipientField.appendText(emailsList);
    }

    @FXML private void changeForm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm2.fxml"));
        Parent root = loader.load();
        Main.primaryStage.setScene(new Scene(root));
        Main.primaryStage.show();
    }

    @FXML private void loadEmails() {
        try {
            if (cb_parse.getValue().equals("TXT"))
                openFile();
            else if (cb_parse.getValue().equals("Word")) {
                Set<String> emails = Parser.parseWord();

                for (String elem : emails)
                    recipientField.appendText(elem + " ");
            }
            else if (cb_parse.getValue().equals("CSV")) {
                Set<String> emails = Parser.parseCSV();

                for (String elem : emails)
                    recipientField.appendText(elem + " ");
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение!");
            alert.setContentText("Выберите способ загрузки из выпадающего списка!");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> load = FXCollections.observableArrayList("TXT", "Word", "CSV");
        cb_parse.setItems(load);
    }
}