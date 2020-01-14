package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SCA method type")
public enum AuthenticationTypeTO {
    SMS_OTP, CHIP_OTP, PHOTO_OTP, PUSH_OTP, EMAIL, APP_OTP
}
