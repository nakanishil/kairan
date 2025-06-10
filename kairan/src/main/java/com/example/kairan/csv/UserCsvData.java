package com.example.kairan.csv;
import com.orangesignal.csv.annotation.CsvEntity;

import com.orangesignal.csv.annotation.CsvColumn;
import lombok.Data;
import com.orangesignal.csv.annotation.CsvEntity;
import com.orangesignal.csv.annotation.CsvColumn;

@Data
@CsvEntity
public class UserCsvData {

    @CsvColumn(name = "氏名")
    private String name;

    @CsvColumn(name = "フリガナ")
    private String furigana;

    @CsvColumn(name = "役職")
    private String roleName;

    @CsvColumn(name = "委員")
    private String committeeName;

    @CsvColumn(name = "町名")
    private String districtName;
}

