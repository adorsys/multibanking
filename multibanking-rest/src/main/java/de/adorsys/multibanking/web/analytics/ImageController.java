package de.adorsys.multibanking.web.analytics;

import java.io.IOException;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.adorsys.multibanking.service.analytics.CustomImageService;
import de.adorsys.multibanking.service.analytics.SystemImageService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;

/**
 * The image controller.
 * - User can upload image in his repository
 * - User can share image with admin
 * - Admin can release image
 * 
 * @author fpo
 *
 */
@UserResource
@RestController
@RequestMapping(path = ImageController.BASE_PATH)
@Api(value = ImageController.BASE_PATH, 
tags = "MB-008 - Analytics", description="Manages account and booking analytics.")
public class ImageController extends BaseController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/image"; 

	@Autowired
	private CustomImageService customImageService;
	@Autowired
	private SystemImageService systemImageService;

	/**
	 * Loading an image to display to the user. If the user has put an image with the same 
	 * name in his repository, this image will be displayed. If not the system image will
	 * be displayed. 
	 * 
	 * This give some power user the chance to test image with their account before sharing
	 * the image with the rest of the system.
	 * 
	 * @param imageName
	 * @return
	 */
	@GetMapping(value = "/{imageName}", produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody ResponseEntity<ByteArrayResource>  getImage(@PathVariable String imageName) throws IOException {
		if(customImageService.hasImage(imageName)){
			return loadBytesForWeb(customImageService.loadUserImage(imageName), MediaType.IMAGE_PNG);
		} else {
			return loadBytesForWeb(systemImageService.loadStaticImage(imageName), MediaType.IMAGE_PNG);
		}
	}

	@RequestMapping(path = "/{imageName}", method = RequestMethod.PUT, consumes=MediaType.IMAGE_PNG_VALUE)
    public HttpEntity<?> putImage(@PathVariable String imageName, @RequestParam MultipartFile imageFile) {
        if (!imageFile.isEmpty())return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("File is empty");
        try {
			customImageService.storeUserImage(imageName, IOUtils.toByteArray(imageFile.getInputStream()));
		} catch (IOException e) {
			throw new BaseException(e);
		}
        return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@RequestMapping(path = "/{imageName}/release", method = RequestMethod.POST)
    public HttpEntity<?> patchImage(@PathVariable String imageName) {
		DSDocument loadUserImage = customImageService.loadUserImage(imageName);
		systemImageService.storeStaticImage(imageName, loadUserImage.getDocumentContent().getValue());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
