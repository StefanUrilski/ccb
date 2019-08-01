package app.ccb.domain.dtos.ImportCards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "cards")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportCardRootDto {

    @XmlElement(name = "card")
    private List<ImportCardDto> cars;

    public List<ImportCardDto> getCars() {
        return cars;
    }

    public void setCars(List<ImportCardDto> cars) {
        this.cars = cars;
    }
}
