package kuit.subway.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomExceptionStatus implements ExceptionStatus {

    // common exception
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "검증조건을 만족하지 못하는 요청이 들어왔습니다."),
    TYPE_MISS_MATCH(HttpStatus.BAD_REQUEST, "잘못된 타입으로 요청이 들어왔습니다."),
    INVALID_URL(HttpStatus.NOT_FOUND, "잘못된 URL 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // station exception
    DUPLICATED_STATION(HttpStatus.BAD_REQUEST, "중복된 이름의 역이 존재합니다."),
    NOT_EXISTED_STATION(HttpStatus.BAD_REQUEST, "존재하지 않는 역입니다."),

    // line exception
    NOT_EXISTED_LINE(HttpStatus.BAD_REQUEST, "존재하지 않는 노선입니다."),
    DUPLICATED_UP_STATION_AND_DOWN_STATION(HttpStatus.BAD_REQUEST, "상행종점역과 하행종점역이 중복됩니다."),

    // section exception
    INVALID_SECTION_NOT_EXISTED_DOWN_STATION(HttpStatus.BAD_REQUEST, "새로운 구간의 상행역은 해당 노선에 등록되어있는 하행 종점역이여야 합니다."),
    INVALID_SECTION_CANNOT_CYCLE(HttpStatus.BAD_REQUEST, "새로운 구간의 하행역은 해당 노선에 등록되어있을 수 없습니다."),
    CANNOT_REMOVE_SECTION(HttpStatus.BAD_REQUEST, "상행 종점역과 하행 종점역만 있는 경우, 구간을 삭제할 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}