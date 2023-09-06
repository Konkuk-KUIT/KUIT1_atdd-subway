package kuit.subway.study.auth;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kuit.subway.AcceptanceTest;
import kuit.subway.dto.response.auth.TokenResponse;
import org.junit.jupiter.api.*;

import static kuit.subway.utils.step.AuthStep.로그인_회원_토근_생성;
import static kuit.subway.utils.step.MemberStep.내_회원_정보_요청;
import static kuit.subway.utils.step.MemberStep.회원_생성;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("로그인 및 개인정보 조회 인수 테스트")
public class AuthAcceptanceTest extends AcceptanceTest {

    @Nested
    @DisplayName("로그인 인수 테스트")
    class Login {

        ExtractableResponse<Response> memberCreateRes;

        @BeforeEach
        void setUp() {
            memberCreateRes = 회원_생성(20, "shin@gmail.com", "123");
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("아이디와 패스워드를 이용하여 토큰 생성 후, 201 Created를 반환한다.")
            void createToken() {

                // given
                String email = memberCreateRes.jsonPath().getString("email");
                String password = "123";

                // when
                ExtractableResponse<Response> 토큰_생성_결과 = 로그인_회원_토근_생성(email, password);

                // then
                assertEquals(201, 토큰_생성_결과.statusCode());
            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("존재하지 않는 이메일로 로그인 시도 시, 404 Not Found Error를 반환한다.")
            void LoginFail1() {

                // given
                String email = "wrong@gmail.com";
                String password = "123";

                // when
                ExtractableResponse<Response> 로그인_결과 = 로그인_회원_토근_생성(email, password);

                // then
                assertEquals(404, 로그인_결과.statusCode());
            }

            @Test
            @DisplayName("존재하지 않는 비밀번호로 로그인 시도 시, 401 Unauthorized를 반환한다.")
            void LoginFail2() {

                // given
                String email = memberCreateRes.jsonPath().getString("email");
                String password = "1234";

                // when
                ExtractableResponse<Response> 로그인_결과 = 로그인_회원_토근_생성(email, password);

                // then
                assertEquals(401, 로그인_결과.statusCode());
            }

        }
    }

    @Nested
    @DisplayName("내 정보 조회 인수 테스트")
    class GetMyInfo {

        TokenResponse token;
        @BeforeEach
        void setUp() {
            회원_생성(20, "shin@gmail.com", "123");
            ExtractableResponse<Response> tokenRes = 로그인_회원_토근_생성("shin@gmail.com", "123");
            token = tokenRes.as(TokenResponse.class);
        }

        @Nested
        @DisplayName("정보 조회 성공")
        class SuccessCase {

            @Test
            @DisplayName("로그인 시 생성된 토큰을 이용하여 내 정보 조회")
            void getMyInfoSuccess() {

                // given
                ExtractableResponse<Response> 내_회원_정보_요청_결과 = 내_회원_정보_요청(token);

                // when
                // then
                assertAll(() -> {
                    assertEquals(200, 내_회원_정보_요청_결과.statusCode());
                    assertEquals(20, 내_회원_정보_요청_결과.jsonPath().getInt("age"));
                    assertEquals("shin@gmail.com", 내_회원_정보_요청_결과.jsonPath().getString("email"));
                });
            }


        }

        @Nested
        @DisplayName("정보 조회 실패")
        class FailCase {

        }

    }
}