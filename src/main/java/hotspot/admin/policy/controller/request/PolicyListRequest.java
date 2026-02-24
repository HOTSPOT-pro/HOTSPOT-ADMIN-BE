package hotspot.admin.policy.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyListRequest {

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;
}
