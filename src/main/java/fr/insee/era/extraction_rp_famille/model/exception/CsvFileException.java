package fr.insee.era.extraction_rp_famille.model.exception;

public class CsvFileException extends RuntimeException {

    public CsvFileException(String message) {
        super(message);
    }

    public CsvFileException(String message, Throwable t) {
        super(message, t);
    }

}
