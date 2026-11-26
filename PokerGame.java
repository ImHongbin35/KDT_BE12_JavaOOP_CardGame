import java.util.*;
import java.util.stream.Collectors;

public class PokerGame {
    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        gameManager.startGame();
    }
}

class GameManager {
    private List<Player> players = new ArrayList<>();
    private List<Player> originalPlayers = new ArrayList<>();
    private Dealer dealer = new Dealer();
    private int roundCount = 0;
    private final Scanner scanner = new Scanner(System.in);

    public void startGame() {
        initializePlayers();
        runTournament();
        displayFinalResults();
    }

    private void initializePlayers() {
        String namePattern = "^[a-zA-Z가-힣]{1,20}$";
        
        System.out.println("=== 플레이어 이름 입력 ===");
        System.out.println("최소 2명, 최대 4명까지 플레이 가능합니다.");
        
        for (int i = 1; i <= 4; i++) {
            while (true) {
                if (i >= 3) {
                    System.out.printf("플레이어 %d 이름 입력 (이름 없이 엔터를 치면 게임이 시작됩니다): ", i);
                } else {
                    System.out.printf("플레이어 %d 이름 입력: ", i);
                }
                
                String name = scanner.nextLine().trim();
                
                // 빈 입력 처리
                if (name.isEmpty()) {
                    if (players.size() >= 2) {
                        System.out.printf("\n총 %d명의 플레이어로 게임을 시작합니다!\n", players.size());
                        return;
                    } else {
                        System.out.println("최소 2명의 플레이어가 필요합니다. 이름을 입력해주세요.");
                        continue;
                    }
                }
                
                if (name.matches(namePattern)) {
                    boolean isDuplicate = players.stream()
                        .anyMatch(p -> p.getName().equals(name));
                    
                    if (!isDuplicate) {
                        Player player = new Player(name);
                        players.add(player);
                        originalPlayers.add(player);
                        System.out.printf("%s 플레이어가 등록되었습니다. (초기 자금: 10,000원)\n", name);
                        break;
                    } else {
                        System.out.println("이미 존재하는 이름입니다. 다른 이름을 입력해주세요.");
                    }
                } else {
                    System.out.println("잘못된 입력! 한글/영어 20자 이내로 입력해주세요.");
                }
            }
        }
        System.out.printf("\n총 %d명의 플레이어로 게임을 시작합니다!\n", players.size());
    }

    private void runTournament() {
        System.out.println("\n=== 100게임 토너먼트 시작! ===\n");
        
        for (int gameNum = 1; gameNum <= 100; gameNum++) {
            roundCount++;
            
            dealer.dealCards(players);
            Player winner = evaluateHands();
            distributePot(winner);
            winner.addWin();
            
            players.forEach(p -> {
                if (p != winner) {
                    p.addLoss();
                }
            });
            
            if (gameNum % 10 == 0) {
                System.out.printf("게임 %d/100 완료\n", gameNum);
                printCurrentStandings();
            }
        }
    }

    private Player evaluateHands() {
        return players.stream()
            .max(Comparator.comparingLong(p -> HandEvaluator.evaluate(p.getHand())))
            .orElseThrow();
    }

    private void distributePot(Player winner) {
        int pot = players.size() * 100;
        players.forEach(p -> p.deductMoney(100));
        winner.addMoney(pot);
    }

    private void printCurrentStandings() {
        System.out.println("\n현재 순위:");
        System.out.println("순위\t이름\t\t승수\t패수\t자금");
        System.out.println("----------------------------------------");
        
        List<Player> sortedPlayers = originalPlayers.stream()
            .sorted((p1, p2) -> {
                if (p1.getWins() != p2.getWins()) {
                    return p2.getWins() - p1.getWins();
                }
                return p2.getMoney() - p1.getMoney();
            })
            .collect(Collectors.toList());
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            System.out.printf("%d\t%-10s\t%d\t%d\t%,d\n", 
                i + 1, p.getName(), p.getWins(), p.getLosses(), p.getMoney());
        }
        System.out.println();
    }

    private void displayFinalResults() {
        System.out.println("\n=== 최종 결과 ===");
        System.out.println("100게임 토너먼트 결과를 승리 수 기준으로 정렬합니다.\n");
        
        List<Player> finalRanking = originalPlayers.stream()
            .sorted((p1, p2) -> {
                if (p1.getWins() != p2.getWins()) {
                    return p2.getWins() - p1.getWins();
                }
                return p2.getMoney() - p1.getMoney();
            })
            .collect(Collectors.toList());
        
        System.out.println("최종 순위\t이름\t\t승수\t패수\t승률\t\t최종 자금");
        System.out.println("================================================================");
        
        for (int i = 0; i < finalRanking.size(); i++) {
            Player p = finalRanking.get(i);
            double winRate = (double) p.getWins() / (p.getWins() + p.getLosses()) * 100;
            System.out.printf("%d위\t\t%-10s\t%d\t%d\t%.1f%%\t\t%,d원\n", 
                i + 1, p.getName(), p.getWins(), p.getLosses(), winRate, p.getMoney());
        }
        
        System.out.println("\n🏆 토너먼트 우승자: " + finalRanking.get(0).getName() + " 🏆");
    }
}

