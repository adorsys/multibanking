package de.adorsys.multibanking.exception.domain;

public abstract class ErrorConstants {
    public static final String ERR_ACCESS_DENIED = "error.accessDenied";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_METHOD_NOT_SUPPORTED = "error.methodNotSupported";
    public static final String ERR_INTERNAL_SERVER_ERROR = "error.internalServerError";
    public static final String MULTIBANKING_CALL_EXCEPTION = "error.mbsCallError";
    public static final String ERR_FORMAT = "error.format";
    public static final String ERR_UNKNOWN = "error.unknown";
    
    public static final String ERR_PARAMETER = "parameter.error";
    public static final String ERR_PARAMETER_MESSAGE = "Missing parameter with name {parameterName} and type {parameterType}";
    public static final String ERR_PARAMETER_NAME_KEY = "parameterName";
    public static final String ERR_PARAMETER_TYPE_KEY = "parameterType";
    public static final String ERR_PARAMETER_VALUE_KEY = "parameterValue";
    public static final String ERR_FIELD_NAME_KEY = "fieldName";
    public static final String ERR_FIELD_VALUE_KEY = "fieldValue";
    public static final String ERR_OBJECT_VALUE_KEY = "objectValue";

    public static final String ERR_UNSUPPORTED_MEDIA_TYPE = "mediaType.unsupported";
    public static final String ERR_UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Hypermedia with type {actual} not supported. Following Mediatypes are supported {supported}";
    public static final String ERR_UNSUPPORTED_MEDIA_TYPE_ACTUAL_KEY = "actual";
    public static final String ERR_UNSUPPORTED_MEDIA_TYPE_SUPPORTED_KEY = "supported";
    
    public static final String ERR_METHOD_ARGUMENT_TYPE_MISMATCH = "argument.typemismatch";
    public static final String ERR_METHOD_ARGUMENT_TYPE_MISMATCH_MESSAGE = "The parameter {parameterName} of value {parameterValue} could not be converted to type {parameterType}";
    
    public static final String ERR_HTTP_CODE_UNAUTHENTICATED_KEY = "user.unauthenticated"; 
    public static final String ERR_HTTP_CODE_UNAUTHENTICATED_MESSAGE = "User not authenticated"; 
    public static final String ERR_HTTP_CODE_UNAUTHENTICATED_DOC = ERR_HTTP_CODE_UNAUTHENTICATED_KEY + " : " + ERR_HTTP_CODE_UNAUTHENTICATED_MESSAGE; 

    public static final String ERR_HTTP_CODE_BAD_REQUEST_DOC = "Invalid request. See response content."; 
}
