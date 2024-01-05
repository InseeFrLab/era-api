package fr.insee.era.extraction_rp_famille.utils;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

@Getter
public class CogBean {
    @CsvBindByName(column = "Code INSEE")
    private String depCom;
    @CsvBindByName(column = "Code Postal")
    private String zipCode;

}
