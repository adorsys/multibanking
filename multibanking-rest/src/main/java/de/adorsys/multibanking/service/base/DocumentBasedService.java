package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for providing access to the document safe service.
 * 
 * @author fpo 2018-04-06 04:36
 *
 */
public abstract class DocumentBasedService {

	@Autowired
	private DocumentSafeService documentSafeService;

	protected abstract UserIDAuth auth();
	
	public boolean documentExists(DocumentFQN documentFQN){
		return documentSafeService.documentExists(auth(), documentFQN);
	}

	/**
	 * Loads the document at the given location 
	 * @param documentFQN
	 * @return
	 */
	public DSDocument loadDocument(DocumentFQN documentFQN) {
		return documentSafeService.readDocument(auth(), documentFQN);
	}

	public void storeDocument(DSDocument dsDocument) {
		documentSafeService.storeDocument(auth(), dsDocument);
	}
	
	/**
	 * Stores the given byte array at the given location.
	 * 
	 * @param documentFQN
	 * @param data
	 */
	public void storeDocument(DocumentFQN documentFQN, byte[] data) {
		DocumentContent documentContent = new DocumentContent(data);
		DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
		documentSafeService.storeDocument(auth(), dsDocument);
	}

	/**
	 * Delete the given directory.
	 * 
	 * @param dirFQN
	 */
	public void deleteDirectory(DocumentDirectoryFQN dirFQN) {
		documentSafeService.deleteFolder(auth(), dirFQN);
	}

	public void deleteDocument(DocumentFQN documentFQN) {
		documentSafeService.deleteDocument(auth(), documentFQN);
	}
}
