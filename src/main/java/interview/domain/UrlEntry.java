package interview.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Rafael Roman on 4/5/17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlEntry {

    @Id
    private String id;

    @NotNull
    private String url;

    private LocalDateTime crawledDate;

    private String patternUsed;

    private Result result;

    private String message;

    //TODO this should be outside the class
    public enum Result {

        MATCHES, NOT_MATCHES, ERROR

    }

}
