package domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by cga.
 */
@Data
@ApiModel(description="The bank Info object.", value="BankInfo" )
public class BankInfos {

    private String blz;
    private String bic;
    private String checksumMethod;
    private String location;
    private String name;
    private String pinTanAddress;
    private String rdhAddress;
	public BankInfos(String blz, String bic, String checksumMethod, String location, String name, String pinTanAddress,
			String rdhAddress) {
		super();
		this.blz = blz;
		this.bic = bic;
		this.checksumMethod = checksumMethod;
		this.location = location;
		this.name = name;
		this.pinTanAddress = pinTanAddress;
		this.rdhAddress = rdhAddress;
	}
	public BankInfos() {
	}
    
    
}
