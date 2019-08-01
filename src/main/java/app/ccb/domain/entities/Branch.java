package app.ccb.domain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "branches")
public class Branch extends BaseEntity {

    private String name;

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
