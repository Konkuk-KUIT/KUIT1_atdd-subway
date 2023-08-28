package kuit.subway.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import kuit.subway.dto.response.station.StationReadResponse;
import kuit.subway.exception.badrequest.section.create.*;
import kuit.subway.exception.badrequest.section.delete.InvalidSectionDeleteOnlyTwoStationsException;
import kuit.subway.exception.badrequest.section.delete.InvalidSectionDeleteStationNotExist;
import kuit.subway.exception.notfound.section.NotFoundSectionHavingCycleException;

import java.util.*;
import java.util.stream.Collectors;

@Embeddable
public class Sections {

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Section> sections = new ArrayList<>();

    public void addSection(Section section) {
        // 구간을 처음 추가하는 경우는 validate 할 필요 X
        if (sections.size() == 0) {
            this.sections.add(section);
        } else {
            validateSectionCreateBothNotExist(section);
            validateSectionCreateBothExist(section);
            validateSectionCreateLengthLonger(section);
            if (sections.size() > 0) {
                // 구간 추가 시, 고려해야 할 예외사항 먼저 체크
                if (validateSectionCreateFirstStation(section)) {
                    // 새로운 역을 상행 종점으로 등록할 경우
                    this.sections.add(0, section);
                } else if (validateSectionCreateFinalStation(section)) {
                    // 새로운 역을 하행 종점으로 등록할 경우
                    this.sections.add(section);
                } else {
                    // 사이 삽입 : 4가지 경우 존재
                    // 1. 새로 삽입 상행이 기존의 상행에 존재 : found 이전 자리에 삽입 후, found 업데이트
                    // 1 2 <= found
                    // 1 3 <= new
                    // 2 5

                    // 2. 새로 삽입 상행이 기존의 하행에 존재 : found 다음 자리에 삽입 후, new 다음걸 업데이트
                    // 1 2 <= found
                    // 2 3 <= new
                    // 2 5

                    // 3. 새로 삽입 하행이 기존의 상행에 존재 : found 이전 자리에 삽입 후, new 이전걸 업데이트
                    // 1 2
                    // 3 2 <= new
                    // 2 5 <= found

                    // 4. 새로 삽입 하행이 기존의 하행에 존재 : found 다음 자리에 삽입 후, found 업데이트
                    // 1 2
                    // 3 5 <= new
                    // 2 5 <= found

                    // 상행역이 이미 존재하는 역인지, 혹은 하행역이 존재하는 역인지 판별
                    Boolean isUpExist = verifyIsUpExist(section);
                    Section findSection;
                    // 새로운 상행이 이미 존재하는 경우
                    if (isUpExist) {
                        // 1 2
                        // 2 3 <= new
                        // 2 5 <= found

                        // 사이에 끼울 경우, 각 기존 구간의 상행역 & 하행역을 신규 구간 정보로 잘 변경
                        findSection = findMatchUpSection(section.getUpStation()).get();

                    } else {
                        // 새로운 하행이 기존 하행으로 존재할 경우
                        // 1 2
                        // 2 5 <= found
                        // 3 5 <= new
                        findSection = findMatchDownSection(section.getDownStation()).get();
                    }
                    adjustSectionBetweenStations(findSection, section, isUpExist);
                }

            }
        }
    }

    private void adjustSectionBetweenStations(Section findSection, Section requestSection, Boolean isUpExist) {
        int index = this.sections.indexOf(findSection);
        int findDistance = findSection.getDistance();
        int newDistance = requestSection.getDistance();

        // 새롭게 추가할 상행역, 하행역
        Station newUpStation = requestSection.getUpStation();
        Station newDownStation = requestSection.getDownStation();

        // 추가해줄 구간 생성
        if(isUpExist){
            Section newSection = Section.createSection(findSection.getLine(), newUpStation, newDownStation, newDistance);
            this.sections.add(index, newSection);

            // 기존 구간 정보 갱신
            findSection.updateSection(newDownStation, findSection.getDownStation(), findDistance - newDistance);
        } else {
            // 추가해줄 구간 생성
            Section newSection = Section.createSection(findSection.getLine(), newUpStation, newDownStation, newDistance);
            this.sections.add(index + 1, newSection);

            // 기존 구간 정보 갱신
            findSection.updateSection(findSection.getUpStation(), newUpStation, findDistance - newDistance);
        }

    }

