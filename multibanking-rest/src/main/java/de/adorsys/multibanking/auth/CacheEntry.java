package de.adorsys.multibanking.auth;

import java.util.Optional;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Data;

@Data
public class CacheEntry<T> {
	private TypeReference<T> valueType;
	private DocumentFQN docFqn;
	private Optional<T> entry;
	private boolean dirty;
}
