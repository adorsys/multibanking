package de.adorsys.multibanking.domain;

import java.util.List;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Bank;
import lombok.Data;

/**
 * Created by alexg on 08.05.17.
 * @author fpo 2018-03-25 02:08
 * 
 */
@Data
public class BankEntity extends Bank implements IdentityIf {

	private String id;
    private String blzHbci;
    private List<String> searchIndex;
}
