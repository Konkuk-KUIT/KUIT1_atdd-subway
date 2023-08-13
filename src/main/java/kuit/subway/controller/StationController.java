package kuit.subway.controller;

import kuit.subway.request.PostStationRequest;
import kuit.subway.response.GetStationsResponse;
import kuit.subway.response.PostStationResponse;
import kuit.subway.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StationController {
    private final StationService stationService;

    @PostMapping("/stations")
    public PostStationResponse createStation(@RequestBody PostStationRequest postStationRequest){
        return stationService.createStation(postStationRequest.getName());
    }

    @GetMapping("/stations")
    public List<GetStationsResponse> createStation(){
        return stationService.getStations();
    }

    @DeleteMapping("/stations/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteStation(@PathVariable("id") Long id){
        stationService.deleteStation(id);
    }

}