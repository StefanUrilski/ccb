package app.ccb.services;

import app.ccb.common.Constants;
import app.ccb.common.FilePath;
import app.ccb.domain.dtos.ImportCards.ImportCardDto;
import app.ccb.domain.dtos.ImportCards.ImportCardRootDto;
import app.ccb.domain.entities.BankAccount;
import app.ccb.domain.entities.Card;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.BankAccountRepository;
import app.ccb.repositories.CardRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import app.ccb.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    private final FileUtil fileUtil;
    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final ValidationUtil validator;
    private final CardRepository cardRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public CardServiceImpl(FileUtil fileUtil, XmlParser xmlParser, ModelMapper modelMapper, ValidationUtil validator, CardRepository cardRepository, BankAccountRepository bankAccountRepository) {
        this.fileUtil = fileUtil;
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.cardRepository = cardRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Boolean cardsAreImported() {
        return this.cardRepository.count() != 0;
    }

    @Override
    public String readCardsXmlFile() {
        return fileUtil.readFile(FilePath.CARDS_XML_PATH);
    }

    @Override
    public String importCards() throws JAXBException {
        List<String> importCardsInfo = new ArrayList<>();
        ImportCardRootDto cardRootDto =
                xmlParser.parseXml(ImportCardRootDto.class, FilePath.CARDS_XML_PATH);

        for (ImportCardDto cardDto : cardRootDto.getCars()) {
            if (! validator.isValid(cardDto)) {
                importCardsInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            BankAccount bankAccount = bankAccountRepository
                    .findByAccountNumber(cardDto.getAccountNumber())
                    .orElse(null);

            if (bankAccount == null) {
                importCardsInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Card card = modelMapper.map(cardDto, Card.class);

            card.setBankAccount(bankAccount);

            cardRepository.saveAndFlush(card);
            importCardsInfo.add(String.format(Constants.SUCCESSFULLY_IMPORTED,
                    card.getClass().getSimpleName(),
                    cardDto.getCardNumber())
            );
        }

        return String.join(System.lineSeparator(), importCardsInfo);
    }
}
