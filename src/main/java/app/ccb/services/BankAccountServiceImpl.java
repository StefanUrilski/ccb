package app.ccb.services;

import app.ccb.common.Constants;
import app.ccb.common.FilePath;
import app.ccb.domain.dtos.importBankAccount.ImportBankAccountDto;
import app.ccb.domain.dtos.importBankAccount.ImportBankAccountRootDto;
import app.ccb.domain.entities.BankAccount;
import app.ccb.domain.entities.Client;
import app.ccb.repositories.BankAccountRepository;
import app.ccb.repositories.ClientRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final FileUtil fileUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ClientRepository clientRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountServiceImpl(FileUtil fileUtil, XmlParser xmlParser, ModelMapper modelMapper, ClientRepository clientRepository, BankAccountRepository bankAccountRepository) {
        this.fileUtil = fileUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.clientRepository = clientRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Boolean bankAccountsAreImported() {
        return this.bankAccountRepository.count() != 0;
    }

    @Override
    public String readBankAccountsXmlFile() {
        return fileUtil.readFile(FilePath.BANK_ACCOUNTS_XML_PATH);
    }

    @Override
    public String importBankAccounts() throws JAXBException {
        List<String> importBankAccountInfo = new ArrayList<>();

        ImportBankAccountRootDto bankAccountsRootDto =
                xmlParser.parseXml(ImportBankAccountRootDto.class, FilePath.BANK_ACCOUNTS_XML_PATH);
        for (ImportBankAccountDto bankAccountDto : bankAccountsRootDto.getBankAccount()) {
            if (bankAccountDto.getAccountNumber() == null) {
                importBankAccountInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Client client = clientRepository.findByFullName(bankAccountDto.getClient()).orElse(null);

            if (client == null) {
                importBankAccountInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            BankAccount bankAccount = modelMapper.map(bankAccountDto, BankAccount.class);

            bankAccount.setClient(client);

            bankAccountRepository.saveAndFlush(bankAccount);
            importBankAccountInfo.add(String.format(Constants.SUCCESSFULLY_IMPORTED,
                    bankAccount.getClass().getSimpleName(),
                    bankAccount.getAccountNumber())
            );
        }

        return String.join(System.lineSeparator(), importBankAccountInfo);
    }
}
