package de.adorsys.multibanking.parsing;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Type;

@AllArgsConstructor
public class StringDecoder implements Decoder {
    @NonNull
    private final Decoder delegate;

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (String.class.getName().equals(type.getTypeName())) {
            Response.Body body = response.body();
            return IOUtils.toString(body.asInputStream());
        }
        return delegate.decode(response, type);
    }
}
