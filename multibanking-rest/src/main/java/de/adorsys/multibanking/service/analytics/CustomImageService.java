package de.adorsys.multibanking.service.analytics;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.utils.FQNUtils;

/**
 * Images are stored accessible to everybody using the system id auth.
 * 
 * @author fpo 2018-03-20 06:46
 *
 */
@Service
public class CustomImageService {
	@Autowired
	private UserObjectService uos;
	
	/**
	 * Check if the user has his own copy of this image.
	 * 
	 * @param imageName
	 * @return
	 */
	public boolean hasImage(String imageName){
		return uos.documentExists(FQNUtils.imageFQN(imageName), null);
	}

	/**
	 * Load image from the user repository.
	 * 
	 * @param imageName
	 * @return
	 */
	public DSDocument loadUserImage(String imageName){
		return uos.loadDocument(FQNUtils.imageFQN(imageName));
	}
	
	/**
	 * Store an image in the user repository.
	 * 
	 * @param imageName
	 * @param data
	 */
	public void storeUserImage(String imageName, byte[] data){
		uos.storeDocument(FQNUtils.imageFQN(imageName), data);
	}
}
