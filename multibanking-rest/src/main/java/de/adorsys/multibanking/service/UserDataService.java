package de.adorsys.multibanking.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import de.adorsys.multibanking.domain.BankAccessEntity;
import domain.BankAccess;
import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.exception.UserNotFoundException;
import de.adorsys.multibanking.service.base.StorageUserService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import de.adorsys.multibanking.utils.Ids;
import domain.BankApi;
import domain.BankApiUser;
import spi.OnlineBankingService;

/**
 * Manage Access to the user data. Manages all state information for this user account.
 * 
 * Consumer shall read this once and have all information need to initialize a user interface.
 * 
 * @author fpo 2018-03-17 08:38
 *
 */
@Service
public class UserDataService {
	@Autowired
	private UserObjectService uos;
    @Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Autowired
    private StorageUserService storageUserService;
    @Autowired
    private DocumentSafeService documentSafeService;

	public UserData load(){
		return uos.load(FQNUtils.userDataFQN(), valueType())
				.orElseThrow(() -> new UserNotFoundException(uos.auth().getUserID().getValue()));
	}
	
	public boolean exists(){
		return uos.documentExists(FQNUtils.userDataFQN(), valueType());
	}

	public void store(UserData userData){
		uos.store(FQNUtils.userDataFQN(), valueType(), userData);		
	}
	
    public DSDocument loadDocument() {
        return documentSafeService.readDocument(uos.auth(), FQNUtils.userDataFQN());
    }
	
	
    /**
     * Returns the user entity or create one if the user does not exist.
     */
    public UserData createUser(Date expire) {
    	UserIDAuth userIDAuth = uos.auth();

    	UserEntity userEntity = new UserEntity();
    	userEntity.setApiUser(new ArrayList<>());
    	userEntity.setId(userIDAuth.getUserID().getValue());
    	userEntity.setExpireUser(expire);
    	
    	UserData userData = new UserData();
    	userData.setUserEntity(userEntity);
    	store(userData);
    	return userData;
    }
	
	private static TypeReference<UserData> valueType(){
		return new TypeReference<UserData>() {};
	}

    /**
     * Returns the bank API user. Registers with the banking API if necessary.
     * 
     * User must have been create before.
     * 
     * @param bankApi
     * @param bankAccess
     * @return
     */
    public BankApiUser checkApiRegistration(BankApi bankApi, BankAccessEntity bankAccess) {
        OnlineBankingService onlineBankingService = bankApi != null
                ? bankingServiceProducer.getBankingService(bankApi)
                : bankingServiceProducer.getBankingService(bankAccess.getBankCode());

        if (onlineBankingService.userRegistrationRequired()) {
        	if(!storageUserService.userExists(uos.auth().getUserID())) 
        		throw new BaseException("Storage user with id: "+ uos.auth().getUserID().getValue() + " non existent ");
        	UserData userData = load();
            UserEntity userEntity = userData.getUserEntity();

            return userEntity.getApiUser()
                    .stream()
                    .filter(bankApiUser -> bankApiUser.getBankApi() == onlineBankingService.bankApi())
                    .findFirst()
                    .orElseGet(() -> {
                        BankApiUser bankApiUser = onlineBankingService.registerUser(Optional.empty(), bankAccess, bankAccess.getPin());
                        userEntity.getApiUser().add(bankApiUser);
                        store(userData);

                        return bankApiUser;
                    });
        } else {
            BankApiUser bankApiUser = new BankApiUser();
            bankApiUser.setBankApi(onlineBankingService.bankApi());
            return bankApiUser;
        }
    }	
}
