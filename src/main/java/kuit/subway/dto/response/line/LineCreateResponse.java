package kuit.subway.dto.response.line;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineCreateResponse {
    private String message;
    private Long id;
}
