package app.ccb.services;

import app.ccb.common.Constants;
import app.ccb.common.FilePath;
import app.ccb.domain.dtos.ImportEmployeeDto;
import app.ccb.domain.entities.Branch;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.BranchRepository;
import app.ccb.repositories.EmployeeRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final Gson gson;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final ValidationUtil validator;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(Gson gson,
                               FileUtil fileUtil,
                               ModelMapper modelMapper,
                               ValidationUtil validator,
                               BranchRepository branchRepository,
                               EmployeeRepository employeeRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.branchRepository = branchRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() != 0;
    }

    @Override
    public String readEmployeesJsonFile() {
        return fileUtil.readFile(FilePath.EMPLOYEES_JSON_PATH);
    }

    @Override
    public String importEmployees(String employees) {
        List<String> importEmployeesInfo = new ArrayList<>();
        ImportEmployeeDto[] importEmployeeDto = gson.fromJson(employees, ImportEmployeeDto[].class);

        for (ImportEmployeeDto employeeDto : importEmployeeDto) {
            if (! validator.isValid(employeeDto)) {
                importEmployeesInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Branch branch = branchRepository.findByName(employeeDto.getBranchName()).orElse(null);

            if (branch == null) {
                importEmployeesInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Employee employee = modelMapper.map(employeeDto, Employee.class);

            String[] names = employeeDto.getFullName().split("\\s+");

            if (names.length != 2) {
                importEmployeesInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            employee.setFirstName(names[0]);
            employee.setLastName(names[1]);
            employee.setStartedOn(LocalDate.parse(
                    employeeDto.getStartedOn(),
                    DateTimeFormatter.ofPattern("yyyy-MM-d")
            ));

            employee.setBranch(branch);
            employeeRepository.saveAndFlush(employee);
            importEmployeesInfo.add(String.format(Constants.SUCCESSFULLY_IMPORTED,
                    employee.getClass().getSimpleName(),
                    employeeDto.getFullName())
            );
        }

        return String.join(System.lineSeparator(), importEmployeesInfo);
    }

    @Override
    public String exportTopEmployees() {
        List<Employee> employees = employeeRepository.findAllByClientsNotNull();

        List<String> topEmployeesInfo = new ArrayList<>();
        employees.stream().sorted((f, s) -> {
            if (s.getClients().size() - f.getClients().size() == 0) {
                return f.getId().compareTo(s.getId());
            }
            return s.getClients().size() - f.getClients().size();
        }).forEach(employee -> {
            String fullName = employee.getFirstName() + " " + employee.getLastName();
            topEmployeesInfo.add("FullName: " + fullName);
            topEmployeesInfo.add("Salary: " + employee.getSalary());
            topEmployeesInfo.add("Started On: " + employee.getStartedOn());
            topEmployeesInfo.add("Clients: ");

            employee.getClients().forEach(client ->
                    topEmployeesInfo.add("      " + client.getFullName())
            );
            topEmployeesInfo.add("");
        });

        return String.join(System.lineSeparator(), topEmployeesInfo);
    }
}
