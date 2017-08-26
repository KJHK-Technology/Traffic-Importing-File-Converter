/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trafficimportfileconverter2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.apache.commons.io.FilenameUtils;

/**
 * FXML Controller class
 *
 * @author Paul
 */
public class Main2Controller implements Initializable {

    @FXML
    private Label lblOutputLoc;
    @FXML
    private Label lblInputFile;
    @FXML
    private Label lblInfo;
    @FXML
    private Button btnSelect;
    @FXML
    private Button btnOutputLocation;
    private final ObjectProperty<File> outputDir = new SimpleObjectProperty<>();
    private final ObjectProperty<File> inputFile = new SimpleObjectProperty<>();
    @FXML
    private Button btnGo;
    private Preferences prefs;
    private final static String INPUT_DIR_LOC = "INPUT_DIR_LOC";
    private final static String OUTPUT_DIR_LOC = "OUTPUT_DIR_LOC";
    private final static String EXTENSION = "EXTENSION";
    private boolean lineBreaksMatter = true;
    @FXML
    private TextArea txtAreaOutput;

    public File getInputFile() {
        return inputFile.get();
    }

    public void setInputFile(File value) {
        inputFile.set(value);
    }

    public ObjectProperty inputFileProperty() {
        return inputFile;
    }

    FileChooser filechooser;
    DirectoryChooser dirchooser;
    @FXML
    private AnchorPane BigPane;

    public File getOutputDir() {
        return outputDir.get();
    }

    public void setOutputDir(File value) {
        outputDir.set(value);
    }

