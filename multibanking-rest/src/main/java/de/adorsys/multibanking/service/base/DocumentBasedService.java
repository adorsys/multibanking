package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for providing access to the document safe service.
 *
 * @author fpo 2018-04-06 04:36
 *
 */
public abstract class DocumentBasedService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentBasedService.class);

    protected abstract UserIDAuth auth();
    protected abstract DocumentSafeService docs();

	public boolean documentExists(DocumentFQN documentFQN){
		return docs().documentExists(auth(), documentFQN);
	}

	/**
	 * Loads the document at the given location
	 * @param documentFQN
	 * @return
	 */
	public DSDocument loadDocument(DocumentFQN documentFQN) {
        LOGGER.debug("loadDocument " + documentFQN);
		return docs().readDocument(auth(), documentFQN);
	}

	public void storeDocument(DSDocument dsDocument) {
		docs().storeDocument(auth(), dsDocument);
	}

	/**
	 * Stores the given byte array at the given location.
	 *
	 * @param documentFQN
	 * @param data
	 */
	public void storeDocument(DocumentFQN documentFQN, byte[] data) {
        LOGGER.debug("storeDocument " + documentFQN);
		DocumentContent documentContent = new DocumentContent(data);
		DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
		docs().storeDocument(auth(), dsDocument);
	}

	/**
	 * Delete the given directory.
	 *
	 * @param dirFQN
	 */
	public void deleteDirectory(DocumentDirectoryFQN dirFQN) {
		docs().deleteFolder(auth(), dirFQN);
	}

	public void deleteDocument(DocumentFQN documentFQN) {
        LOGGER.debug("deleteDocument " + documentFQN);
        docs().deleteDocument(auth(), documentFQN);
	}
}