class Dealer {
    private Deck deck = new Deck();

    public void dealCards(List<Player> players) {
        deck.initialize();
        players.forEach(player -> {
            player.clearHand();
            for (int i = 0; i < 5; i++) {
                player.receiveCard(deck.drawCard());
            }
        });
    }
}

class Deck {
    private List<Card> cards = new ArrayList<>();

    public void initialize() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("덱에 카드가 없습니다!");
        }
        return cards.remove(0);
    }
}

class Player {
    private String name;
    private List<Card> hand = new ArrayList<>();
    private int money = 10000;
    private int wins = 0;
    private int losses = 0;

    public Player(String name) {
        this.name = name;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void clearHand() {
        hand.clear();
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public void deductMoney(int amount) {
        money = Math.max(money - amount, 0);
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    public String getName() { return name; }
    public int getMoney() { return money; }
    public List<Card> getHand() { return hand; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
}

class Card {
    private Suit suit;
    private Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    @Override
    public String toString() {
        return rank.toString() + suit.getSymbol();
    }
}

enum Suit { 
    SPADES("♠"), HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣");
    
    private String symbol;
    
    Suit(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
}

enum Rank {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
    NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(14);

    private int value;

    Rank(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

class HandEvaluator {
    public static long evaluate(List<Card> hand) {
        if (hand.size() != 5) {
            throw new IllegalArgumentException("핸드는 정확히 5장이어야 합니다.");
        }

        List<Card> sorted = hand.stream()
            .sorted(Comparator.comparingInt(c -> c.getRank().getValue()))
            .collect(Collectors.toList());

        long primaryScore = calculatePrimaryScore(sorted);
        long kickerScore = calculateKickers(sorted);
        
        return primaryScore * 1_000_000 + kickerScore;
    }

    private static long calculatePrimaryScore(List<Card> sorted) {
        boolean isFlush = isFlush(sorted);
        boolean isStraight = isStraight(sorted);
        int[] counts = getRankCounts(sorted);

        if (isFlush && isStraight) {
            if (sorted.get(4).getRank() == Rank.ACE && sorted.get(0).getRank() == Rank.TEN) {
                return 10; // 로얄 스트레이트 플러쉬
            }
            return 9; // 스트레이트 플러쉬
        }
        if (counts[0] == 4) return 8; // 포카드
        if (counts[0] == 3 && counts[1] == 2) return 7; // 풀하우스
        if (isFlush) return 6; // 플러쉬
        if (isStraight) return 5; // 스트레이트
        if (counts[0] == 3) return 4; // 트리플
        if (counts[0] == 2 && counts[1] == 2) return 3; // 투페어
        if (counts[0] == 2) return 2; // 원페어
        return 1; // 하이카드
    }

    private static long calculateKickers(List<Card> sorted) {
        return sorted.stream()
            .sorted((c1, c2) -> c2.getRank().getValue() - c1.getRank().getValue())
            .mapToLong(c -> c.getRank().getValue())
            .reduce(0, (a, b) -> a * 100 + b);
    }

    private static boolean isFlush(List<Card> cards) {
        return cards.stream()
            .map(Card::getSuit)
            .distinct()
            .count() == 1;
    }

    private static boolean isStraight(List<Card> cards) {
        int[] values = cards.stream()
            .mapToInt(c -> c.getRank().getValue())
            .sorted()
            .toArray();
        
        // 일반 스트레이트 확인
        for (int i = 1; i < values.length; i++) {
            if (values[i] - values[i-1] != 1) {
                // A-2-3-4-5 (마운틴) 확인
                if (Arrays.equals(values, new int[]{2, 3, 4, 5, 14})) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private static int[] getRankCounts(List<Card> cards) {
        Map<Rank, Integer> countMap = new HashMap<>();
        
        for (Card card : cards) {
            countMap.put(card.getRank(), countMap.getOrDefault(card.getRank(), 0) + 1);
        }
        
        return countMap.values().stream()
            .sorted(Collections.reverseOrder())
            .mapToInt(Integer::intValue)
            .toArray();
    }
}