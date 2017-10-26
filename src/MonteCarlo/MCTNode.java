import java.util.ArrayList;
import java.util.List;
import java.lang.RuntimeException;

public class MCTNode {
    private int nVisits;
    private int nWins;
    private MCTNode parent;
    private List<MCTNode> children;
    private GameState state;

    public MCTNode(MCTNode parent, GameState state){
        this.parent = parent;
        this.state = state;
        nVisits = 1;
        nWins = 0;
        children = new ArrayList<MCTNode>();
    }

    public MCTNode(GameState state) {
        this(null, state);
    }

    public double getUpperConfidenceBound() {
        // System.out.println("---------------------------------------------");
        // System.out.println("nWins: " + nWins);
        // System.out.println("nVisits: " + nVisits);
        // System.out.println("---------------------------------------------");        
        double winScore = (double)nWins / (double)nVisits;
        double c = 1.41; // ~ sqrt(2)
        double lnt = Math.log(parent.getnVisits());
        double ucb = winScore + c * Math.sqrt(lnt/nVisits);
        return ucb;
    }

    public int getnVisits() {
        return nVisits;
    }

    public int getnWins() {
        return nWins;
    }

    public MCTNode getParent() {
        return parent;
    }

    public boolean hasParent() {
        return getParent() != null;
    }

    public List<MCTNode> getChildren() {
        return children;
    }

    public GameState getState() {
        return state;
    }

    public void incrementWon() {
        nWins++;
    }

    public void incrementVisited() {
        nVisits++;
    }

    public void appendChild(MCTNode node) {
        children.add(node);
    }

    public void appendChild(GameState state) {
        MCTNode node = new MCTNode(this, state);
        children.add(node);
    }

    public void print() {
        System.out.println("nWins: " + nWins);
        System.out.println("nVisits: " + nVisits);
        System.out.println("");
        for (MCTNode child: children) 
            child.print();
    }

    public Card getPlayedCard() {
        for (Card card: state.getPlayedCards()) {
            if (!parent.getState().getPlayedCards().contains(card))
                return card;
        }
        System.out.println("Could not determine played card.");
        System.out.println("Played cards in this node: ");
        for (Card card: state.getPlayedCards()) {
            System.out.print(card + ", ");
        }     
        System.out.println();
        System.out.println("Played cards in parent node: ");
        for (Card card: parent.getState().getPlayedCards()) {
            System.out.print(card + ", ");
        }     
        System.out.println();       
        throw new RuntimeException("This shouldn't happen"); 
    }
}
