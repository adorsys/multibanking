package de.adorsys.multibanking.pers.utils;

import java.io.IOException;
import java.security.cert.CertificateException;

import org.adorsys.encobject.domain.KeyCredentials;
import org.adorsys.encobject.domain.ObjectHandle;
import org.adorsys.encobject.service.EncObjectService;
import org.adorsys.encobject.service.MissingKeyAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreAlgorithmException;
import org.adorsys.encobject.service.MissingKeystoreProviderException;
import org.adorsys.encobject.service.ObjectNotFoundException;
import org.adorsys.encobject.service.UnknownContainerException;
import org.adorsys.encobject.service.WrongKeyCredentialException;
import org.adorsys.encobject.service.WrongKeystoreCredentialException;
import org.adorsys.jjwk.selector.UnsupportedEncAlgorithmException;
import org.adorsys.jjwk.selector.UnsupportedKeyLengthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.pers.jcloud.domain.UserBookingRecord;
import de.adorsys.multibanking.pers.jcloud.domain.UserMainRecord;

@Service
public class ObjectPersistenceAdapter {

    @Autowired
    private EncObjectService encObjectService;

    private ObjectMapper objectMapper = new ObjectMapper();

	public <T> T load(ObjectHandle handle, Class<T> klass, KeyCredentials keyCredentials)  {
		byte[] src = null;
		try {
			src = encObjectService.readObject(keyCredentials, handle);
		} catch(ObjectNotFoundException e){
			return null;
		} catch(CertificateException | WrongKeystoreCredentialException | MissingKeystoreAlgorithmException | MissingKeystoreProviderException | MissingKeyAlgorithmException | IOException | WrongKeyCredentialException | UnknownContainerException e){
			throw new IllegalStateException(e);
		}
		
		try {
			return objectMapper.readValue(src, klass);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void store(ObjectHandle userMainRecordhandle, UserMainRecord userMainRecord, KeyCredentials keyCredentials) {
		storeInternal(userMainRecordhandle, userMainRecord, keyCredentials);
	}

	public void store(ObjectHandle userBookingRecordhandle, UserBookingRecord userBookingRecord, KeyCredentials keyCredentials) {
		storeInternal(userBookingRecordhandle, userBookingRecord, keyCredentials);
	}

	private void storeInternal(ObjectHandle handle, Object obj, KeyCredentials keyCredentials) {
		try {
			byte[] data = objectMapper.writeValueAsBytes(obj);
			encObjectService.writeObject(data, null, handle, keyCredentials);
		} catch (CertificateException | ObjectNotFoundException | WrongKeystoreCredentialException
				| MissingKeystoreAlgorithmException | MissingKeystoreProviderException | MissingKeyAlgorithmException
				| IOException | UnsupportedEncAlgorithmException | WrongKeyCredentialException
				| UnsupportedKeyLengthException | UnknownContainerException e) {
			throw new IllegalStateException(e);
		}
		
	}
}
