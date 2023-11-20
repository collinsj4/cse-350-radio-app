package com.app.vaporwave.database.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.app.vaporwave.models.Radio;
import com.google.gson.annotations.Expose;

@Entity(tableName = "radio")
public class RadioEntity {

    @PrimaryKey
    @NonNull
    public String radio_id = "";

    @Expose
    @ColumnInfo(name = "radio_name")
    public String radio_name = "";

    @Expose
    @ColumnInfo(name = "radio_image")
    public String radio_image = "";

    @Expose
    @ColumnInfo(name = "radio_url")
    public String radio_url = "";

    @Expose
    @ColumnInfo(name = "category_name")
    public String category_name = "";

    @Expose
    @ColumnInfo(name = "saved_date")
    public long saved_date = System.currentTimeMillis();

    public RadioEntity() {
    }

    public static RadioEntity entity(Radio radio) {
        RadioEntity entity = new RadioEntity();
        entity.radio_id = radio.radio_id;
        entity.radio_name = radio.radio_name;
        entity.radio_image = radio.radio_image;
        entity.radio_url = radio.radio_url;
        entity.category_name = radio.category_name;
        return entity;
    }

    public Radio original() {
        Radio radio = new Radio();
        radio.radio_id = radio_id;
        radio.radio_name = radio_name;
        radio.radio_image = radio_image;
        radio.radio_url = radio_url;
        radio.category_name = category_name;
        return radio;
    }
}