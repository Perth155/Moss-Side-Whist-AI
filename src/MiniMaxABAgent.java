import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;

public class MiniMaxABAgent implements MSWAgent 
{
	private String name;
	private List<Card> currentHand; //stores the cards in player's hand. 
	private List<Card> hearts;
	private List<Card> diamonds;
	private List<Card> clubs;
	private List<Card> spades;
	private Comparator<Card> cardComparator;

	public MiniMaxABAgent()
	{
		this.name = "MiniMax by abraram";
		this.currentHand = new LinkedList<Card>();
		this.clubs = new LinkedList<Card>();
		this.spades = new LinkedList<Card>();
		this.hearts = new LinkedList<Card>();
		this.diamonds = new LinkedList<Card>();
		initCardComparator();
	} 

	
	/**
	 * Initializes the comparator object for ranking a list of cards
	 * from lowest to highest values. 
	 */
	private void initCardComparator()
	{
		this.cardComparator = new Comparator<Card>(){
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
		};
	}
	

	@Override
	public void setup(String agentLeft, String agentRight) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seeHand(List<Card> hand, int order) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Card[] discard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Card playCard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void seeCard(Card card, String agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seeResult(String winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seeScore(Map<String, Integer> scoreboard) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String sayName() {
		// TODO Auto-generated method stub
		return null;
	}

}
