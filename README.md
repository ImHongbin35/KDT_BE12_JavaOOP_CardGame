# KDT_BE12_JavaOOP_CardGame

Java OOP 학습용 포커 게임 콘솔 프로그램입니다.

## 구성

`PokerGame.java` 한 파일에 다음 클래스들이 정의되어 있습니다.

- `PokerGame` — 메인 진입점
- `GameManager` — 게임 진행(플레이어 등록, 토너먼트 진행, 결과 출력) 총괄
- `Player`, `Dealer` — 참가자
- `Deck`, `Card`, `Suit`, `Rank` — 카드 모델
- `HandEvaluator` — 포커 핸드 평가 로직

## 실행

```bash
javac PokerGame.java
java PokerGame
```
