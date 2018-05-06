package de.adorsys.multibanking.web.common;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.web.user.UserDataController;

public abstract class BaseController {
	public static final String BASE_PATH = "/api/v1";

	@Autowired
	private UserContext user;
	
	protected String userId(){
		return user.getAuth().getUserID().getValue();
	}
	
	protected ResponseEntity<ByteArrayResource> loadBytesForWeb(DSDocument dsDocument) {
		byte[] data = dsDocument.getDocumentContent().getValue();
		return ResponseEntity.ok()
				// Content-Type
				.contentType(MediaType.APPLICATION_JSON_UTF8) //
				// Content-Lengh
				.contentLength(data.length) //
				.body(new ByteArrayResource(data));
	}

	protected ResponseEntity<ByteArrayResource> loadBytesForWeb(DSDocument dsDocument, MediaType mediaType) {
		byte[] data = dsDocument.getDocumentContent().getValue();
		return ResponseEntity.ok()
				// Content-Type
				.contentType(mediaType) //
				// Content-Lengh
				.contentLength(data.length) //
				.body(new ByteArrayResource(data));
	}
	
	protected <T> ResponseEntity<T> returnDocument(T dsObjet){
		return ResponseEntity.ok()
				// Content-Type
				.contentType(MediaType.APPLICATION_JSON) //
				.body(dsObjet);
	}
	protected <T> ResponseEntity<T> returnDocument(T dsObjet, MediaType mediaType){
		return ResponseEntity.ok()
				// Content-Type
				.contentType(mediaType) //
				.body(dsObjet);
	}

	protected ResourceNotFoundException resourceNotFound(Class<?> klass, String id) {
		return new ResourceNotFoundException(klass, id);
	}
	
	protected static ObjectMapper yamlObjectMapper() {
		final YAMLFactory ymlFactory = new YAMLFactory();
		ObjectMapper objectMapper = new ObjectMapper(ymlFactory);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}
	
	protected HttpHeaders userDataLocationHeader(){
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(linkTo(methodOn(UserDataController.class).loadUserData()).toUri());
		return headers;
	}
}