    // 구간들 상행 종점역 기준으로 정렬한 후, 정렬된 구간 리스트를 반환해주는 함수
    public List<Section> getOrderSections() {
        Section startSection = findStartSection();
        Map<Station, Section> upStationAndSectionRoute = getSectionRoute();
        List<Section> orderedSections = new ArrayList<>();
        Section nextSection = startSection;
        while (nextSection != null) {
            orderedSections.add(nextSection);
            Station curDownStation = nextSection.getDownStation();
            nextSection = upStationAndSectionRoute.get(curDownStation);
        }

        return orderedSections;
    }

    // 구간들 상행 종점역 기준으로 정렬한 후, 정렬된 역 리스트를 반환해주는 함수
    public List<StationReadResponse> getOrderStations() {
        Section startSection = findStartSection();
        Map<Station, Section> upStationAndSectionRoute = getSectionRoute();
        List<Section> orderedSections = new ArrayList<>();
        Section nextSection = startSection;
        while (nextSection != null) {
            orderedSections.add(nextSection);
            Station curDownStation = nextSection.getDownStation();
            nextSection = upStationAndSectionRoute.get(curDownStation);
        }

        return getStations(orderedSections);
    }

    private Section findStartSection() {
        Set<Station> downStations = sections.stream()
                .map(Section::getDownStation)
                .collect(Collectors.toSet());
        // 전체 상행역 중 하행역이 아닌 상행역 추출(=> 시작점)
        return sections.stream()
                .filter(section -> !downStations.contains(section.getUpStation()))
                .findFirst()
                .orElseThrow(() -> new NotFoundSectionHavingCycleException());
    }

    private Map<Station, Section> getSectionRoute() {
        return sections.stream()
                .collect(
                        Collectors.toMap(Section::getUpStation,
                                section -> section,
                                (stationKey1, stationKey2) -> stationKey1,
                                HashMap::new));
    }


    public void deleteSection(Station deleteStation) {

        if (sections.size() > 1) {
            validateSectionDeleteStationNotExist(deleteStation);

            if (verfiyIsLastStationDelete(deleteStation)) {
                // 하행 종점 제거
                this.sections.remove(sections.size() - 1);
            } else if (verifyIsFirstStationDelete(deleteStation)){
                // 상행 종점 제거
                this.sections.remove(0);
            } else {
                // 중간역 제거
                // A B
                // B C
                Section findSection = sections.stream()
                        .filter(s -> s.getDownStation().equals(deleteStation))
                        .findFirst().get();
                
                // 거리 계산을 위해 다음 구간 가져오기
                int index = sections.indexOf(findSection);
                Section nextSection = sections.get(index + 1);
                
                // 거리 계산까지 하여 findSection 업데이트
                findSection.updateSection(findSection.getUpStation(), nextSection.getDownStation(), findSection.getDistance() + nextSection.getDistance());
                
            }

        } else if (sections.size() == 1){
            // 구간이 하나인 노선에서 구간 제거 불가
            throw new InvalidSectionDeleteOnlyTwoStationsException();
        }
        this.sections.removeIf(section -> (section.getDownStation().equals(deleteStation)));

    }

    private List<StationReadResponse> getStations(List<Section> sections) {

        List<StationReadResponse> result = new ArrayList<>();
        Long nextUpStationId;

        // 맨 처음 첫 구간은 상행, 하행 둘 다 삽입
        Station upStation = sections.get(0).getUpStation();
        result.add(StationReadResponse.of(upStation));

        Station downStation = sections.get(0).getDownStation();
        result.add(StationReadResponse.of(downStation));

        nextUpStationId = downStation.getId();

        for (int i = 0; i < sections.size() - 1; i++) {
            Long finalNextUpStationId = nextUpStationId;
            Section findSection = sections.stream()
                    .filter(section -> section.getUpStation().getId().equals(finalNextUpStationId))
                    .findFirst().get();
            System.out.println(findSection.getDownStation().getId());
            downStation = findSection.getDownStation();
            result.add(StationReadResponse.of(downStation));
            nextUpStationId = downStation.getId();
        }

        return result;
    }


    // 주어진 상행역을 이미 상행역으로 가지고 있는 구간 반환
    private Optional<Section> findMatchUpSection(Station upStaiton) {
        return this.sections.stream()
                .filter(s -> s.getUpStation().equals(upStaiton))
                .findFirst();
    }

