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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.converter.LocalDateTimeStringConverter;
import javax.swing.event.DocumentEvent;
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
    @FXML
    private DatePicker date1;
    @FXML
    private DatePicker date2;
    @FXML
    private DatePicker date3;
    @FXML
    private DatePicker date4;
    @FXML
    private DatePicker date5;
    @FXML
    private DatePicker date6;
    @FXML
    private DatePicker date7;
    @FXML
    private CheckBox chkDay1;
    @FXML
    private CheckBox chkDay2;
    @FXML
    private CheckBox chkDay3;
    @FXML
    private CheckBox chkDay4;
    @FXML
    private CheckBox chkDay5;
    @FXML
    private CheckBox chkDay6;
    @FXML
    private CheckBox chkDay7;
    @FXML
    private CheckBox chkAutoInc;

    public CheckBox[] dateChecks = new CheckBox[7];
    public DatePicker[] datePickers = new DatePicker[7];
    private Label[] dayLabels = new Label[7];
    @FXML
    private CheckBox chkSelectAll;
    @FXML
    private Label lbl1;
    @FXML
    private Label lbl2;
    @FXML
    private Label lbl3;
    @FXML
    private Label lbl4;
    @FXML
    private Label lbl5;
    @FXML
    private Label lbl6;
    @FXML
    private Label lbl7;
    @FXML
    private Label lblSelectAll;
    private ArrayList<LocalDate> dates = new ArrayList<>(7);
    private ArrayList<String> data = new ArrayList<>(7);
    private Thread myThread;
    private ArrayList<String> myfiles = new ArrayList<>();

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

        initDateArrays();

        dirchooser = new DirectoryChooser();

        outputDir.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                lblOutputLoc.setText(newValue.getAbsolutePath());
                prefs.put(OUTPUT_DIR_LOC, newValue.getAbsolutePath());
            }
        });
        setOutputDir(safeFileSet(new File(prefs.get(OUTPUT_DIR_LOC, "./"))));
        try {
            dirchooser.setInitialDirectory(getOutputDir());
        } catch (Exception e) {
            System.out.println("Error setting init dir: " + e);
            e.printStackTrace();
        }

        filechooser = new FileChooser();
        try {
            filechooser.setInitialDirectory(new File(prefs.get(INPUT_DIR_LOC, "./")));
        } catch (Exception e) {
            System.out.println("Error setting init directory of file chooser: " + e);
            e.printStackTrace();
        }
        setExtension(prefs.get(EXTENSION, ".gen"));

        /*
        Save in preferences default getting file location every time
        the input file is reselected.
         */
        inputFile.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                lblInputFile.setText(newValue.getAbsolutePath());
                prefs.put(INPUT_DIR_LOC, newValue.getParent());
                reloadFileData(newValue);
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

    private void initDateArrays() {
        //populate arrays;
        datePickers[0] = date1;
        datePickers[1] = date2;
        datePickers[2] = date3;
        datePickers[3] = date4;
        datePickers[4] = date5;
        datePickers[5] = date6;
        datePickers[6] = date7;

        dateChecks[0] = chkDay1;
        dateChecks[1] = chkDay2;
        dateChecks[2] = chkDay3;
        dateChecks[3] = chkDay4;
        dateChecks[4] = chkDay5;
        dateChecks[5] = chkDay6;
        dateChecks[6] = chkDay7;

        dayLabels[0] = lbl1;
        dayLabels[1] = lbl2;
        dayLabels[2] = lbl3;
        dayLabels[3] = lbl4;
        dayLabels[4] = lbl5;
        dayLabels[5] = lbl6;
        dayLabels[6] = lbl7;

        //set property to icrement all others if selected.
        datePickers[0].setOnAction(new EventHandler() {
            public void handle(Event t) {
                System.out.println("I've been touched!!!");
                if (chkAutoInc.isSelected() && datePickers[0].getValue() != null) {
                    for (int i = 1; i < dateChecks.length; i++) {
                        datePickers[i].setValue(datePickers[i - 1].getValue().plusDays(1));                        
                    }
                }
            }
        });

        //if the checkbox is checked after setting the date, trigger event.
        chkAutoInc.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    datePickers[0].fireEvent(new ActionEvent());
                    datePickers[0].setValue(datePickers[0].getValue());
                }
            }
        });

        for (int i = 0; i < datePickers.length; i++) {
            final int x = i;
            datePickers[i].valueProperty().addListener(new ChangeListener<LocalDate>() {
                @Override
                public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                    dates.set(x, newValue);
                    dayLabels[x].setText(newValue.getDayOfWeek().name());
                    datePickers[x].setStyle("-fx-background-color: red");
                }
            });
        }

        //set select all functionality
        chkSelectAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                for (int i = 0; i < dateChecks.length; i++) {
                    dateChecks[i].setSelected(newValue);
                }
                lblSelectAll.setText(newValue ? "Uncheck to: Unselect All" : "Check to: Select All");
            }
        });

        //default to include everything.
        chkSelectAll.setSelected(true);

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

    private void reloadFileData(File newValue) {
        loadInInfo(newValue);
        //display it all.
        for (int i = 0; i < datePickers.length; i++) {
            try {
                datePickers[i].setValue(dates.get(i));
            } catch (Exception e) {
                System.out.println("failed to set datePicker:" + i + " " + e);
                e.printStackTrace();
            }
        }
    }

    private void loadInInfo(File file) {
        BufferedReader TSVFile = null;
        //get rid of any dates and data already there.
        dates.clear();
        data.clear();
        try {
            TSVFile = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String dataRow = TSVFile.readLine();
            while (dataRow != null) {
                String[] row = dataRow.split("\t");
                System.out.println(row[1] + "Will be:\n" + row[2]);
                dates.add(LocalDate.parse("20" + row[0]));
                data.add(getData(row));
                dataRow = TSVFile.readLine();
            }
            TSVFile.close();
            log("Days in file: " + dates.size());
            if (dates.size() > datePickers.length) {
                log("WARNING: More days found in file than can be displayed!\n"
                        + "Only the first " + datePickers.length + " "
                        + "can be displayed and configurable.\nThe last "
                        + (dates.size() - datePickers.length) + " "
                        + "dates in your file will be\nimported with"
                        + " the dates untouched!\n\n");
                log("Unconfigurable days that will be imported:");
                for (int i = datePickers.length; i < dates.size(); i++) {
                    log(dates.get(i).format(DateTimeFormatter.ISO_DATE));
                }
            }
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

        //threadedHandledFileConversion(getInputFile());
        myfiles.addAll(handleFileWriting());
        
        if (null == myThread || !myThread.isAlive()) {
            myThread = (new Thread(() -> {
                try {
                    watchForConsumption();
                } catch (IOException ex) {
                    Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }));
            myThread.setDaemon(true);
            myThread.start();
        }

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
                        + "Modified: " + new Date(mostRecent.lastModified()) + "\n\n"
                        + "Would you like load this file?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.isPresent() && answer.get().getButtonData().equals(ButtonData.OK_DONE)) {
                    System.out.println(answer.get());
                    setInputFile(mostRecent);

                    //setting the input file is enough to trigger the load!
                    //     loadInInfo(mostRecent);
                    //not safe, just load the file in for you.
//                    onBtnGo(null);
                } else {
                }
            });

        }).start();
    }

