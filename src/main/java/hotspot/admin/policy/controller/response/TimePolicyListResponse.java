package hotspot.admin.policy.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record TimePolicyListResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext,
        List<TimePolicyListItem> items
) {
}
