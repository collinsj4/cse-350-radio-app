package com.app.vaporwave.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRadio(RadioEntity radio);

    @Query("DELETE FROM radio WHERE radio_id = :radio_id")
    void deleteRadio(String radio_id);

    @Query("DELETE FROM radio")
    void deleteAllRadio();

    @Query("SELECT * FROM radio ORDER BY saved_date DESC")
    List<RadioEntity> getAllRadio();

    @Query("SELECT COUNT(radio_id) FROM radio")
    Integer getRadioCount();

    @Query("SELECT * FROM radio WHERE radio_id = :radio_id LIMIT 1")
    RadioEntity getRadio(String radio_id);

}
