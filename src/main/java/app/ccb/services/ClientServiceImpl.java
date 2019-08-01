package app.ccb.services;

import app.ccb.common.Constants;
import app.ccb.common.FilePath;
import app.ccb.domain.dtos.ImportClientDto;
import app.ccb.domain.entities.Client;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.ClientRepository;
import app.ccb.repositories.EmployeeRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ValidationUtil validator;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ClientServiceImpl(Gson gson, FileUtil fileUtil, ValidationUtil validator, ClientRepository clientRepository, EmployeeRepository employeeRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.validator = validator;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Boolean clientsAreImported() {
        return this.clientRepository.count() != 0;
    }

    @Override
    public String readClientsJsonFile() {
        return fileUtil.readFile(FilePath.CLIENTS_JSON_PATH);
    }

    @Override
    public String importClients(String clients) {
        List<String> importClientInfo = new ArrayList<>();
        ImportClientDto[] importClientsDto = gson.fromJson(clients, ImportClientDto[].class);

        for (ImportClientDto clientDto : importClientsDto) {
            if (! validator.isValid(clientDto)) {
                importClientInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            String[] employeeName = clientDto.getAppointedEmployee().split("\\s+");

            if (employeeName.length != 2) {
                importClientInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Employee employee = employeeRepository.findByFirstNameAndLastName(
                    employeeName[0],
                    employeeName[1]
            ).orElse(null);

            if (employee == null) {
                importClientInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            String fullName = clientDto.getFirstName() + " " + clientDto.getLastName();
            Client client = clientRepository.findByFullName(fullName).orElse(null);

            if (client != null) {
                importClientInfo.add(Constants.DUPLICATE_DATA);
                continue;
            }

            client = new Client();

            client.setFullName(fullName);
            client.setAge(clientDto.getAge());
            client.getEmployee().add(employee);

            clientRepository.saveAndFlush(client);
            importClientInfo.add(String.format(Constants.SUCCESSFULLY_IMPORTED,
                    client.getClass().getSimpleName(),
                    client.getFullName())
            );
        }

        return String.join(System.lineSeparator(), importClientInfo);
    }

    @Override
    public String exportFamilyGuy() {
        List<String> familyGuyInfo = new ArrayList<>();

        Client client = clientRepository
                .findClientsOrderByCardCount(PageRequest.of(1,1))
                .stream()
                .findFirst()
                .get();

        familyGuyInfo.add("Full Name: " + client.getFullName());
        familyGuyInfo.add("Age: " + client.getAge());
        familyGuyInfo.add("Bank Account: " + client.getBankAccount().getAccountNumber());
        client.getBankAccount()
                .getCards()
                .forEach(card -> {
                    familyGuyInfo.add("     Card Number: " + card.getCardNumber());
                    familyGuyInfo.add("     Card Status: " + card.getCardStatus());
//                    familyGuyInfo.add("-----------------------------------------");
                });

        return String.join(System.lineSeparator(), familyGuyInfo);
    }
}
