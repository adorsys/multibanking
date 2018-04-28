package de.adorsys.multibanking.web.common;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.BankAccessService;

public class BankAccessBasedController extends BaseController {
    @Autowired
    protected BankAccessService bankAccessService;
    
    protected void checkBankAccessExists(String accessId){
        if (!bankAccessService.exists(accessId)) 
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
    }
}
