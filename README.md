# atdd-subway
## Week2
### Step1. 구간 추가 제약사항 변경

> POST lines/{line-id}/sections 로 매핑
- request body
```
sectionType : Integer  // 0: 사이구간, 1:상행 종점, 2: 하행 종점
downStationId : Long
upStationId : Long
```
- response body
```
lineId : Long
```

- 추가 케이스 세분화
  - 상행 종점으로 등록할 경우 맨 상위 구간으로
  - 하행 종점으로 등록할 경우 맨 하위 구간으로
  - 사이 구간으로 등록할 경우를 각각 나누어서 적용
- 예외 케이스
  - 구간 request 로 들어온 상행역, 하행역이 해당 노선에 모두 존재하지 않으면 reject
  - 상행역과 하행역 모두 이미 구간에 등록되어 있다면 reject
  - 기존 구간보다 크거나 같은 거리의 구간 생성 요청은 reject

|| pseudo code
```agsl
Sections.java
// addSection
if ( sections 사이즈가 0 이면 ) {
    sections.add(section);
    return;
}
예외처리(상행역과 하행역 모두 이미 구간에 등록되어 있다면 reject)
예외처리(구간 request 로 들어온 상행역, 하행역이 해당 노선에 모두 존재하지 않으면 reject)
case ( 상행종점 구간 생성 요청 ) {
    addSectionAtFirst(Section);
}
case ( 하행종점 구간 생성 요청 ) {
    addSectionAtLast(Section);
}
case ( 사이구간 생성 요청 ) {
    addSectionBetween(Section);
}
```

### addSectionAtFirst
```agsl
private void addSectionAtFirst(...){
    if( 상행 종점역과, 새로 추가할 구간의 하행역이 다르면){
        예외발생!
    }
    sections.add(맨처음, 새 구간);
}
```

### addSectionAtLast
```agsl
private void addSectionAtLast(...){
    if( 하행 종점역과, 새로 추가할 구간의 상행역이 다르면){
        예외발생!
    }
    section.add(마지막, 새 구간);
}
```

### addSectionBetween
```agsl
private void addSectionBetween(...){
    for( i : sections 순회 ){
        for( station : section 내에 속한 상행역, 하행역에 대하여 ) {
            if (새로 추가할 구간의 상행역이 station 과 같다면 ) {
                i+1 번째에 구간 추가
                i 번째에 구간 추가
            }
        } 
    } 
}
```
