package com.sorochuk.parser;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sorochuk.Main;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.EmailValidator;
/**
 * Класс, отвечающий за функционал парсера
 * @author Сорочук Евгений и Левкович Алексей
 * @version 1.0
 */
public class Parser {

    /** Rexeg для поиска email */
    private final static String RFC5322 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    /**
     * Метод для парсинга из интернет страниц
     * @param urls - url страниц
     * @return возвращает множнество email адресов
     */
    public static Set<String> parse(String urls) {
        String[] URLList = urls.split(" ");
        Set<String> emails = new HashSet<String>();

        for (String url : URLList) {
            try {
                Document document = Jsoup.connect(url).get();
                String strDocument = document.toString();

                Matcher m = Pattern.compile(RFC5322).matcher(strDocument);
                while (m.find()) {
                    EmailValidator validator = EmailValidator.getInstance();
                    if (validator.isValid(m.group()))
                        emails.add(m.group());
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка!");
                alert.show();
                return null;
            }
        }
        return emails;
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
    /**
     * Метод для парсинга email из word документа
     * @return возвращает множнество email адресов
     */
    public static Set<String> parseWord() throws IOException {
        Set<String> emails = new HashSet<String>();
        XWPFDocument doc = getDocument();
        List<IBodyElement> bodyElements = doc.getBodyElements();

        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph para = (XWPFParagraph) bodyElement;
                Matcher m = Pattern.compile(RFC5322).matcher(para.getText());
                while (m.find()) {
                    emails.add(m.group());
                }
            } else if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    List<XWPFTableCell> tableCells = row.getTableCells();
                    for (XWPFTableCell tableCell : tableCells) {
                        Matcher m = Pattern.compile(RFC5322).matcher(tableCell.getText());
                        while (m.find()) {
                            emails.add(m.group());
                        }
                    }
                }
            }
        }
        return emails;
    }
    /**
     * Метод для парсинга email из файла csv
     * @return возвращает множнество email адресов
     */
    public static Set<String> parseCSV() throws IOException, CsvValidationException {
        Set<String> emails = new HashSet<String>();

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage s = Main.getPrimaryStage();
        File file = fileChooser.showOpenDialog(s);

        CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            Matcher m = Pattern.compile(RFC5322).matcher(Arrays.toString(nextLine));
            while (m.find()) {
                emails.add(m.group());
            }
        }
        return emails;
    }
}