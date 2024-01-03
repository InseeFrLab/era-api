package fr.insee.era.extraction_rp_famille.utils;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import fr.insee.era.extraction_rp_famille.model.exception.CsvFileException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Slf4j
public final class CsvUtils {
    private CsvUtils() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    public static ByteArrayInputStream write(List<String[]> sources) throws CsvFileException {
        try (
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
                CSVWriter writer = new CSVWriter(streamWriter, ';', ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END)) {

            writer.writeAll(sources);

            streamWriter.flush();

            return new ByteArrayInputStream(stream.toByteArray());

        } catch (Exception e) {
            throw new CsvFileException("File write error", e);
        }

    }
}