//    private void threadedHandledFileConversion(File file) {
//        new Thread(() -> hanldeFileConversion(file)).start();
//    }
    /**
     *
     * @param file The location of the file downloaded from the google sheet.
     * @return The dates the scheduled traffic.
     */
//    private ArrayList<LocalDate> parseDates(File file) {
//        BufferedReader TSVFile = null;
//        ArrayList<LocalDate> answer = new ArrayList<>(7);
//        try {
//            TSVFile = new BufferedReader(new FileReader(file.getAbsoluteFile()));
//            String dataRow = TSVFile.readLine();
//            while (dataRow != null) {
//                String[] row = dataRow.split("\t");
//                System.out.println("I think the day is " + row[0]);
//                answer.add(LocalDate.parse("20" + row[0]));
//                dataRow = TSVFile.readLine();
//            }
//            TSVFile.close();
////            return answer;
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                TSVFile.close();
//            } catch (IOException ex) {
//                Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
//        return answer;
//    }
    private ArrayList<String> handleFileWriting() {
        ArrayList<String> filenames = new ArrayList<>(dates.size());

        dates.parallelStream().forEach(x -> {
            int index = dates.indexOf(x);

            if (!dateChecks[index].isSelected()) {
                return;
            }

            //Am i already imported?
            File alreadyThereFile = isPresentForDate(x);
            if (null != alreadyThereFile) {
                UI(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Re-import?");
                    alert.setContentText("Hey there, just thought I'd let you know "
                            + "that you've already imported a file for this day: "
                            + x.format(DateTimeFormatter.ISO_DATE) + ". Because I found this file: " + alreadyThereFile.getName()
                            + "\nAre you sure you want to do this?");
                    Optional<ButtonType> answer = alert.showAndWait();
                    if (answer.isPresent() && answer.get().getButtonData().equals(ButtonData.OK_DONE)) {
                        writeFile(toWideOrbitTitle(x), data.get(index));
                        UI(()-> datePickers[index].setStyle("-fx-background-color: yellow"));
                        filenames.add(toWideOrbitTitle(x));
                    } else {
                        log("Skipping: " + alreadyThereFile.getName());
                    }
                });
            } else {
                writeFile(toWideOrbitTitle(x), data.get(index));
                UI(()->datePickers[index].setStyle("-fx-background-color: yellow"));
                filenames.add(toWideOrbitTitle(x));
            }
        });
        return filenames;
    }

