package fr.insee.era.extraction_rp_famille.utils;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.era.extraction_rp_famille.model.exception.CsvFileException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public final class CsvToBeanUtils {

    private CsvToBeanUtils() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    public static <T> List<T> parse(String fileName, Class<T> targetType) throws CsvFileException {
        try (Reader reader = new BufferedReader(new InputStreamReader(CsvToBeanUtils.class.getClassLoader().getResourceAsStream(fileName)))) {

            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader).withType(targetType).withSeparator(';')
                .withIgnoreLeadingWhiteSpace(true).withEscapeChar('\0')
                .withThrowExceptions(false).build();

            Iterator<T> it = csvToBean.iterator();

            List<T> listT = new ArrayList<>();

            while (it.hasNext()) {
                T t = it.next();
                listT.add(t);
            }

            if (!csvToBean.getCapturedExceptions().isEmpty()) {
                csvToBean.getCapturedExceptions().stream().forEach(e -> log.error(e.getMessage(), e));
                throw new CsvFileException("File read error");
            }

            return listT;
        } catch (Exception e) {
            throw new CsvFileException("File read error", e);
        }

    }
}
