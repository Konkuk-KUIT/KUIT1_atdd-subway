package kuit.subway.service;

import kuit.subway.domain.Station;
import kuit.subway.dto.request.station.StationCreateRequest;
import kuit.subway.dto.response.station.StationCreateResponse;
import kuit.subway.dto.response.station.StationDeleteResponse;
import kuit.subway.dto.response.station.StationReadResponse;
import kuit.subway.exception.notfound.station.NotFoundStationException;
import kuit.subway.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;

    @Transactional
    public StationCreateResponse addStation(StationCreateRequest res) {
        Station station = Station.createStation(res.getName());
        stationRepository.save(station);

        return new StationCreateResponse("지하철 역 추가 완료", station.getId());
    }
    public List<StationReadResponse> findStations() {
        List<Station> findStations = stationRepository.findAll();
        List<StationReadResponse> result = findStations.stream()
                .map(station -> StationReadResponse.createStationDto(station.getId(), station.getName()))
                .collect(Collectors.toList());
        return result;
    }

    public StationReadResponse findStationById(Long id) {
        Station station = validateStationExist(id);
        return StationReadResponse.createStationDto(station.getId(), station.getName());
    }

    @Transactional
    public StationDeleteResponse deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(NotFoundStationException::new);

        stationRepository.delete(station);
        return new StationDeleteResponse("지하철 역 삭제 완료", station.getId());
    }

    // 존재하는 역인지 판별해주는 함수
    private Station validateStationExist(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(NotFoundStationException::new);
    }


}
