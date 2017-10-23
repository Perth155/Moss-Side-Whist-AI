import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class GreedyNaive implements MSWAgent
{
	private Card seenCards[]; //stores the current hand of the player. 
	private int turn;
	private List<Card> currentHand; //stores the cards in player's hand. 
	private List<Card> hearts;
	private List<Card> diamonds;
	private List<Card> clubs;
	private List<Card> spades;
	private Comparator<Card> cardComparator;

	public GreedyNaive()
	{
		this.turn = 0;
		this.seenCards = new Card[3];
		this.currentHand = new LinkedList<Card>();
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
	
	
	/**
	* Tells the agent the names of the competing agents, and their relative position.
  	* */
 	public void setup(String agentLeft, String agentRight)
	{
		//TODO
	}

       	/**
   	* Starts the round with a deal of the cards.
   	* The agent is told the cards they have (16 cards, or 20 if they are the leader)
   	* and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
   	*/
  	public void seeHand(List<Card> hand, int order)
	{
		this.currentHand = hand;
		putCardsToSuitList();
	}

  	
  	/**
  	 * Print discarded cards.
  	 * @param dc discarded cards array of size 4.
  	 */
  	private void printDiscardArray(Card[] dc) 
  	{
  		System.out.println("-----------------\nDiscarded\n-----------------");
  		for(int i = 0; i < dc.length; i++)
  		{
  			System.out.println(dc[i].toString());
  		}
		
	}
  	
  	//draw out strong spades of opponents by playing weak spades. Look into this...
  	
  	/**
  	 * Cards are allocated to their relevant list of suits.
  	 */
  	private void putCardsToSuitList()
  	{
  		for(int i = 0; i < this.currentHand.size(); i++)
  		{
  			Card c = this.currentHand.get(i);
  			if(c.suit == Suit.CLUBS)
  				clubs.add(c);
  			else if(c.suit == Suit.DIAMONDS)
  				diamonds.add(c);
  			else if(c.suit == Suit.HEARTS)
  				hearts.add(c);
  			else
  				spades.add(c);
  		}
  	}
  	
  	/**
   	* This method will be called on the leader agent, after the deal.
   	* If the agent is not the leader, it is sufficient to return an empty array.
   	*/
  	public Card[] discard()
	{
		Collections.sort(currentHand, cardComparator);
		
		Card discardArr[] = new Card[4];
		for(int i = 0; i < discardArr.length; i++)
		{
			discardArr[i] = currentHand.remove(0);
		}
		printDiscardArray(discardArr);
		return discardArr;
	}
	
	/**
	 * Returns the largest suit in Agent's hand.
	 * @return
	 */
 	private List<Card> findLargestSuitInHand() 
 	{
 		Collections.sort(this.hearts); Collections.sort(this.diamonds);
 		Collections.sort(this.spades); Collections.sort(this.clubs);
 		List<Card> ls = ((this.spades.size() > this.clubs.size()) ? this.spades : this.clubs);
 		ls = ((ls.size() > this.hearts.size()) ? 
 				ls : this.hearts);
 		ls = ((ls.size() > this.diamonds.size()) ? 
 				ls : this.diamonds);	
 		return ls;
	}

 	
 	/**
 	 * Check to see if a round has been completed. I.e. all 3 cards have been played.
 	 */
	private void checkForRoundCompletion() {
		if(this.turn == 2)
			this.turn = 0;
	}
 	
 	
	/**
   	* Agent returns the card they wish to play.
   	* A 200 ms timelimit is given for this method
   	* @return the Card they wish to play.
  	*/
 	public Card playCard()
	{
 		checkForRoundCompletion();
 		List<Card> largestSuitInHandRef = findLargestSuitInHand();
 		Card pc = null; //play card
 		// we are the leader --- if spades is the largest suit in hand, play the weakest spade first?? Or should the strongest to draw out all other winning Spade cards??
 		if(this.turn == 0)
 		{
 			if(largestSuitInHandRef.get(0).suit == Suit.SPADES)
 			{
 				pc = this.spades.remove(0); //weakest spade.
 				this.currentHand.remove(pc);
 			}
 			else
 			{
 				pc = largestSuitInHandRef.remove(largestSuitInHandRef.size()-1);
 				this.currentHand.remove(pc);
 			}
 		}
 		else 
 		{
 			if(this.turn == 1)
 			{
 				
 			}
 		}
 		
 		this.turn++;
 		return pc;
	}



	/**
   	* Sees an Agent play a card.
   	* A 50 ms timelimit is given to this function.
   	* @param card, the Card played.
   	* @param agent, the name of the agent who played the card.
   	*/
  	public void seeCard(Card card, String agent)
	{
		seenCards[turn] = card;
		this.turn++;
	}

  	/**
   	* See the result of the trick. 
   	* A 50 ms timelimit is given to this method.
   	* This method will be called on each eagent at the end of each trick.
   	* @param winner, the player who played the winning card.
   	* */
  
	public void seeResult(String winner)
	{
		//TODO
	}

  	/**
   	* See the score for each player.
   	* A 50 ms timelimit is givien to this method
  	* @param scoreboard, a Map from agent names to their score.
   	**/
  	public void seeScore(Map<String, Integer> scoreboard)
	{
		//TODO
	}

  	/**
   	* Returns the Agents name.
   	* A 10ms timelimit is given here.
   	* This method will only be called once.
   	*/
  	public String sayName()
	{
		return "NaiveGreedy by abraram";
	}
}
