package de.adorsys.multibanking.service;

import domain.StandingOrder;

public interface XLSStandingOrderService {

	void addStandingOrders(String bankLogin, String iban, StandingOrder standingOrder);

}
