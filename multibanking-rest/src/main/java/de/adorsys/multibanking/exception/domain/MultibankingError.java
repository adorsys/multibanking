package de.adorsys.multibanking.exception.domain;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import de.adorsys.multibanking.utils.DateConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Main Representation of API Errors
 * 
 * @author fpo 2018-04-07 08:01
 * @author bwa
 * @author igrex
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ApiModel(description = "Error detail information", value = "Error")
@Builder(toBuilder = true)
@AllArgsConstructor
public class MultibankingError implements Serializable {
	private static final long serialVersionUID = -6789536775158955859L;

	public static enum Severity {
		ERROR, WARNING, INFO
	}

	@JsonIgnore
	private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

	@JsonIgnore
	private Throwable exception;

	@ApiModelProperty(value = "The http status code", example = "401")
	private int http = status.value();

	@ApiModelProperty(value = "The occurence time stamp '" + DateConstants.DATE_FORMAT_ISO8601
			+ "'.", example = "2018-02-12T04:33:38Z")
	private String timestamp = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern(DateConstants.DATE_FORMAT_ISO8601));

	@ApiModelProperty(value = "The severity of the error", example = "WARNING")
	@JsonProperty("severity")
	private Severity severity = Severity.ERROR;

	@ApiModelProperty(value = "The key (i18n) for the error message.", example = "message.key")
	@JsonProperty("text")
	private String text = ErrorConstants.ERR_UNKNOWN;

	@ApiModelProperty(value = "The default text (i18n) for the error message, eventualy in the user language.", example = "Message text {param-key}")
	@JsonProperty("messageText")
	private String renderedMessage;

	@ApiModelProperty(value = "List of parameter associated with the message (optional)")
	@JsonProperty("params")
	private Map<String, String> params; // NOSONARz

	@ApiModelProperty(value = "Developer level detailed exception message (optional)", example = "An exceptional message")
	@JsonProperty("developer_text")
	private String developerText;

	@ApiModelProperty(value = "Additional information for error handling (optional)", example = "More info")
	@JsonProperty("more_info")
	private String moreInfo;

	@ApiModelProperty(value = "List of sub errors (optional)")
	@JsonProperty("sub_errors")
	private List<SubError> subErrors;

	public void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	public void addGlobalErrors(List<ObjectError> globalErrors) {
		globalErrors.forEach(this::addValidationError);
	}

	public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
		constraintViolations.forEach(this::addValidationError);
	}

	/**
	 * Utility method for adding error of ConstraintViolation. Usually when
	 * a @Validated validation fails.
	 * 
	 * @param cv
	 *            the ConstraintViolation
	 */
	private void addValidationError(ConstraintViolation<?> cv) {
		this.addValidationError(cv.getRootBeanClass().getSimpleName(),
				((PathImpl) cv.getPropertyPath()).getLeafNode().asString(), cv.getInvalidValue(), cv.getMessage());
	}

	private void addValidationError(String object, String field, Object rejectedValue, String message) {
		Map<String, String> params = new HashMap<>();
		params.put(ErrorConstants.ERR_OBJECT_VALUE_KEY, object);
		if(field!=null)params.put(ErrorConstants.ERR_FIELD_NAME_KEY, field);
		if(rejectedValue!=null)params.put(ErrorConstants.ERR_FIELD_VALUE_KEY, "" + rejectedValue);
		SubError subError = SubError.builder().text(ErrorConstants.ERR_VALIDATION).renderedMessage(message).params(params).build();
		addSubError(subError);
	}

	private void addValidationError(String object, String message) {
		addValidationError(object, null, null, message);
	}

	private void addSubError(SubError subError) {
		if (subErrors == null) {
			subErrors = new ArrayList<>();
		}
		subErrors.add(subError);
	}

	private void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(),
				fieldError.getDefaultMessage());
	}

	private void addValidationError(ObjectError objectError) {
		this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
	}

    public static MultibankingErrorBuilder builder(){
    	return new MultibankingErrorBuilder(){

			@Override
			public MultibankingError build() {
				return postBuild(super.build());
			}

		    private MultibankingError postBuild(MultibankingError error) {
		    	// http
				error.http = error.status!=null?error.status.value():error.http;
				
				// Set developer text if none.
		        if (error.exception != null && StringUtils.isBlank(error.developerText))
		        	error.developerText = error.exception.getLocalizedMessage();
		        return error;
		    }
    	};
    }
	
}

class LowerCaseClassNameResolver extends TypeIdResolverBase {

	@Override
	public String idFromValue(Object value) {
		return value.getClass().getSimpleName().toLowerCase();
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return suggestedType.getSimpleName().toLowerCase();
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CUSTOM;
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		if (id.equals(idFromValueAndType(null, MultibankingError.class)))
			return context.getTypeFactory().constructSimpleType(MultibankingError.class, null);
		return super.typeFromId(context, id);
	}

}
