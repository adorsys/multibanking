package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import domain.Bank;

@JsonIgnoreProperties(ignoreUnknown=true)
public class XLSBank extends Bank {

}
