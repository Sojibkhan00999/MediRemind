package com.dreamsofmyparents.mediremind;

public class ReminderModel {
    public int id;
    public String name;
    public String medicine; // Medicine name
    public String dose;
    public String time;
    public String type; // "once" or "daily"
    public String frequency; // "once" or "daily" - for compatibility
    public String meal; // "খাবার আগে" or "খাবার পরে"

    public ReminderModel(int id, String name, String dose, String time, String type) {
        this.id = id;
        this.name = name;
        this.medicine = name; // Set medicine same as name for compatibility
        this.dose = dose;
        this.time = time;
        this.type = type;
        this.frequency = type; // Set frequency same as type for compatibility
        this.meal = "খাবার পরে"; // Default meal timing
    }
    
    // Constructor with all fields
    public ReminderModel(int id, String medicine, String dose, String time, String frequency, String meal) {
        this.id = id;
        this.medicine = medicine;
        this.name = medicine; // For backward compatibility
        this.dose = dose;
        this.time = time;
        this.frequency = frequency;
        this.type = frequency; // For backward compatibility
        this.meal = meal;
    }
}
