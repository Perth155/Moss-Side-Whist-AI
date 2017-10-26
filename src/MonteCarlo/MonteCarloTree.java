import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.RuntimeException;

/**
 * Created by Robbie on 21/10/2017.
 */
public class MonteCarloTree {
    private MCTNode rootNode;

    public MonteCarloTree(MCTNode node) {
        rootNode = node;
    }

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

    // Call this to get the best card after all phases have been completed.
    // TODO - If going third, use minimal winning card.
    public Card getBestCard() {
        if (rootNode.getChildren().size() == 0) {
            throw new IllegalStateException("Required phases not yet completed");
        }

        if (!rootNode.getState().isPlayerTurn()) {
            rootNode.getState().print();
            throw new IllegalStateException("This node does not represent the Player's turn");
        }

        System.out.println("Potential plays: ");
        MCTNode bestNode = null;
        double bestNodeScore = -1;
        for (MCTNode child: rootNode.getChildren()){
            double score = (double)child.getnWins() / (double)child.getnVisits();
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

        if (bestNodeScore == 1.0) {
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAA");
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
    // TODO - Clean up
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

    // From MossSideWhist class
    private boolean followsSuit(Card c1, Card c2){
        return c2.suit==c1.suit;
    }


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

    public void print() {
        rootNode.print();
    }

    /**
     * Perform the simulation phase.
     * Randomly plays cards until the trick ends.
     * @param MCTNode The MCTNode to start the simulation from.
     * @return True if player won, False otherwise.
     */
    public boolean simulateGame(MCTNode node) {
        // Find probability of this card winning.
        // Wins if a random float is less than that probability.
        GameState state = node.getState();
        Card playerCard = state.getPlayedCards().get(state.getPlayerTurn());
        Random r = new Random();
        CardComparator cc = new CardComparator();
        int nWins;
        double winRatio;
        switch (state.getPlayerTurn()) {
            case 0:
                nWins = 0;
                for (Card card: state.getRemainingCards()) {
                    if (cc.compare(playerCard, card) > 0) {
                        nWins++;
                    }
                }
                winRatio = (double)nWins / (double)state.getRemainingCards().size();
                return (r.nextDouble() <= winRatio && r.nextDouble() <= winRatio);
            case 1:
                Card previousCard = state.getPlayedCards().get(0);
                if (cc.compare(playerCard, previousCard) < 0) 
                    return false;
                nWins = 0;
                for (Card card: state.getRemainingCards()) {
                    if (cc.compare(playerCard, card) > 0) {
                        nWins++;
                    }
                }
                winRatio = (double)nWins / (double)state.getRemainingCards().size();
                return (r.nextDouble() <= winRatio);
            case 2:
                Card card1 = state.getPlayedCards().get(0);
                Card card2 = state.getPlayedCards().get(1);
                return cc.compare(playerCard, card1) > 0 &&
                       cc.compare(playerCard, card2) > 0;
            default:
                throw new RuntimeException("Players turn number exceeded 2");
        }
    }

    public void backPropogate(MCTNode node, boolean outcome) {
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
