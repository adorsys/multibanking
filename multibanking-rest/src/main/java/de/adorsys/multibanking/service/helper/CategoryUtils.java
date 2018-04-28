package de.adorsys.multibanking.service.helper;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import de.adorsys.multibanking.utils.FQNUtils;

/**
 * @author fpo 2018-03-24 01:43
 *
 */
public abstract class CategoryUtils{
	/*
	 * We use this for the static and custom categories. Caller must pass the correct userIdAuth
	 */
	public static final DocumentFQN bookingCategoriesFQN = FQNUtils.bookingCategoriesFQN();
}
