package de.adorsys.multibanking.exception.domain;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Main Representation of API SubErrors
 * 
 * @author fpo 2018-04-07 05:16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ApiModel(description = "SubError detail information", value = "SubError")
@Builder(toBuilder = true)
@AllArgsConstructor
public class SubError implements Serializable {
	private static final long serialVersionUID = 3656014078861748932L;

	public static enum Severity {
		ERROR, WARNING, INFO
	}

	@JsonIgnore
	private Throwable exception;

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

    public static SubErrorBuilder builder(){
    	return new SubErrorBuilder(){

			@Override
			public SubError build() {
				return postBuild(super.build());
			}

		    private SubError postBuild(SubError error) {
				// Set developer text if none.
		        if (error.exception != null && StringUtils.isBlank(error.developerText))
		        	error.developerText = error.exception.getLocalizedMessage();
		        return error;
		    }
    	};
    }
}
