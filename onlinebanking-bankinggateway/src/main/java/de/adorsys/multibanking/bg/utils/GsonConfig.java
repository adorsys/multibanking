package de.adorsys.multibanking.bg.utils;

import com.google.gson.Gson;
import de.adorsys.multibanking.xs2a_adapter.JSON;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GsonConfig {

    @Getter
    private final static Gson gson;

    static {
        gson = new JSON().getGson();
    }
}
