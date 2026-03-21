package hotspot.admin.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisplayIdType {
    FAMILY("FAM", 6),
    TIME_POLICY("TP", 3),
    APP_POLICY("AP", 3),
    FAMILY_REQUEST("REQ", 3);

    private final String prefix;
    private final int padLength;
}