    // 주어진 하행역을 이미 상행역으로 가지고 있는 구간 반환
    private Optional<Section> findMatchDownSection(Station downStation) {
        return this.sections.stream()
                .filter(s -> s.getDownStation().equals(downStation))
                .findFirst();
    }

    // 새로운 구간의 상행역이 등록되어있는 하행 종점역이면, 새로운 역을 하행 종점으로 등록할 경우
    private Boolean validateSectionCreateFirstStation(Section section) {
        Section firstSection = sections.get(0);
        if(firstSection.getUpStation().equals(section.getDownStation())) {
            return true;
        } else {
            return false;
        }
    }

    // 새로운 구간의 상행역이 등록되어있는 하행 종점역이면, 새로운 역을 하행 종점으로 등록할 경우
    private Boolean validateSectionCreateFinalStation(Section section) {
        Section lastSection = getOrderSections().get(this.sections.size() - 1);
        if(lastSection.getDownStation().equals(section.getUpStation())) {
            System.out.println("create final station!");

            return true;
        } else {
            System.out.println("create nope station!");

            return false;
        }
    }

    // 상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가 불가
    private void validateSectionCreateBothNotExist(Section section) {
        boolean upStationExist = sections.stream().anyMatch(existedSection ->
                existedSection.getUpStation().equals(section.getUpStation()) || existedSection.getDownStation().equals(section.getUpStation()));
        boolean downStationExist =sections.stream().anyMatch(existedSection ->
                existedSection.getUpStation().equals(section.getDownStation()) || existedSection.getDownStation().equals(section.getDownStation()));
        if (!upStationExist && !downStationExist) {
            throw new InvalidSectionCreateBothNotExistExcpetion();
        }
    }

    // 상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가 불가
    private void validateSectionCreateBothExist(Section section) {
        boolean upStationExist = sections.stream().anyMatch(existedSection ->
                existedSection.getUpStation().equals(section.getUpStation()) || existedSection.getDownStation().equals(section.getUpStation()));
        boolean downStationExist =sections.stream().anyMatch(existedSection ->
                existedSection.getUpStation().equals(section.getDownStation()) || existedSection.getDownStation().equals(section.getDownStation()));
        if (upStationExist && downStationExist) {
            throw new InvalidSectionCreateBothExistException();
        }
    }

    // 역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 추가 불가
    private void validateSectionCreateLengthLonger(Section section) {
        Optional<Section> upExist = this.sections.stream()
                .filter(s -> s.getUpStation().equals(section.getUpStation()))
                .findAny();

        Optional<Section> downExist = this.sections.stream()
                .filter(s -> s.getDownStation().equals(section.getDownStation()))
                .findAny();


        if ((upExist.isPresent() && upExist.get().getDistance() <= section.getDistance()) ||
                (downExist.isPresent() && downExist.get().getDistance() <= section.getDistance())) {
            throw new InvalidSectionCreateLengthLongerException();
        }
    }

    // 노선에 등록되어 있지 않은 역은 제거 불가
    private void validateSectionDeleteStationNotExist(Station deleteStation) {
        Boolean isExist = this.sections.stream()
                .anyMatch(s -> s.getUpStation().equals(deleteStation) || s.getDownStation().equals(deleteStation));

        if (!isExist) {
            throw new InvalidSectionDeleteStationNotExist();
        }
    }

    // 역 사이에 새로운 역 등록할 경우, 상행역, 혹은 하행역 중 어느 쪽이 이미 존재하는지 판별
    private Boolean verifyIsUpExist(Section section) {
        Optional<Section> existUp = this.sections.stream()
                .filter(s -> s.getUpStation().equals(section.getUpStation()))
                .findAny();

        if (existUp.isPresent()) {
            return true;
        } else
            return false;
    }

    // 하행 종점 제거인지 아닌지 판별해주는 함수
    private Boolean verfiyIsLastStationDelete(Station deleteStation) {
        return this.sections.get(sections.size() - 1).getDownStation().equals(deleteStation);
    }

    // 상행 종점 제거인지 아닌지 판별해주는 함수
    private Boolean verifyIsFirstStationDelete(Station deleteStation) {
        return this.sections.get(0).getUpStation().equals(deleteStation);
    }

}
