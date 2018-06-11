package de.adorsys.multibanking.service;

import java.util.Date;

import de.adorsys.multibanking.exception.ResourceNotFoundException;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.BankAccessCredentials;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.utils.FQNUtils;

/**
 * Manages credential associated with a bank access.
 * 
 * @author fpo 2018-04-06 05:47
 *
 */
@Service
public class BankAccessCredentialService  {

	@Autowired
	private UserObjectService uos;

    public void store(BankAccessCredentials credentials) {
        uos.store(FQNUtils.credentialFQN(credentials.getAccessId()), credentialsType(), credentials);
    }

	public void setInvalidPin(String accessId) {
		DocumentFQN credentialsFQN = FQNUtils.credentialFQN(accessId);
		BankAccessCredentials credentials = uos.load(credentialsFQN, credentialsType())
				.orElseThrow(() -> new ResourceNotFoundException(BankAccessCredentials.class, accessId));
		invalidate(credentials);
	}
	
	public BankAccessCredentials loadCredentials(String accessId){
		return uos.load(FQNUtils.credentialFQN(accessId), credentialsType())
				.orElseThrow(() -> new ResourceNotFoundException(BankAccessCredentials.class, accessId));
	}

	public void invalidate(BankAccessCredentials credentials) {
		DocumentFQN credentialsFQN = FQNUtils.credentialFQN(credentials.getAccessId());
		credentials.setPinValid(false);
		credentials.setLastValidationDate(new Date());
		uos.store(credentialsFQN, credentialsType(), credentials);
	}

	private static TypeReference<BankAccessCredentials> credentialsType(){
		return new TypeReference<BankAccessCredentials>() {};
	}
}
