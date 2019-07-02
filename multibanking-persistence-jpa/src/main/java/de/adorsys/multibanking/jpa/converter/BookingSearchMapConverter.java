package de.adorsys.multibanking.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BookingSearchMapConverter implements AttributeConverter<Map<String, List<String>>
        , String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> customerInfo) {
        try {
            return objectMapper.writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String tanTransportTypesJson) {
        TypeReference<HashMap<String, List<String>>> typeRef
                = new TypeReference<HashMap<String, List<String>>>() {
        };

        try {
            return objectMapper.readValue(tanTransportTypesJson, typeRef);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
