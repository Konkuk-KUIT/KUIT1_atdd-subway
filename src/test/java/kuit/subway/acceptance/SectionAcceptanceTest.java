package kuit.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kuit.subway.request.section.DeleteSectionRequest;
import kuit.subway.request.section.SectionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kuit.subway.acceptance.fixtures.LineStep.지하철_2호선_생성_요청;
import static kuit.subway.acceptance.fixtures.SectionStep.지하철_구간_등록_요청;
import static kuit.subway.acceptance.fixtures.SectionStep.지하철_구간_삭제_요청;
import static kuit.subway.acceptance.fixtures.StationFixture.*;
import static kuit.subway.acceptance.fixtures.StationStep.지하철_역_생성_요청;
import static kuit.subway.utils.BaseResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SectionAcceptanceTest extends AcceptanceTest {
    private static final String ID_PATH = "result.id";
    private static final String RESPONSECODE = "responseCode";
    @Test
    void 노선에_구간_등록_요청_테스트() {
        // given
        지하철_2호선_생성_요청(성수역, 강남역);
        지하철_역_생성_요청(교대역);

        // when
        SectionRequest request = new SectionRequest(10L, 3L, 2L);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청("1", request);

        // then
        assertEquals(1L, response.jsonPath().getLong(ID_PATH));
        assertEquals(200, response.statusCode());
        assertEquals(REGISTERED_SUCCESS.getResponseCode(), response.jsonPath().getLong(RESPONSECODE));
    }

    @Test
    @DisplayName("상행종점역의 상행에 구간 추가")
    void addSectionFromLastUpStation() {
        // given
        지하철_2호선_생성_요청(성수역, 강남역);
        지하철_역_생성_요청(뚝섬역);

        /* when
           새로운 구간의 상행역이 해당 노선에 등록되어있는 하행 종점역이 아닌 경우
           기존 노선의 상행종점역에 상행역 추가
         */
        SectionRequest request = new SectionRequest(5L, 1L, 3L);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청("1", request);

        // then
        // then - 성공
        assertEquals(200, response.statusCode());
        assertThat(response.jsonPath().getList("result.stations"))
                .extracting("name")
                .containsExactly("뚝섬역", "성수역", "강남역");
    }

    @Test
    void 노선에_구간_등록_요청_테스트_WHEN_존재하는_역() {
        // given
        지하철_2호선_생성_요청(성수역, 강남역);

        /* when
           새로운 구간의 하행역이 해당 노선에 등록되어있는 역인 경우 등록 불가
         */
        SectionRequest request = new SectionRequest(5L, 1L, 2L);
        ExtractableResponse<Response> response = 지하철_구간_등록_요청("1", request);

        // then
        assertEquals(400, response.statusCode());
        assertEquals(ALREADY_REGISTERED_SECTION.getResponseCode(), response.jsonPath().getLong(RESPONSECODE));
    }


    @Test
    void 구간_삭제_WHEN_마지막구간(){
        // given
        지하철_2호선_생성_요청(강남역, 성수역);
        지하철_역_생성_요청(교대역);
        SectionRequest request = new SectionRequest(10L, 3L, 2L);
        지하철_구간_등록_요청("1", request);

        // when
        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(3L);
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청("1", deleteSectionRequest);

        // then
        assertEquals(204, response.statusCode());
    }

    // TODO 일단은 중간역 삭제 안됨.
    @Test
    void 구간_삭제_WHEN_중간구간(){
        // given
        지하철_2호선_생성_요청(성수역, 강남역);
        지하철_역_생성_요청(교대역);
        SectionRequest request = new SectionRequest(10L, 3L, 2L);
        지하철_구간_등록_요청("1", request);

        // when
//        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(2L, 1L);
        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(2L);
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청("1", deleteSectionRequest);

        // then
        assertEquals(400, response.statusCode());
        assertEquals(ONLY_LAST_SECTION_DELETION_ALLOWED.getResponseCode(), response.jsonPath().getLong(RESPONSECODE));
    }

    @Test
    void 구간_삭제_WHEN_싱글구간(){
        // given
        지하철_2호선_생성_요청(성수역, 강남역);

        // when
//        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(2L, 1L);
        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(2L);
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청("1", deleteSectionRequest);

        // then
        assertEquals(400, response.statusCode());
        assertEquals(CANNOT_DELETE_SECTION.getResponseCode(), response.jsonPath().getLong(RESPONSECODE));
    }



}
