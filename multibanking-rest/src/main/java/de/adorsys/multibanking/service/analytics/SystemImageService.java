package de.adorsys.multibanking.service.analytics;

import org.adorsys.docusafe.business.types.complex.DSDocument;
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
		return sos.loadDocument(FQNUtils.imageFQN(imageName));
	}
	
	/**
	 * Store an image in the system repository.
	 * 
	 * @param imageName
	 * @param data
	 */
	public void storeStaticImage(String imageName, byte[] data){
		sos.storeDocument(FQNUtils.imageFQN(imageName), data);
	}
}
