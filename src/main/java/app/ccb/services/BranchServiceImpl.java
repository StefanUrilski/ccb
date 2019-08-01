package app.ccb.services;

import app.ccb.common.Constants;
import app.ccb.common.FilePath;
import app.ccb.domain.dtos.ImportBranchDto;
import app.ccb.domain.entities.Branch;
import app.ccb.repositories.BranchRepository;
import app.ccb.util.FileUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private final Gson gson;
    private final FileUtil fileUtil;
    private final BranchRepository branchRepository;

    @Autowired
    public BranchServiceImpl(Gson gson, FileUtil fileUtil, BranchRepository branchRepository) {
        this.gson = gson;
        this.fileUtil = fileUtil;
        this.branchRepository = branchRepository;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() != 0;
    }

    @Override
    public String readBranchesJsonFile() {
        return fileUtil.readFile(FilePath.BRANCHES_JSON_PATH);
    }

    @Override
    public String importBranches(String branchesJson) {
        List<String> importBranchesInfo = new ArrayList<>();
        ImportBranchDto[] importBranchDto = gson.fromJson(branchesJson, ImportBranchDto[].class);

        for (ImportBranchDto branchDto : importBranchDto) {
            if (branchDto.getName() == null || branchDto.getName().trim().isEmpty()) {
                importBranchesInfo.add(Constants.INCORRECT_DATA);
                continue;
            }

            Branch branch = new Branch();
            branch.setName(branchDto.getName());

            branchRepository.saveAndFlush(branch);
            importBranchesInfo.add(String.format(Constants.SUCCESSFULLY_IMPORTED,
                    branch.getClass().getSimpleName(),
                    branch.getName())
            );
        }

        return String.join(System.lineSeparator(), importBranchesInfo);
    }
}
