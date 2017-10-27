/**
 * A rule-based Greedy Agent that attempts to make the best decisions at each trick
 * (locally optimal decisions)
 * by following the heuristic of attempting to remove the highest scoring card from the
 * biggest non-spade suit in hand if leader, Else playing the lowest possible card to win
 * if that is not possible, discarding the lowest possible legal card.
 * @author Abrar Amin (abrar.a.amin@gmail.com)
 * @author Robbie Fernandez ()
 */

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;

public class GreedyAgentX implements MSWAgent
{
	private String name; //name of the agent.
	private List<Card> seenCards; //stores the current hand of the player.
	private int turn; //stores how many cards have been played out of 3 players...
	private List<Card> currentHand; //stores the cards in player's hand, reset at each round.
	private List<Card> hearts; //stores all the hearts in player's hand.
	private List<Card> diamonds; //stores all the diamonds in player's hand.
	private List<Card> clubs; //stores all the clubs in player's hand.
	private List<Card> spades; //stores all the spades in player's hand.
	private Comparator<Card> cardComparator; //a comparator to compare cards based on strength.


	/**
	* Default Constructor, sets up the greedy agent by initializing all classfields.
	* Name is set to "GR33D by abraram" by default.
	*/
	public GreedyAgentX()
	{
		this.name = "GR33D_EX by abraram";
		setGreedyAgent();
	}

	/**
	* Constructor, sets up the greedy agent by initializing all classfields.
	* Name is set to String argument provided.
	* @param providedName specify the agent's name. Set to defaults if empty string.
	*/
	public GreedyAgentX(String providedName)
	{
		if(providedName.equals(""))
				this.name = "GR33D_EX by abraram";
		else
			this.name = providedName;
		setGreedyAgent();
	}


	/**
	* Initialize all the classfields of Greedy Agent. To be called by Constructor.
	*/
	public void setGreedyAgent()
	{
		this.turn = 0;
		this.seenCards = new LinkedList<Card>();
		this.currentHand = new LinkedList<Card>();
		this.clubs = new LinkedList<Card>();
		this.spades = new LinkedList<Card>();
		this.hearts = new LinkedList<Card>();
		this.diamonds = new LinkedList<Card>();
		initCardComparator();
	}

