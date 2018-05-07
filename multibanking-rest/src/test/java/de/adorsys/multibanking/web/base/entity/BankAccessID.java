package de.adorsys.multibanking.web.base.entity;

import domain.BankAccess;
import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 07.05.18 at 09:12.
 */
public class BankAccessID extends BaseTypeString {
    public BankAccessID(String s) {
        super(s);
    }
}
