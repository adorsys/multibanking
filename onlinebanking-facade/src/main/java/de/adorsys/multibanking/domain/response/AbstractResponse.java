package de.adorsys.multibanking.domain.response;

import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractResponse {

    private List<String> messages;
}
