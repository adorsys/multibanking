package de.adorsys.multibanking.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class MlAnonymizedBookingEntity {

	@Id
	private String id;
	@Indexed
	private final String userId;
	private final String transactionPurpose;
	private final String amountBin;
	private final String amountRemainder;
	private final String creditorId;
	private final String referenceName;
	private final String executionDate;
	private final String mainCategory;
	private final String subCategory;
	private final String specification;
}