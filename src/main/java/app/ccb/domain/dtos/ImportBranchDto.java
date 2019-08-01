package app.ccb.domain.dtos;

import com.google.gson.annotations.Expose;

public class ImportBranchDto {

    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