//    private void hanldeFileConversion(File file) {
//        BufferedReader TSVFile = null;
//        try {
//            TSVFile = new BufferedReader(new FileReader(file.getAbsoluteFile()));
//            String dataRow = TSVFile.readLine();
//            while (dataRow != null) {
//                String[] row = dataRow.split("\t");
//                System.out.println(row[1] + "Will be:\n" + row[2]);
//                handleImport(row);
//                dataRow = TSVFile.readLine();
//            }
//            TSVFile.close();
//
//            System.out.println();
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                TSVFile.close();
//            } catch (IOException ex) {
//                Logger.getLogger(Main2Controller.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//    
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

//    private TrafficDay parseRow(String[] row) {
//        TrafficDay t = new TrafficDay();
//        t.setDay(LocalDate.now());
//        String dateStr = "";
//        for (String string : row) {
//            if (string.matches("\\d\\d-\\d\\d-\\d\\d")) {
//                dateStr = string;
//                break;
//            }
//        }
//        //yy-mm-dd.
//        String[] parts = dateStr.split("-");
//        t.getDay().withYear(2000 + Integer.parseInt(parts[0]));
//        t.getDay().withDayOfMonth(Integer.parseInt(parts[1]));
//        t.getDay().withMonth(Integer.parseInt(parts[2]));
//        t.setContent(row[row.length - 1]);
//        return t;
//    }
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
    private String writeFile(String fileName, String data) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.write(data);
            writer.close();
            String newFileName = getOutputDir().getAbsolutePath() + java.io.File.separator + fileName + getExtension();
            if (rename(fileName, newFileName)) {
                log("Success writing: " + newFileName);
            } else {
                log("failed to write:" + newFileName);
            };
            return newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            // do something
        }
        return null;

    }

//    private void handleImport(String[] row) {
//
//        chkAlreadyImported(row[0], (fileName)
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

    public static String toWideOrbitTitle(LocalDate ld) {
        return ld.format(DateTimeFormatter.ofPattern("yyMMdd"));

    }

    public File isPresentForDate(LocalDate ld) {
        //the date/file name
        File[] files = getOutputDir().listFiles();
        String fileName = toWideOrbitTitle(ld);
        Optional<File> opSome = Arrays.stream(files).parallel().filter(x -> x.getName().contains(fileName)).findAny();
        if (opSome.isPresent()) {
            return opSome.get();
        } else {
            return null;
        }

    }

