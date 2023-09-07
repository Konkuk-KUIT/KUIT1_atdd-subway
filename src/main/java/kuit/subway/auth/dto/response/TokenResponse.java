package kuit.subway.auth.dto.response;

import kuit.subway.member.domain.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenResponse {

    private String email;
    private String accessToken;

    public static TokenResponse of(Member member, String accessToken) {
        return TokenResponse.builder()
                .email(member.getEmail())
                .accessToken(accessToken)
                .build();
    }
}
