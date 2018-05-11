package de.adorsys.multibanking.service.base;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DSDocumentStream;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.AccessType;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.jwk.JWK;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;

public class ExceptionHandlingDocumentSafeService implements DocumentSafeService {

	private DocumentSafeService delegate;

    public ExceptionHandlingDocumentSafeService(DocumentSafeService delegate) {
		this.delegate = delegate;
	}

	/**
     * USER
     * ===========================================================================================
     */
    @Override
    public void createUser(UserIDAuth userIDAuth) {
    	delegate.createUser(userIDAuth);
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
    	delegate.destroyUser(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
    	return delegate.userExists(userID);
    }

    /**
     * DOCUMENT
     * ===========================================================================================
     */

    /**
     * -- byte orientiert --
     */
    @Override
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
    	try {
    		delegate.storeDocument(userIDAuth, dsDocument);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
    	try {
    		return delegate.readDocument(userIDAuth, documentFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }
    

//	@Override
//	public void linkDocument(UserIDAuth userIDAuth, DocumentFQN sourceDocumentFQN, DocumentFQN destinationDocumentFQN) {
//    	try {
//    		delegate.linkDocument(userIDAuth, sourceDocumentFQN, destinationDocumentFQN);
//    	} catch(BaseException b){
//    		throw checkContainer(b, userIDAuth);
//    	}
//	}
    

    /**
     * -- stream orientiert --
     */
    @Override
    public void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream) {
    	try {
    		delegate.storeDocumentStream(userIDAuth, dsDocumentStream);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }



    @Override
    public DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
    	try {
    		return delegate.readDocumentStream(userIDAuth, documentFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
    	try {
    		delegate.deleteDocument(userIDAuth, documentFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
    	try {
    		return delegate.documentExists(userIDAuth, documentFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
    public void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
    	try {
    		delegate.deleteFolder(userIDAuth, documentDirectoryFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    /**
     * GRANT/DOCUMENT
     * ===========================================================================================
     */
    @Override
    public void grantAccessToUserForFolder(UserIDAuth userIDAuth, UserID receiverUserID,
                                           DocumentDirectoryFQN documentDirectoryFQN,
                                           AccessType accessType) {
    	try {
    		delegate.grantAccessToUserForFolder(userIDAuth, receiverUserID, documentDirectoryFQN, accessType);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
    public void storeGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DSDocument dsDocument) {
    	try {
    		delegate.storeGrantedDocument(userIDAuth, documentOwner, dsDocument);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }


    @Override
    public DSDocument readGrantedDocument(UserIDAuth userIDAuth, UserID documentOwner, DocumentFQN documentFQN) {
    	try {
    		return delegate.readGrantedDocument(userIDAuth, documentOwner, documentFQN);
    	} catch(BaseException b){
    		throw checkContainer(b, userIDAuth);
    	}
    }

    @Override
	public JWK findPublicEncryptionKey(UserID userID) {
    	try {
    		return delegate.findPublicEncryptionKey(userID);
    	} catch(BaseException b){
    		throw checkContainer(b, userID);
    	}
	}


    private static boolean isContainerNotExist(BaseException b){
        if (b instanceof org.adorsys.encobject.exceptions.ResourceNotFoundException) {
            return true;
        }
		String message = b.getMessage();
		return StringUtils.startsWith(message, "Container") && StringUtils.endsWith(message, "does not exist");
    }

    private static RuntimeException checkContainer(BaseException b, UserIDAuth userIDAuth){
    	return checkContainer(b, userIDAuth.getUserID());
    }

    private static RuntimeException checkContainer(BaseException b, UserID userID){
		if(isContainerNotExist(b)) return new ResourceNotFoundException(UserEntity.class, userID.getValue());
		return b;
    }
}
