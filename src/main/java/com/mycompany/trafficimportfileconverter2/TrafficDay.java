/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trafficimportfileconverter2;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Paul
 */
public class TrafficDay {

    private final ObjectProperty<LocalDate> day = new SimpleObjectProperty<>();

    public TrafficDay(){
    
        
    }
    public LocalDate getDay() {
        return day.get();
    }

    public void setDay(LocalDate value) {
        day.set(value);
    }

    public ObjectProperty dayProperty() {
        return day;
    }
    private final StringProperty content = new SimpleStringProperty();

    public String getContent() {
        return content.get();
    }

    public void setContent(String value) {
        content.set(value);
    }

    public StringProperty contentProperty() {
        return content;
    }
    
    public String dayString(){
        //LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
        return day.toString();
        
    }
    private final StringProperty exportName = new SimpleStringProperty();

    public String getExportName() {
        return exportName.get();
    }

    public void setExportName(String value) {
        exportName.set(value);
    }

    public StringProperty exportNameProperty() {
        return exportName;
    }
    
    
}