	/**
	 * Initializes the comparator object for ordering a list of cards
	 * from lowest to highest values. All cards with the same value is considered
	 * equal irrespective of suits, with the exception of Spades,
	 * which are ranked higher than any other card from any other suits.
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


	/**
	 * Compare 2 cards, c1 and c2 return the difference between the comparison
	 * values of c1 and c2.
	 * @param c1 the first Card object.
	 * @param c2 the second Card object.
	 * @return the difference between evaluated score of card 1 (c1) and/ (minus) card 2, c2.
	 */
	public int compare(Card c1, Card c2)
	{
		System.out.println("* Comparing between... "+c1.toString() + " & " + c2.toString());
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
	*/
	public void setup(String agentLeft, String agentRight)
	{
		
	}

    /**
   	* Starts the round with a deal of the cards.
   	* The agent is told the cards they have (16 cards, or 20 if they are the leader)
   	* and the order they are playing (0 for the leader, 1 for the left of the leader,
	* and 2 for the right of the leader).
   	*/
  	public void seeHand(List<Card> hand, int order)
	{
		this.currentHand = hand;
		putCardsToSuitList();
	}


  	/**
	* A method for printing an array of cards, mainly used to
  	* Print discarded cards for debugging.
  	* @param dc discarded cards array of size 4.
  	*/
  	private void printDiscardArray(Card[] dc)
  	{
  		for(int i = 0; i < dc.length; i++)
  		{
  			System.out.println(dc[i].toString());
  		}
	}

  	/**
  	* Cards are allocated to their relevant list of suits. This helps when
  	* agent has to play card from a specific suit.
  	*/
  	private void putCardsToSuitList()
  	{
  		clubs.clear(); diamonds.clear(); hearts.clear(); spades.clear();
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
   	* Works by removing the lowest ranked 4 cards hand according to the Comparator
   	* class Object cardComparator declared above.
   	* @return discardArr an array of cards that are to be discarded.
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
		putCardsToSuitList();
		return discardArr;
		}

	/**
	* Returns the largest suit in Agent's hand. If the 2 suits are of equal size,
	* return the list of suit with the smaller stronger card.
	* @return list of cards belonging to the largest suit in hand.
	*/
 	private List<Card> findLargestSuitInHand()
 	{
 		Collections.sort(this.hearts, cardComparator); Collections.sort(this.diamonds, cardComparator);
 		Collections.sort(this.spades, cardComparator); Collections.sort(this.clubs, cardComparator);
 		List<Card> ls = ((this.spades.size() > this.clubs.size()) ? this.spades : this.clubs);
 		ls = ((ls.size() > this.hearts.size()) ?
 				ls : this.hearts);
 		ls = ((ls.size() > this.diamonds.size()) ?
 				ls : this.diamonds);

		if(ls.size() == clubs.size()){
			if((compare(ls.get(ls.size()-1), clubs.get(ls.size()-1)) > 0) || ls.get(0).suit == Suit.SPADES){ls = this.clubs;}
		}
		if(ls.size() == diamonds.size()){
			if((compare(ls.get(ls.size()-1), diamonds.get(ls.size()-1)) > 0) || ls.get(0).suit == Suit.SPADES) {ls = this.diamonds;}
		}
		if(ls.size() == hearts.size()){
			if((compare(ls.get(ls.size()-1), hearts.get(ls.size()-1)) > 0) || ls.get(0).suit == Suit.SPADES){ls = this.hearts;}
		}

 		return ls;
	}


 	/**
 	 * Check to see if a round has been completed. I.e. all 3 cards have been played.
 	 * And hence the seenCards list will be cleared.
 	 */
	private void checkForRoundCompletion() 
	{
		if(this.turn == 3)
		{
			this.turn = 0;
			seenCards.clear();
		}
	}


	/**
	* A method that can be called to print out the cards on the agent's hand.
	* Mainly used for debugging.
	*/
	private void printHand()
	{
		System.out.println("The hand : ");
		for(int i = 0; i < this.currentHand.size(); i++)
		{
			System.out.print(this.currentHand.get(i).toString() + " ");
		}
		System.out.println("");
	}


	/**
	 * This method keeps track the best possible move to play in case Agent does not have the card
	 * belonging to the leader's suit. Heuristics involve If possible try to play a trump card and win
	 * else play the lowest value card in hand.
	 * @return outCard, the best possible legal card to get rid of in case Agent does not have leader's played suit.
	 */
	public Card noCardOfLeadersSuitFallback()
	{
		Card outCard = null;
		Collections.sort(currentHand, cardComparator);
		if(spades.size() == 0){
			outCard = currentHand.remove(0); //the weakest card we have...
		}else {
			Collections.sort(spades, cardComparator); // Spades clause... try and win. Compare largest spade seen with largest card that has been played..
			if(this.compare( spades.get(spades.size()-1), seenCards.get(seenCards.size()-1)) < 0)
				outCard = currentHand.remove(0); //strongest spade in hand is weaker than a spade that has been played, play weakest card.
			else {
				for(int i = 0; i < spades.size(); i++)
				{
					if(this.compare( spades.get(i), seenCards.get(seenCards.size()-1)) > 0)
					{
						outCard = spades.remove(i); //if P2 played a Spade, we want to beat that too, if possible.
					}
				}
			}
		} 
		return outCard;
	}

	/**
   	* Agent returns the card they wish to play.
   	* A 200 ms timelimit is given for this method.
   	* This method implements the above mentioned heuristic to make the best 
   	* possible choice locally.
   	* @return the Card they wish to play.
  	*/
 	public Card playCard()
	{
 		checkForRoundCompletion(); //check if the round has been completed...
 		List<Card> playingSuitReference = findLargestSuitInHand(); //stores the reference to the list of card we play from.
 		Card pc = null; // play card
 		// If we are the leader, play the strongest card of the non-Ace largest suit.
 		if(this.turn == 0)
 		{
 				pc = playingSuitReference.remove(playingSuitReference.size()-1);
 		} //else we are not the leader and are playing on either position 2 or 3.
 		else 
 		{
 			Suit targetSuit = seenCards.get(0).suit;  //Suit played by the leader, our target...
			Collections.sort(seenCards, cardComparator); //sort the seen card list, so that the last seen card is now the strongest card to be played.
			if(targetSuit == Suit.CLUBS) {playingSuitReference = clubs;}
			else if(targetSuit == Suit.SPADES) {playingSuitReference = spades;}
			else if(targetSuit == Suit.HEARTS) {playingSuitReference = hearts;}
			else {playingSuitReference = diamonds;}

			// Don't have the suit we should be playing.
			if(playingSuitReference.size() == 0){
				pc = noCardOfLeadersSuitFallback();
			} else {
				boolean playCardFound = false;
				Collections.sort(playingSuitReference, cardComparator);
				for(int i = 0; i < playingSuitReference.size(); i++)
				{
					if( this.compare(playingSuitReference.get(i), seenCards.get(seenCards.size()-1)) < 0 )
						continue;
					else
					{
						pc = playingSuitReference.remove(i); //remove smallest required to win...
						System.out.println("* Ha! found one. " + pc.toString());
						playCardFound = true;
						break;
					}
				}
				if(!playCardFound) 
				{
					pc = playingSuitReference.get(0);
					System.out.println("* Meh.. You win. " + pc.toString());
				}
			}
 		}
 		printHand();
 		this.currentHand.remove(pc);
 		putCardsToSuitList();
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
  		if(agent.equals(this.name) == false)
  		{
  			checkForRoundCompletion();
  			seenCards.add(card);
  			this.turn++;
  		}
		}

  	/**
   	* See the result of the trick.
   	* A 50 ms timelimit is given to this method.
   	* This method will be called on each eagent at the end of each trick.
   	* @param winner, the player who played the winning card.
   	* */
	public void seeResult(String winner)
	{
	
	}

  	/**
   	* See the score for each player.
   	* A 50 ms timelimit is givien to this method
  	* @param scoreboard, a Map from agent names to their score.
   	**/
  	public void seeScore(Map<String, Integer> scoreboard)
	{
  		
	}

  	/**
   	* Returns the Agents name.
   	* A 10ms timelimit is given here.
   	* This method will only be called once.
	* @return the name of the agent.
   	*/
  	public String sayName()
	{
		return this.name;
	}
}
