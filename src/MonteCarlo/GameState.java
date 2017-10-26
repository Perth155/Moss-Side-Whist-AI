import java.util.*;

/**
 * Created by Robbie on 24/10/2017.
 */
public class GameState implements Cloneable {
    private List<Card> playedCards;    // Cards that have been played so far in this trick.
    private List<Card> playersCards;   // Cards available to the player
    private List<Card> remainingCards; // Cards that the player has not yet seen
    private int currentTurn;
    private int playerTurn;
    private int leader;

    public GameState(List<Card> playedCards, List<Card> playersCards, List<Card> remainingCards, int currentTurn, int playerTurn){
        this.playedCards = playedCards;
        this.playersCards = playersCards;
        this.remainingCards = remainingCards;
        this.currentTurn = currentTurn;
        this.playerTurn = playerTurn;
    }

    public GameState(GameState state){
        // Need to clone all the arrays.
        playedCards = new ArrayList<Card>(state.getPlayedCards());
        playersCards = new ArrayList<Card>(state.getPlayersCards());
        remainingCards = new ArrayList<Card>(state.getRemainingCards());
        this.currentTurn = state.getCurrentTurn();
        this.playerTurn = state.getPlayerTurn();
    }

    public void playCard(Card card) {
        if (isPlayerTurn()) {
            playersCards.remove(card);
        } else {
            remainingCards.remove(card);
        }
        playedCards.add(card);
        incrementTurn();
    }

    public void playCard() {
        Random random = new Random();
        if (isPlayerTurn()) {
            playCard(playersCards.get(random.nextInt(playersCards.size())));
        } else {
            playCard(remainingCards.get(random.nextInt(remainingCards.size())));
        }
    }

    public Object clone(){
        return new GameState(this);
    }

    public boolean playerWonTrick() {
        if (!isTrickFinished()) {
            throw new IllegalStateException("This trick has not finished yet.");
        } else {
            Card playersCard = playedCards.get(playerTurn);
            Collections.sort(playedCards, new CardComparator());
            return playedCards.get(2) == playersCard;
        }
    }

    public boolean isTrickFinished() {
        int size = playedCards.size();
        if (size > 3) {
            throw new IllegalStateException("More than 3 cards have been played in this trick");
        }
        return size == 3;
    }

    public List<Card> getPlayedCards() {
        return playedCards;
    }

    public List<Card> getPlayersCards() {
        return playersCards;
    }

    public List<Card> getRemainingCards() {
        return remainingCards;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(int turn) {
        playerTurn = turn;
    }

    public void incrementTurn() {
        currentTurn = (currentTurn + 1) % 3;
    }

    public void reset() {
        currentTurn = 0;
        playedCards = new ArrayList<Card>();
    }

    public boolean isPlayerTurn(){
        return (currentTurn == playerTurn);
    }

    public void print() {
        System.out.println("Player Turn: " + playerTurn);
        System.out.println("Current Turn: " + currentTurn);
    }
}
