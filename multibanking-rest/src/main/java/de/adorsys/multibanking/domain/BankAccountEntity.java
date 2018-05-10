package de.adorsys.multibanking.domain;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.adorsys.cryptoutils.exceptions.BaseException;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.BankAccount;
import lombok.Data;

/**
 * Created by alexg on 07.02.17.
 */
@Data
public class BankAccountEntity extends BankAccount implements IdentityIf {
    private final static Logger LOGGER = Logger.getLogger(BankAccount.class.getName());
    private String id;
    private String bankAccessId;
    private String userId;

    public String getId() {
        return id;
    }

    public BankAccountEntity id(String id) {
        this.id = id;
        return this;
    }

    public BankAccountEntity bankAccessId(String bankAccessId) {
        this.bankAccessId = bankAccessId;
        return this;
    }

	@Override
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	public LocalDateTime getLastSync() {
		return super.getLastSync();
	}
    
    public void setSyncStatus(SyncStatus s) {
        if (s!=null && getSyncStatus() != null) {
            if (s.equals(getSyncStatus())) {
                return;
            }
        }
        LOGGER.info("setSyncStatus von " + getSyncStatus() + " auf " + s);
        try {
            throw new BaseException("setSyncStatus von " + getSyncStatus() + " auf " + s);
        } catch (Exception e) {
        }
    	super.setSyncStatus(s);
    }    
}
