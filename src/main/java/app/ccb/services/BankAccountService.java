package app.ccb.services;

import javax.xml.bind.JAXBException;

public interface BankAccountService {

    Boolean bankAccountsAreImported();

    String readBankAccountsXmlFile();

    String importBankAccounts() throws JAXBException;
}
