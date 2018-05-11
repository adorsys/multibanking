package de.adorsys.multibanking.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserAgentKeyEntry {
	private String keyId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime exp;
	private Map<String, String> keyData = new HashMap<>();
//	private OctetSequenceKey jwk;
}
