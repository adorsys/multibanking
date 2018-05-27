package de.adorsys.multibanking.service.analytics;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.service.base.SystemObjectService;
import de.adorsys.multibanking.utils.FQNUtils;

/**
 * Images are stored accessible to everybody using the system id auth.
 * 
 * @author fpo 2018-03-20 06:46
 *
 */
@Service
public class SystemImageService {
	@Autowired
	private SystemObjectService sos;
    @Autowired
    private DocumentSafeService documentSafeService;

	public boolean hasImage(String imageName){
		return sos.documentExists(FQNUtils.imageFQN(imageName), null);
	}

	/**
	 * Load an image from the system repository.
	 * 
	 * @param imageName
	 * @return
	 */
	public DSDocument loadStaticImage(String imageName){
        return documentSafeService.readDocument(sos.auth(), FQNUtils.imageFQN(imageName));
	}
	
	/**
	 * Store an image in the system repository.
	 * 
	 * @param imageName
	 * @param data
	 */
	public void storeStaticImage(String imageName, byte[] data){
        DocumentContent documentContent = new DocumentContent(data);
        DSDocument dsDocument = new DSDocument(FQNUtils.imageFQN(imageName), documentContent, null);
        documentSafeService.storeDocument(sos.auth(), dsDocument);
	}
}
