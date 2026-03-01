package hotspot.admin.family.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyListRequest {

    @Min(0)
    @Schema(description = "페이지 번호(0부터 시작)", example = "0", minimum = "0")
    private Integer page = 0;

    @Min(1)
    @Max(100)
    @Schema(description = "페이지 크기", example = "20", minimum = "1", maximum = "100")
    private Integer size = 20;
}
