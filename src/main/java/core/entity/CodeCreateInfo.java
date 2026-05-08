package core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "code_create_info")
public class CodeCreateInfo {

    @Id
    @Column(name = "category")
    private String category;

    @Column(name = "code")
    private String code;

    @Column(name = "number")
    private String number;

}
