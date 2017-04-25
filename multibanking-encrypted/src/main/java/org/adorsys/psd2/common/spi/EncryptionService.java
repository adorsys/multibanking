package org.adorsys.psd2.common.spi;

import org.adorsys.psd2.common.domain.JweEncryptionSpec;

public interface EncryptionService {

	public <T> T decrypt(String jweString, Class<T> modelKlass);

	public String encrypt(Object object, JweEncryptionSpec encSpec);
}
