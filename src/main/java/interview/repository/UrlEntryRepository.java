package interview.repository;

import interview.domain.UrlEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Rafael Roman on 4/5/17.
 */

public interface UrlEntryRepository extends MongoRepository<UrlEntry, String> {


}
