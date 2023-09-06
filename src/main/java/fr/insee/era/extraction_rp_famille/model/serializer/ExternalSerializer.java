package fr.insee.era.extraction_rp_famille.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.insee.era.extraction_rp_famille.model.dto.ExternalsVariables;
import fr.insee.era.extraction_rp_famille.model.dto.LoopVariable;
import fr.insee.era.extraction_rp_famille.model.dto.SimpleVariable;

import java.io.IOException;

public class ExternalSerializer extends JsonSerializer<ExternalsVariables> {


    @Override
    public void serialize(ExternalsVariables value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("EXTERNAL");
        gen.writeStartObject();
        for (SimpleVariable simpleVariable : value.getSimpleVariables()) {
            gen.writeObjectField(simpleVariable.getName(), simpleVariable.getValue());
        }
        for (LoopVariable loopVariable : value.getLoopVariables()) {
            gen.writeFieldName(loopVariable.getName());
            gen.writeStartArray();
            for (String loopValue : loopVariable.getValues()) {
                gen.writeObject(loopValue);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }


}
