package com.sorochuk;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
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
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainFormController implements Initializable {
    @FXML
    public TextArea emails;
    @FXML
    public TextField urls;
    @FXML
    public Label LEmailsCount;
    @FXML
    private ComboBox<String> cb_load_emails;
    @FXML
    private ComboBox<String> cb_save_emails;

    @FXML
    private void parseURL() {
        if (urls.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Заполните поле для ввода URL!");
            alert.setTitle("Предупреждение");
            alert.show();
        } else {
            Set<String> emailList;
            emailList = Parser.parse(urls.getText());

            for (String email : emailList)
                emails.appendText(email + System.lineSeparator());

            getEmailsCount();
        }
    }

    private void saveEmailsToFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file.getAbsolutePath(), false)) {
                String[] emailsList = emails.getText().split("\n");
                for (String email : emailsList) {
                    fileWriter.write(email);
                    fileWriter.write(System.lineSeparator());
                }
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Возникла ошибка!");
                alert.setContentText("Ошибка записи!");
                alert.showAndWait();
            }
        }
    }

    private void loadURLFromFile() {
        FileChooser fileChooser = new FileChooser();
        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file.getAbsolutePath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                urls.appendText(line + " ");
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Возникла ошибка!");
            alert.setContentText("Ошибка чтения!");
            alert.showAndWait();
        }
    }

    private void loadURLFromCSV() throws IOException, CsvValidationException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            urls.appendText(Arrays.toString(nextLine).replaceAll("[\\[\\]]", "") + " ");
        }
    }

    private static XWPFDocument getDocument() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Word files", "*.docx");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        XWPFDocument doc = new XWPFDocument(
                new FileInputStream(
                        new File(file.getAbsolutePath())));
        return doc;
    }

    private void loadURLFromWord() throws IOException {
        XWPFDocument doc = getDocument();
        List<IBodyElement> bodyElements = doc.getBodyElements();

        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph para = (XWPFParagraph) bodyElement;
                urls.appendText(para.getText() + " ");
            } else if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    List<XWPFTableCell> tableCells = row.getTableCells();
                    for (XWPFTableCell tableCell : tableCells) {
                        urls.appendText(tableCell.getText() + " ");
                    }
                }
            }
        }
    }

    private void getEmailsCount() {
        LEmailsCount.setText("Количество: " + String.valueOf(emails.getText().split("\n").length));
    }

    @FXML
    private void changeForm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIMailSendler.fxml"));
        Parent root = loader.load();
        Main.primaryStage.setScene(new Scene(root));
        MailSendlerController controllerEditBook = loader.getController(); //получаем контроллер для второй формы
        controllerEditBook.setEmailsList(emails.getText().replace("\n", " "));// передаем необходимые параметры
        Main.primaryStage.show();
    }

    private void saveCSV() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsolutePath(), true));
        String[] record = emails.getText().split("\n");
        writer.writeNext(record);
        writer.close();
    }

    private void saveWord() throws IOException {
        int rowsCount = emails.getText().split("\n").length;
        String[] emailsList = emails.getText().split("\n");
        int iteration = 1;

        XWPFDocument document = new XWPFDocument();
        XWPFTable table = document.createTable(rowsCount + 1, 1);

        table.getRow(0).getCell(0).setText("Email list table");
        table.getRow(0).getCell(0).setColor("FFCC00");

        for (String email : emailsList) {
            table.getRow(iteration).getCell(0).setText(email);
            table.getRow(iteration++).getCell(0).setColor("CCDAF1");
        }

        FileOutputStream fos = new FileOutputStream(new File("Email list" + ".docx"));
        document.write(fos);
        fos.close();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Успех!");
        alert.setContentText("Успешное сохранение документа!");
        alert.showAndWait();
    }

    @FXML private void saveEmails() {
        try {
            if (cb_save_emails.getValue().equals("TXT"))
                saveEmailsToFile();
            else if (cb_save_emails.getValue().equals("Word"))
                saveWord();
            else if (cb_save_emails.getValue().equals("CSV"))
                saveCSV();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение!");
            alert.setContentText("Выберите способ сохранения из выпадающего списка!");
            alert.showAndWait();
        }
    }

    @FXML private void loadUrls() {
        try {
            if (cb_load_emails.getValue().equals("TXT"))
                loadURLFromFile();
            else if (cb_load_emails.getValue().equals("Word")) {
                loadURLFromWord();
            }
            else if (cb_load_emails.getValue().equals("CSV")) {
                loadURLFromCSV();
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
        cb_load_emails.setItems(load);
        cb_save_emails.setItems(load);
    }
}