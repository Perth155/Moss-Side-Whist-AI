import java.util.*;
import java.lang.RuntimeException;
import java.security.InvalidParameterException;

public class MSWAgentImp implements MSWAgent{
    private String leftAgent;
    private String rightAgent;
    private GameState gameState;
    private String name;

    public MSWAgentImp() {
        this.name = "Monte Carlo";
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

        MCTNode bestNode = null;
        while (timeLimit - totalTime > averageTime) {
            long startTime = System.currentTimeMillis();
            // Selection phase.
            bestNode = mct.selectNode();

            // Expansion phase.
            MCTNode expandedNode = mct.expansionPhase(bestNode);

            // Simulation phase.
            boolean won = mct.simulateGame(expandedNode);

            //Back propogation phase.
            mct.backPropagate(expandedNode, won);
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

    private class MonteCarloTree {
        /**
        * A MonteCarloTree. Holds all the nodes and performs the MCT search phases.
        */

        private MCTNode rootNode;

        /**
        * @param node The root node for this tree.
        */
        public MonteCarloTree(MCTNode node) {
            rootNode = node;
        }

        /**
        * @param state The state to build the root node from
        */
        public MonteCarloTree(GameState state) {
            this(new MCTNode(state));
        }

        /**
         * Performs the selection phase of the monte carlo tree search
         * @param MCTNode The MCTNode to select the optimal child of.
         * @return The optimal child of this MCTNode
         */
        public MCTNode selectNode(MCTNode node) {
            if (node.getChildren().size() == 0) {
                return node;
            }

            // if (node == rootNode() && !node.getState().isPlayerTurn()) {
            //     node.getState().print();
            //     throw new IllegalArgumentException("This node does not represent the Player's turn");
            // }

            MCTNode bestNode = null;
            double bestNodeScore = -1;
            for (MCTNode child: node.getChildren()){
                if (child.getUpperConfidenceBound() > bestNodeScore) {
                    bestNode = child;
                    bestNodeScore = child.getUpperConfidenceBound();
                }
            }
            return selectNode(bestNode);
        }

        /**
         * Performs the selection phase of the monte carlo tree search.
         * @return The optimal child of the root MCTNode.
         */
        public MCTNode selectNode() {
            return selectNode(rootNode);
        }

        /**
        * Gets the best card to play for this MCT.
        * Perform all phases before calling this method.
        * @return the Card object to play
        */
        public Card getBestCard() {
            if (rootNode.getChildren().size() == 0) {
                throw new IllegalStateException("Required phases not yet completed");
            }

            if (!rootNode.getState().isPlayerTurn()) {
                throw new IllegalStateException("This node does not represent the Player's turn");
            }

            System.out.println("Potential plays: ");
            MCTNode bestNode = null;
            double bestNodeScore = -1;
            for (MCTNode child: rootNode.getChildren()){
                double score;
                if (child.getnVisits() != 0) {
                    score = (double)child.getnWins() / child.getnVisits();
                } else {
                    score = 0;
                }
                System.out.print("(" + child.getPlayedCard() + ", " + score + "), ");
                if (score > bestNodeScore) {
                    bestNode = child;
                    bestNodeScore = score;
                }
            }

            if (bestNodeScore == 0.0) {
                // Agent has no winning cards. Discard worse card
                bestNode = rootNode.getChildren().get(0);
                Card worstCard = bestNode.getPlayedCard();
                CardComparator cc = new CardComparator();
                for (MCTNode child: rootNode.getChildren()) {
                    Card card = child.getPlayedCard();
                    if (cc.compare(worstCard, card) > 0) {
                        worstCard = card;
                        bestNode = child;
                    }
                }
            }

            System.out.println();
            System.out.println("Playing (" + bestNode.getPlayedCard() + ", " + bestNodeScore + ")");
            return bestNode.getPlayedCard();
        }

        /**
         * Expands a node, appending all children.
         * @param node The node to be expanded.
         */
        public void expandNode(MCTNode node) {
            List<Card> cards;
            GameState state = node.getState();
            if (node.getChildren().size() != 0 || node.getState().isTrickFinished())
                return;

            if (state.isPlayerTurn())
                cards = node.getState().getPlayersCards();
            else
                cards = node.getState().getRemainingCards();

            cards = getLegalCards(node, cards);
            for (Card card: cards) {
                GameState newState = (GameState)node.getState().clone();
                newState.playCard(card);
                node.appendChild(newState);
            }
        }

        // Filter a list of cards to only include legal cards.
        private List<Card> getLegalCards(MCTNode node, List<Card> cards) {
            if (node.getState().getCurrentTurn() == 0) {
                ArrayList<Card> clonedCards = new ArrayList<Card>(cards.size());
                for (Card card: cards) {
                    clonedCards.add(card);
                }
                return clonedCards;
            } else {
                Card cardToFollow = node.getState().getPlayedCards().get(0);
                List<Card> legalCards = new ArrayList<Card>();
                for (Card card: cards) {
                    if (followsSuit(cardToFollow, card)) {
                        legalCards.add(card);
                    }
                }
                if (legalCards.size() == 0) {
                    ArrayList<Card> clonedCards = new ArrayList<Card>(cards.size());
                    for (Card card: cards) {
                        clonedCards.add(card);
                    }
                    return clonedCards;
                }
                return legalCards;
            }
        }

        /**
        * Perform the expansion phase on this MCT for a given node.
        * @param node The node to expand
        * @return A randomly selected newly created node
        */
        public MCTNode expansionPhase(MCTNode node) {
            if (node.getState().isTrickFinished())
                return node;
            else {
                expandNode(node);
                Random random = new Random();
                List<MCTNode> children = node.getChildren();
                return children.get(random.nextInt(children.size()));
            }
        }

        // From MossSideWhist class
        private boolean followsSuit(Card c1, Card c2){
            return c2.suit==c1.suit;
        }

        /**
         * Perform the simulation phase.
         * Randomly plays cards until the trick ends.
         * @param MCTNode The MCTNode to start the simulation from.
         * @return True if player won, False otherwise.
         */
        public boolean simulateGame(MCTNode node) {
            GameState state = node.getState();
            List<Card> playedCards = state.getPlayedCards();
            int playerTurn = state.getPlayerTurn();
            int opponentTurn1 = (playerTurn + 1) % 3;
            int opponentTurn2 = (playerTurn + 2) % 3;
            Card playerCard = playedCards.get(playerTurn);
            List<Card> remainingCards = state.getRemainingCards();

            int nRemaining = remainingCards.size();
            int handSize = Math.max(1, state.getPlayersCards().size());
            List<Card> hand2;

            Random r = new Random();
            CardComparator cc = new CardComparator();

            Card card1 = null;
            try {
                card1 = playedCards.get(opponentTurn1);
            } catch (IndexOutOfBoundsException e ) {
                // Opponent has not gone yet.
                // Deal a random hand and play a random valid card.
                List<Card> hand = new ArrayList<Card>(0);
                while (hand.size() < handSize) {
                    Card randomCard = remainingCards.get(r.nextInt(nRemaining));
                    while (hand.contains(randomCard)){
                        randomCard = remainingCards.get(r.nextInt(nRemaining));
                    }
                    hand.add(randomCard);
                }  
                List<Card> legalCards = new ArrayList<Card>();
                for (Card card: hand) {
                    legalCards.add(card);
                }
                if (legalCards.size() != 0) {
                    card1 = legalCards.get(r.nextInt(legalCards.size()));     
                } else {
                    card1 = hand.get(r.nextInt(hand.size()));     
                }
            }

            Card card2 = null;
            try {
                card2 = playedCards.get(opponentTurn2);
            } catch (IndexOutOfBoundsException e ) {
                // Opponent has not gone yet.
                // Deal a random hand and play a random valid card.
                List<Card> hand = new ArrayList<Card>(0);
                while (hand.size() < handSize) {
                    Card randomCard = remainingCards.get(r.nextInt(nRemaining));
                    while (hand.contains(randomCard)){
                        randomCard = remainingCards.get(r.nextInt(nRemaining));
                    }
                    hand.add(randomCard);
                }  
                List<Card> legalCards = new ArrayList<Card>();
                for (Card card: hand) {
                    legalCards.add(card);
                }
                if (legalCards.size() != 0) {
                    card2 = legalCards.get(r.nextInt(legalCards.size()));     
                } else {
                    card2 = hand.get(r.nextInt(hand.size()));    
                } 
            }

            return cc.compare(playerCard, card1) > 0 &&
                   cc.compare(playerCard, card2) > 0;
        }

        /**
        * Performs the backpropagation phase
        */
        public void backPropagate(MCTNode node, boolean outcome) {
            node.incrementVisited();
            if (outcome) 
                node.incrementWon();
            while (node.hasParent()) {
                node = node.getParent();
                node.incrementVisited();
                if (outcome)
                    node.incrementWon();
            }
        }
    }

    private class CardComparator implements Comparator<Card> {
        public int compare(Card c1, Card c2)
        {
            int mult1 = 1;
            int mult2 = 1;
            if(c1.suit == Suit.SPADES)
                mult1 = 100;
            if(c2.suit == Suit.SPADES)
                mult2 = 100;
            int score1 = mult1 * c1.rank;
            int score2 = mult2 * c2.rank;
            return score1 - score2;
        }
    }

    private class MCTNode {
        private int nVisits;
        private int nWins;
        private MCTNode parent;
        private List<MCTNode> children;
        private GameState state;

        /**
        * @param parent The parent of this node.
        * @param state The gamestate this node represents
        */
        public MCTNode(MCTNode parent, GameState state){
            this.parent = parent;
            this.state = state;
            nVisits = 0;
            nWins = 0;
            children = new ArrayList<MCTNode>();
        }

        /**
        * Create a root node.
        * @param state The gamestate this node represents
        */
        public MCTNode(GameState state) {
            this(null, state);
        }

        /**
        * @return The upper confidence bound of this node
        */
        public double getUpperConfidenceBound() {
            double winScore;
            if (nVisits != 0) {
                winScore = (double)nWins / nVisits;
            } else {
                winScore = 0;
            }

            double c = 1.4;
            double lnt = Math.log(parent.getnVisits());
            double ucb = winScore + c * Math.sqrt(lnt/nVisits);
            return ucb;
        }

        /**
        * @return The number of times this node has been visited
        */
        public int getnVisits() {
            return nVisits;
        }

        /**
        * @return The number of simulated games this node has won
        */
        public int getnWins() {
            return nWins;
        }

        /**
        * @return This node's parent
        */
        public MCTNode getParent() {
            return parent;
        }

        /**
        * @return True if this node has a parent. False if this is the root node.
        */
        public boolean hasParent() {
            return getParent() != null;
        }

        /**
        * @return A list of this node's children
        */
        public List<MCTNode> getChildren() {
            return children;
        }

        /**
        * @return This node's gamestate
        */
        public GameState getState() {
            return state;
        }

        /**
        * Increment this node's win count
        */
        public void incrementWon() {
            nWins++;
        }

        /**
        * Increment this node's visited count
        */
        public void incrementVisited() {
            nVisits++;
        }

        /**
        * Add a child to this node.
        * @param node The node to append
        */
        public void appendChild(MCTNode node) {
            children.add(node);
        }

        /**
        * Add a child to this node.
        * @param state The gamestate to build the child node from
        */
        public void appendChild(GameState state) {
            MCTNode node = new MCTNode(this, state);
            children.add(node);
        }

        /**
        * Get the card played to create this node from its parent.
        * @return The card played to create this node from its parent.
        */
        public Card getPlayedCard() {
            return state.getPlayedCards().get(state.getPlayerTurn());
        }
    }

    private class GameState implements Cloneable {
        private List<Card> playedCards;    // Cards that have been played so far in this trick.
        private List<Card> playersCards;   // Cards available to the player
        private List<Card> remainingCards; // Cards that the player has not yet seen
        private int currentTurn;
        private int playerTurn;
        private int leader;

        /**
        * @param playedCards A list of the cards that have been played in this trick
        * @param playersCards A list of the cards in the agent's hand
        * @param remainingCards A list of cards that the agent has not yet seen
        * @param currentTurn The current turn in this gamestate
        * @param playerTurn The player's turn in the order
        */
        public GameState(List<Card> playedCards, List<Card> playersCards, List<Card> remainingCards, int currentTurn, int playerTurn){
            this.playedCards = playedCards;
            this.playersCards = playersCards;
            this.remainingCards = remainingCards;
            this.currentTurn = currentTurn;
            this.playerTurn = playerTurn;
        }

        /**
        * @param state The gamestate to copy
        */
        public GameState(GameState state){
            // Need to clone all the arrays.
            playedCards = new ArrayList<Card>(state.getPlayedCards());
            playersCards = new ArrayList<Card>(state.getPlayersCards());
            remainingCards = new ArrayList<Card>(state.getRemainingCards());
            this.currentTurn = state.getCurrentTurn();
            this.playerTurn = state.getPlayerTurn();
        }

        /**
        * Play a card and update this gamestate accordingly.
        * @param card The card to play
        */
        public void playCard(Card card) {
            if (isPlayerTurn()) {
                playersCards.remove(card);
            } else {
                remainingCards.remove(card);
            }
            playedCards.add(card);
            incrementTurn();
        }

        /** 
        * Play a card at random and update this gamestate accordingly
        */
        public void playCard() {
            Random random = new Random();
            if (isPlayerTurn()) {
                playCard(playersCards.get(random.nextInt(playersCards.size())));
            } else {
                playCard(remainingCards.get(random.nextInt(remainingCards.size())));
            }
        }

        /**
        * Clone the gamestate. Performs a deep copy for all lists.
        */
        public Object clone(){
            return new GameState(this);
        }

        /**
        * @return True if the player won this trick
        * @throws IllegalStateException if the trick is not over yet
        */
        public boolean playerWonTrick() {
            if (!isTrickFinished()) {
                throw new IllegalStateException("This trick has not finished yet.");
            } else {
                Card playersCard = playedCards.get(playerTurn);
                Collections.sort(playedCards, new CardComparator());
                return playedCards.get(2) == playersCard;
            }
        }

        /**
        * @return True if the trick has ended
        * @throws IllegalStateException if more than 3 cards have been played.
        */        
        public boolean isTrickFinished() {
            int size = playedCards.size();
            if (size > 3) {
                throw new IllegalStateException("More than 3 cards have been played in this trick");
            }
            return size == 3;
        }

        /**
        * @return A list of cards that have been played in this trick
        */
        public List<Card> getPlayedCards() {
            return playedCards;
        }

        /**
        * @return A list of cards in the agent's hand
        */
        public List<Card> getPlayersCards() {
            return playersCards;
        }

        /**
        * @return A list of cards the agent hasn't seen yet
        */
        public List<Card> getRemainingCards() {
            return remainingCards;
        }

        /**
        * @return The current turn
        */
        public int getCurrentTurn() {
            return currentTurn;
        }

        /**
        * @return The agent's turn
        */
        public int getPlayerTurn() {
            return playerTurn;
        }

        /**
        * @param turn The player's turn number. (0-2)
        */
        public void setPlayerTurn(int turn) {
            playerTurn = turn;
        }

        /**
        * Increment the turn number in this gamestate.
        */
        public void incrementTurn() {
            currentTurn = (currentTurn + 1) % 3;
        }

        /**
        * Reset the gamestate
        */
        public void reset() {
            currentTurn = 0;
            playedCards = new ArrayList<Card>();
        }

        /**
        * @return True if it is the player's turn
        */
        public boolean isPlayerTurn(){
            return (currentTurn == playerTurn);
        }
    }
}