package gov.hi.dhs.bes.mongodb.repository.batch_repo;

import gov.hi.dhs.bes.mongodb.model.dto.BatchDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchJobRep extends MongoRepository<BatchDTO,Integer> {

}