    public ObjectProperty outputDirProperty() {
        return outputDir;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        prefs = Preferences.userNodeForPackage(this.getClass());

        dirchooser = new DirectoryChooser();

        outputDir.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                lblOutputLoc.setText(newValue.getAbsolutePath());
                prefs.put(OUTPUT_DIR_LOC, newValue.getAbsolutePath());
            }
        });
        setOutputDir(new File(prefs.get(OUTPUT_DIR_LOC, "./")));
        dirchooser.setInitialDirectory(getOutputDir());

        filechooser = new FileChooser();
        filechooser.setInitialDirectory(new File(prefs.get(INPUT_DIR_LOC, "./")));
        setExtension(prefs.get(EXTENSION, ".gen"));
        inputFile.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                lblInputFile.setText(newValue.getAbsolutePath());
                prefs.put(INPUT_DIR_LOC, newValue.getParent());
            }
        });
        txtAreaOutput.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                    Object newValue) {
                txtAreaOutput.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });
        filechooser.getExtensionFilters().add(new ExtensionFilter("Tab Separated Values", "*.tsv", "*.TSV"));

        autoSearch();
    }

    @FXML

    private void onBtnSelect(ActionEvent event) {
        File sel = filechooser.showOpenDialog(BigPane.getScene().getWindow());
        setInputFile(sel);
    }

    @FXML
    private void onBtnOutputLocation(ActionEvent event) {

        File selectedDirectory = dirchooser.showDialog(BigPane.getScene().getWindow());
        setOutputDir(selectedDirectory);

    }

    @FXML
    private void onBtnGo(ActionEvent event) {
        if (null == outputDir.getValue()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Select Output Directory!");
            alert.showAndWait();
            return;
        }
        if (null == inputFile.getValue()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Select Input File!");
            alert.showAndWait();
            return;
        }

        threadedHandledFileConversion(getInputFile());
    }

    private void autoSearch() {
        new Thread(() -> {
            File[] files = filechooser.getInitialDirectory().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String x = FilenameUtils.getExtension(pathname.getAbsolutePath());
                    return x.compareToIgnoreCase("tsv") == 0;
                }
            });

            File mostRecent = Arrays.stream(files).max((x, y) -> Long.compare(x.lastModified(), y.lastModified())).orElse(null);
            if (mostRecent == null) {
                return;
            }
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("We found the following TSV file in your default location:\n\""
                        + mostRecent.getName() + "\"\n"
                        + "Modified: " + new Date(mostRecent.lastModified()) + "\n"
                        + "Would you like to quick use?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.isPresent() && answer.get().getButtonData().equals(ButtonData.OK_DONE)) {
                    System.out.println("THEY HAVE CHOSEN TO SHORT CIRCUIT");
                    System.out.println(answer.get());
                    setInputFile(mostRecent);
                    onBtnGo(null);
                } else {
                    System.out.println("They have chosen the path of darkness. Manualness");
                }
            });

        }).start();
    }

    private void threadedHandledFileConversion(File file) {
        new Thread(() -> hanldeFileConversion(file)).start();
    }

    private void hanldeFileConversion(File file) {
        BufferedReader TSVFile = null;
        try {
            TSVFile = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String dataRow = TSVFile.readLine();
            while (dataRow != null) {
                String[] row = dataRow.split("\t");
                System.out.println(row[1] + "Will be:\n" + row[2]);
                handleImport(row);
                dataRow = TSVFile.readLine();
            }
            TSVFile.close();

            System.out.println();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                TSVFile.close();
            } catch (IOException ex) {
                Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private final StringProperty extension = new SimpleStringProperty();

    public String getExtension() {
        return extension.get();
    }

    public void setExtension(String value) {
        extension.set(value);
    }

    public StringProperty extensionProperty() {
        return extension;
    }

    private TrafficDay parseRow(String[] row) {
        TrafficDay t = new TrafficDay();
        t.setDay(LocalDate.now());
        String dateStr = "";
        for (String string : row) {
            if (string.matches("\\d\\d-\\d\\d-\\d\\d")) {
                dateStr = string;
                break;
            }
        }
        //yy-mm-dd.
        String[] parts = dateStr.split("-");
        t.getDay().withYear(2000 + Integer.parseInt(parts[0]));
        t.getDay().withDayOfMonth(Integer.parseInt(parts[1]));
        t.getDay().withMonth(Integer.parseInt(parts[2]));
        t.setContent(row[row.length-1]);
        return t;
    }

//    private void handleImport(TrafficDay td){
//         chkAlreadyImported(row[0], (fileName)
//                -> {
//            //on yes, let's import
//            try {
//                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
//                if (lineBreaksMatter) {
//                    writer.write(mkLineBreaks(row[2]));
//                } else {
//                    writer.write(row[2]);
//
//                }
//                writer.close();
//                String newFileName = getOutputDir().getAbsolutePath() + java.io.File.separator + fileName + getExtension();
//                if (rename(fileName, newFileName)) {
//                    log("Success writing: " + newFileName);
//                } else {
//                    log("failed to write:" + newFileName);
//                };
//            } catch (IOException e) {
//                e.printStackTrace();
//                // do something
//            }
//        });
//        
//    }
    private void handleImport(String[] row) {

        chkAlreadyImported(row[0], (fileName)
                -> {
            //on yes, let's import
            try {
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
                if (lineBreaksMatter) {
                    writer.write(mkLineBreaks(row[2]));
                } else {
                    writer.write(row[2]);

                }
                writer.close();
                String newFileName = getOutputDir().getAbsolutePath() + java.io.File.separator + fileName + getExtension();
                if (rename(fileName, newFileName)) {
                    log("Success writing: " + newFileName);
                } else {
                    log("failed to write:" + newFileName);
                };
            } catch (IOException e) {
                e.printStackTrace();
                // do something
            }
        });

    }

    public void log(String str) {
        Platform.runLater(() -> {
            txtAreaOutput.appendText("\n" + str);
        });
    }

    public static boolean rename(String oldFileName, String newFileName) {
        new File(newFileName).delete();
        File oldFile = new File(oldFileName);
        return oldFile.renameTo(new File(newFileName));
    }

    private void chkAlreadyImported(String datestr, Consumer<String> onDoImport) {
        //the date/file name
        File[] files = getOutputDir().listFiles();
        String fileName = Arrays.stream(datestr.split("-")).collect(Collectors.joining());
        Optional<File> opSome = Arrays.stream(files).parallel().filter(x -> x.getName().contains(fileName)).findAny();
        if (opSome.isPresent()) {
            //oh snap, looks like one's here already!
            UI(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Re-import?");
                alert.setContentText("Hey there, just thought I'd let you know "
                        + "that you've already imported a file for this day: "
                        + datestr + ". Because I found this file: " + opSome.get().getName()
                        + "\nAre you sure you want to do this?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.isPresent() && answer.get().getButtonData().equals(ButtonData.OK_DONE)) {
                    onDoImport.accept(fileName);
                } else {
                    log("Skipping: " + fileName);
                }

                return;
            });
        } else {
            //then of course we can import.
            onDoImport.accept(fileName);
        }
    }

    public void UI(Runnable f) {
        Platform.runLater(f);
    }

    private String mkLineBreaks(String string) {

        return Arrays.stream(string.split("(?=\\d\\d:\\d\\d:\\d\\d)")).collect(Collectors.joining("\n"));
    }

}
