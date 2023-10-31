package gov.hi.dhs.bes.mongodb.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "BatchDto")
public class BatchDTO {
    @MongoId
    @Field(name="id")
    public Integer id;
    @Field(name="name")
    public String name;
    @Field(name="middleName")
    public String middleName;
    @Field(name="surname")
    public String surname;
    @Field(name="birthDate")
    public String birthDate;

    public BatchDTO(Integer id, String name, String surname,String middleName, String birthDate) {
        this.id=id;
        this.name=name;
        this.middleName=middleName;
        this.surname=surname;
        this.birthDate=birthDate;
    }
}
