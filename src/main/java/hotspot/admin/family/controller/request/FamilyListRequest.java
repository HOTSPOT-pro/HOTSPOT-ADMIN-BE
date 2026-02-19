package hotspot.admin.family.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FamilyListRequest {

    @NotNull
    @Min(1)
    @Max(100)
    private Integer size = 30;

    private Long cursor;
}
