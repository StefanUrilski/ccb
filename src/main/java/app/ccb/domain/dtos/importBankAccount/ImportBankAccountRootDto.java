package app.ccb.domain.dtos.importBankAccount;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "bank-accounts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportBankAccountRootDto {

    @XmlElement(name = "bank-account")
    private List<ImportBankAccountDto> bankAccount;

    public List<ImportBankAccountDto> getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(List<ImportBankAccountDto> bankAccount) {
        this.bankAccount = bankAccount;
    }
}
