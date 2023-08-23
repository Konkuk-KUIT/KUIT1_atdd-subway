package kuit.subway.utils.fixtures;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kuit.subway.dto.request.station.StationCreateRequest;

import static kuit.subway.study.common.CommonRestAssured.*;
import static kuit.subway.utils.steps.StationStep.지하철_역_생성_요청;

public class StationFixtures {

    public static final String STATION_PATH = "/stations";

    public static ExtractableResponse<Response> 지하철_역_등록(String name) {
        StationCreateRequest req = 지하철_역_생성_요청(name);
        return post(STATION_PATH, req);
    }

    public static ExtractableResponse<Response> 지하철_역_조회() {
        return get(STATION_PATH);
    }

    public static ExtractableResponse<Response> 지하철_역_삭제(Long id) {
        return delete(STATION_PATH + "/" + id);
    }
}