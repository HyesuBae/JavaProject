package jpabook.start;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by hyesubae on 16. 7. 17.
 */

@Entity
@Table(name="MEMBER")
@Data
public class Member {
    @Id
    private String id;
    @Column(name="NAME")
    private String username;
    private Integer age;

}