//    private void chkAlreadyImported(String datestr, Consumer<String> onDoImport) {
//        //the date/file name
//        File[] files = getOutputDir().listFiles();
//        String fileName = Arrays.stream(datestr.split("-")).collect(Collectors.joining());
//        Optional<File> opSome = Arrays.stream(files).parallel().filter(x -> x.getName().contains(fileName)).findAny();
//        if (opSome.isPresent()) {
//            //oh snap, looks like one's here already!
//            UI(() -> {
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle("Re-import?");
//                alert.setContentText("Hey there, just thought I'd let you know "
//                        + "that you've already imported a file for this day: "
//                        + datestr + ". Because I found this file: " + opSome.get().getName()
//                        + "\nAre you sure you want to do this?");
//                Optional<ButtonType> answer = alert.showAndWait();
//                if (answer.isPresent() && answer.get().getButtonData().equals(ButtonData.OK_DONE)) {
//                    onDoImport.accept(fileName);
//                } else {
//                    log("Skipping: " + fileName);
//                }
//
//                return;
//            });
//        } else {
//            //then of course we can import.
//            onDoImport.accept(fileName);
//        }
//    }
    public void UI(Runnable f) {
        Platform.runLater(f);
    }

    private String mkLineBreaks(String string) {
        return Arrays.stream(string.split("(?=\\d\\d:\\d\\d:\\d\\d)")).collect(Collectors.joining("\n"));
    }

    private File safeFileSet(File file) {
        return safeFileSetHelper(file, 17);
    }

    private File safeFileSetHelper(File file, int i) {
        if (file.exists()) {
            return file;
        } else if (i > 0) {
            return safeFileSetHelper(file.getParentFile(), i - 1);
        } else {
            return (new DirectoryChooser()).getInitialDirectory();
        }
    }

    @FXML
    private void selectAllClicked(MouseEvent event) {
        chkSelectAll.setSelected(!chkSelectAll.isSelected());
    }

    private String getData(String[] row) {
        return lineBreaksMatter ? row[2] : mkLineBreaks(row[2]);
    }

    private void watchForConsumption() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();

        try {
            Path dir = getOutputDir().toPath();
            WatchKey key = dir.register(watcher,
                    ENTRY_DELETE);

            for (;;) {

                if (Thread.interrupted()) {
                    key.cancel();
                    return;
                }
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == OVERFLOW) {
                        continue;
                    }

//                        // The filename is the
//                        // context of the event.
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filepath = ev.context();
                    String filename = filepath.toString();
                    System.out.println("the filename was: " + filename);
                    System.out.println(kind);
                    Optional<String> res = myfiles.parallelStream().filter(str -> {
                        return filename.contains(str);
                    }).findAny();
                    if (res.isPresent()) {
                        myfiles.remove(res.get());
                        DatePicker dp = findThisDP(res.get());
                        if (null != dp) {
                            UI(()->dp.setStyle("-fx-background-color: lightgreen"));
                        }
                        log("Wide Orbit CONSUMED: " + filename);

                    }
                    // Reset the key -- this step is critical if you want to
                    // receive further watch events.  If the key is no longer valid,
                    // the directory is inaccessible so exit the loop.
                    boolean valid = key.reset();
                    if (!valid) {
                        return;
                    }
                    if (myfiles.isEmpty()) {
                        key.cancel();
                        log("ALL WRITTEN FILES CONSUMED.");

                        return;
                    }
                }//end of events
            }//end of infinite loop

        } catch (IOException x) {
            System.err.println(x);
        } finally {
            Thread.currentThread().interrupt();
        }

    }

    private DatePicker findThisDP(String str) {
        for (int i = 0; i < datePickers.length; i++) {
            if (toWideOrbitTitle(datePickers[i].getValue()).compareTo(str) == 0) {
                return datePickers[i];
            }
        }
        return null;
    }

}
