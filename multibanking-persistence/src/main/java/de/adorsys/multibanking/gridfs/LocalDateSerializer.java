package de.adorsys.multibanking.gridfs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author Christian Brandenstein
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {

    private static final Logger log = LoggerFactory.getLogger(LocalDateSerializer.class);

    @Override
    public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}