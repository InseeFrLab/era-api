package fr.insee.era.extraction_rp_famille.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.insee.era.extraction_rp_famille.model.dto.ExternalJsonDto;
import fr.insee.era.extraction_rp_famille.model.dto.LoopVariablesDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExternalSerializer extends JsonSerializer<ExternalJsonDto> {


    @Override
    public void serialize(ExternalJsonDto value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("EXTERNAL");
        gen.writeStartObject();
        gen.writeObjectField("RPLISTPRENOMS", value.getSimpleVariableDto().getRpListePrenoms());
        gen.writeObjectField("TYPE_QUEST", value.getSimpleVariableDto().getTypeQuest());
        gen.writeObjectField("RPTYPEQUEST", value.getSimpleVariableDto().getRpTypeQuest());
        gen.writeObjectField("RPNBQUEST", value.getSimpleVariableDto().getRpNbQuest());
        // loop on surveyedIds
        int index = 0;
        for (Long id : value.getSurveyedIds()) {
            // surveyed
            serializeLoopVariables(value.getSurveyedVariablesList(), gen, index, id);

            // conjoint
            serializeLoopVariables(value.getConjointVariablesList() , gen, index, id);

            //parents
            serializeListLoopVariables(value.getParentsVariablesList(), gen, index, id);

            //enfants
            serializeListLoopVariables(value.getEnfantsVariablesList(), gen, index, id);

            index++;

        }
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private void serializeLoopVariables(Map<Long,LoopVariablesDto> map, JsonGenerator gen, int index, Long id) throws IOException {
        if (map != null) {
            LoopVariablesDto object = map.get(id);
            if (object != null) {
                serializeVariables(gen, index, object, null);
            }
        }
    }

    private void serializeListLoopVariables(Map<Long,List<LoopVariablesDto>> map, JsonGenerator gen, int index, Long id) throws IOException {
        if (map != null) {
            List<LoopVariablesDto> objects = map.get(id);
            if (objects != null) {
                for (LoopVariablesDto object : objects) {
                    serializeVariables(gen, index, object, object.getIndex());
                }
            }
        }
    }

    private void serializeVariables(JsonGenerator gen, int index, LoopVariablesDto object, Integer count) throws IOException {
        String type = object.getType().getLabel() + (count != null ? count : "");
        gen.writeObjectField("RPID" + type + "_" + index, object.getId());
        gen.writeObjectField("RPPRENOM" + type + "_" + index, object.getFirstName());
        gen.writeObjectField("RPSEX" + type + "_" + index, object.getGender());
        gen.writeObjectField("RPANAIS" + type + "_" + index, object.getBirthYear());
        gen.writeObjectField("RPMNAIS" + type + "_" + index, object.getBirthMonth());
        gen.writeObjectField("RPJNAIS" + type + "_" + index, object.getBirthDay());
        gen.writeObjectField("RPDPNAICODE" + type + "_" + index, object.getDpnaicode());
        gen.writeObjectField("RPCNAIF" + type + "_" + index, object.getCnaif());
        gen.writeObjectField("RPCNAIE" + type + "_" + index, object.getCnaie());
        gen.writeObjectField("RPPNAI" + type + "_" + index, object.getPnai());
    }


}
