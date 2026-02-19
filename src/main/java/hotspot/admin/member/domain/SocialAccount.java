package hotspot.admin.member.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SocialAccount {
    private final Long socialAccountId;
    private final Long memberId;
    private final String email;
    private final String socialId;
    private final Provider provider;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
