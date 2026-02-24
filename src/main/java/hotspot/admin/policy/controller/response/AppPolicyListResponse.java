package hotspot.admin.policy.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record AppPolicyListResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext,
        List<AppPolicyListItem> items
) {
}
