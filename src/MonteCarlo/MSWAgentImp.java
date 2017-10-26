import java.util.*;
import java.lang.RuntimeException;

public class MSWAgentImp implements MSWAgent{
    private String leftAgent;
    private String rightAgent;
    private GameState gameState;
    private String name;

    public MSWAgentImp(String name) {
        this.name = name;
    }

    public MSWAgentImp() {
        this.name = "My Agent";
    }


    /**
     * Tells the agent the names of the competing agents, and their relative position.
     * */
    public void setup(String agentLeft, String agentRight) {
        leftAgent = agentLeft;
        rightAgent = agentRight;
    }

    /**
     * Starts the round with a deal of the cards.
     * The agent is told the cards they have (16 cards, or 20 if they are the leader)
     * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
     * */
    public void seeHand(List<Card> hand, int order){
        ArrayList<Card> remainingCards = new ArrayList<Card>();
        // All cards (except the Agent's) are stored in the remainingCards List.
        for (Card card: Card.values()) {
            if (!hand.contains(card)) {
                remainingCards.add(card);
            }
        }
        gameState = new GameState(
                new ArrayList<Card>(),
                hand,
                remainingCards,
                0,
                order
        );
    }

    /**
     * This method will be called on the leader agent, after the deal.
     * If the agent is not the leader, it is sufficient to return an empty array.
     */
    public Card[] discard() {
        // Naive - Discard lowest 4 cards.
        // TODO - Discard cards properly
        if (!gameState.isPlayerTurn()) {
            return new Card[0];
        }

        // class CardComparator implements Comparator<Card> {
        //     public int compare(Card card1, Card card2) {
        //         return card1.rank - card2.rank;
        //     }
        // }

        List<Card> myCards = gameState.getPlayersCards();
        Collections.sort(myCards, new CardComparator());
        Card[] worstCards = myCards.subList(0, 4).toArray(new Card[0]);
        // Remove last 4 cards from sorted array:
        while (myCards.size() > 16) {
            myCards.remove(myCards.size()-1);
        }
        return worstCards;
    }

    /**
     * Agent returns the card they wish to play.
     * A 200 ms timelimit is given for this method
     * @return the Card they wish to play.
     * */
    public Card playCard() {
        long totalTime = 0;
        long timeLimit = 200;
        int nLoops = 0;
        long averageTime = 0;
        MonteCarloTree mct = new MonteCarloTree(gameState);

        while (timeLimit - totalTime > averageTime) {
            long startTime = System.currentTimeMillis();
            // Selection phase.
            MCTNode bestNode = mct.selectNode();

            // Expansion phase.
            MCTNode expandedNode = mct.expansionPhase(bestNode);

            // Simulation phase.
            boolean won = mct.simulateGame(expandedNode);

            //Back propogation phase.
            mct.backPropogate(expandedNode, won);
            long executionTime = System.currentTimeMillis() - startTime;
            nLoops++;
            totalTime += executionTime;
            averageTime = totalTime / (long)nLoops;
        }

        Card bestCard = mct.getBestCard();

        // Throw exception if the agent chose a card it does not have.
        if (!gameState.getPlayersCards().contains(bestCard)) {
            System.out.println("Invalid card chosen");
            System.out.println("Player chose " + bestCard);
            System.out.println("Player's cards: ");
            for (Card card : gameState.getPlayersCards()) {
                System.out.print(card + ", ");
            }
            throw new RuntimeException("Invalid card chosen");                
        }

        gameState.getPlayersCards().remove(bestCard);
        gameState.incrementTurn();
        return bestCard;
    }

    /**
     * Sees an Agent play a card.
     * A 50 ms timelimit is given to this function.
     * @param card, the Card played.
     * @param agent, the name of the agent who played the card.
     * */
    public void seeCard(Card card, String agent) {
        gameState.getRemainingCards().remove(card);
        gameState.getPlayedCards().add(card);
        if (!agent.equals(sayName()))
            gameState.incrementTurn();
    }

    /**
     * See the result of the trick.
     * A 50 ms timelimit is given to this method.
     * This method will be called on each eagent at the end of each trick.
     * @param winner, the player who played the winning card.
     * */
    public void seeResult(String winner){
        if (winner.equals(leftAgent)) {
            gameState.setPlayerTurn(2);
        }
        else if (winner.equals(rightAgent)) {
            gameState.setPlayerTurn(1);
        }
        else {
            gameState.setPlayerTurn(0);
        }
        gameState.reset();
        return;
    }

    /**
     * See the score for each player.
     * A 50 ms timelimit is givien to this method
     * @param scoreboard, a Map from agent names to their score.
     **/
    public void seeScore(Map<String, Integer> scoreboard) {
        // TODO - Something
        return;
    }

    /**
     * Returns the Agents name.
     * A 10ms timelimit is given here.
     * This method will only be called once.
     * */
    public String sayName() {
        return name; // TODO - Something better
    }
}
