package app.ccb.services;

import javax.xml.bind.JAXBException;

public interface CardService {

    Boolean cardsAreImported();

    String readCardsXmlFile();

    String importCards() throws JAXBException;
}
