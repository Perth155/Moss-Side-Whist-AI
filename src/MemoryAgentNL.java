
import java.util.*;

public class MemoryAgentNL implements MSWAgent{    
    
    private List<Card> currentHand; //the agents current hand
    private String leftAgent; //variable holding who the agent to the left is
    private String rightAgent; //variable holding who the agent to the right is
    private String leader; //variable holding who the leader is
    private Card leaderCard; //variable holding the card the leader played
    private String name; //variable holding the agents name
    private Card highestCardPlayed; //variable holding the highest card played for the trick
    private Card firstCardPlayed; //first card of the trick played
    private Card secondCardPlayed; //second card of the trick played
    private List<Card> cardsLeft = new ArrayList<Card>(Arrays.asList(Card.values()));; //the cards in the deck yet to be played
    private List<Card> leftAgentsPotentialCards; //the potential remaining cards in leftAgents hand
    private List<Card> rightAgentsPotentialCards; //the potential remaining cards in rightAgents hand
    private int trickNumber;
    private Map suitCount = new HashMap();  
    private Map played = new HashMap();
    private Map won = new HashMap();
    private int count = 0;
    private Card probCard = null;
        
    //SPADES [][0], DIAMONDS [][1], CLUBS [][2], HEARTS [][3], leftAgent [0][], rightAgent [1][]
    private Suit remainingSuits[][];
    /**
    * Tells the agent the names of the competing agents, and their relative position.
    * */
    public void setup(String agentLeft, String agentRight){
        leftAgent = agentLeft; //sets the global variable
        rightAgent = agentRight; //sets the global variable     
    }

    /**
    * Starts the round with a deal of the cards.
    * The agent is told the cards they have (16 cards, or 20 if they are the leader)
    * and the order they are playing (0 for the leader, 1 for the left of the leader, and 2 for the right of the leader).
    * */
    public void seeHand(List<Card> hand, int order){          
        //count++;
        leftAgentsPotentialCards = new ArrayList<Card>(Arrays.asList(Card.values()));
        rightAgentsPotentialCards = new ArrayList<Card>(Arrays.asList(Card.values()));
        for (int i = 0; i < hand.size(); i++) {
            leftAgentsPotentialCards.remove(hand.get(i));
            rightAgentsPotentialCards.remove(hand.get(i));
        }     
        //System.out.println(Arrays.toString(leftAgentsPotentialCards.toArray()));
        
        remainingSuits = new Suit[][] {{Suit.SPADES,Suit.DIAMONDS,Suit.CLUBS,Suit.HEARTS},{Suit.SPADES,Suit.DIAMONDS,Suit.CLUBS,Suit.HEARTS}};
        if (order == 0) { //if this agent is the leader
            leader = name;
        } else if (order == 1) { //if the agent is to the left of the leader
            leader = rightAgent;
        } else if (order == 2) { //if the agent is to the right of the leader
            leader = leftAgent;
        }
        currentHand = hand; //sets the global variable
    }

    /**
    * This method will be called on the leader agent, after the deal.
    * If the agent is not the leader, it is sufficient to return an empty array.
    */
    public Card[] discard(){ //discards the 4 worst cards (cards with the lowest rank that are not spades)
        Card[] discard = new Card[4]; 
        if (name.equals(leader)) { 
            List<Card> sortedHand = new ArrayList<Card>(currentHand);
            int count1 = 0;
            int count2 = 0;
            //removes all spades
            for (int i = 0; i < 20; i++) {
                if (currentHand.get(i).suit != Suit.SPADES) {
                    sortedHand.set(count1, currentHand.get(i)); 
                    count1 ++;
                } else {
                    count2 ++;
                }               
            } 
            //Bubble sort
            boolean swapped = true;
            while (swapped == true) {
                swapped = false;                
                for (int i = 1; i < 20-count2; i++) {
                    if (sortedHand.get(i).rank < sortedHand.get(i-1).rank) {
                        Card tempCard = sortedHand.get(i-1);
                        sortedHand.set(i-1, sortedHand.get(i));
                        sortedHand.set(i, tempCard);                    
                        swapped = true;
                    }
                }
            }             
            for (int i = 0; i < 4; i++) {
                discard[i] = sortedHand.get(i);
                currentHand.remove(discard[i]);
            }
        }        
        return discard;      
    }

    /**
    * Agent returns the card they wish to play.
    * A 200 ms timelimit is given for this method
    * @return the Card they wish to play.
    * */
    public Card playCard(){
        Card cardToPlay; //card to be played
        trickNumber = (trickNumber + 1)%16;
        Suit currentSuit1 = null; //refers to the current suit in your hand
        Suit currentSuit2 = null; //refers to the current suit in the remaining cards
        
        cardToPlay = currentHand.get(currentHand.size()-1); //Change this to play the lowest card if you do not have a card of the same suit.?
        
        //this always plays the highest card that is not a spade
        if (leader.equals(name) == true) { //if your turn is the first of the trick (you are the leader)    
            for (int i = 0; i < currentHand.size(); i++) { //loops through all cards in your current hand
                currentSuit2 = null; //resets currentSuit2 to null
                if (currentHand.get(i).suit != currentSuit1) { //if it is your highest ranked card of the suit
                    currentSuit1 = currentHand.get(i).suit; //set currentSuit1 to the current suit                     
                    for (int j = 0; j < cardsLeft.size(); j++) { //loops through all the cards left in the game
                        if (cardsLeft.get(j).suit != currentSuit2) { //if the current card is the highest ranked card of the suit
                            currentSuit2 = cardsLeft.get(j).suit; //set currentSuit2 to the current suit
                            if ((cardsLeft.get(j).suit == currentHand.get(i).suit) && (cardsLeft.get(j).rank == currentHand.get(i).rank)) { //if your card is the highest ranked card of that suit left in the game
                                cardToPlay = currentHand.get(i); //set it to be the card to be played
                                currentHand.remove(i); //remove the card to be played from the hand
                                return cardToPlay; //return the card
                            }
                        }
                    }
                }
            }
            //You do not have the highest ranked card of any suit
            Map suitCount = new HashMap();
            suitCount.put(Suit.SPADES, 0);
            suitCount.put(Suit.DIAMONDS, 0);
            suitCount.put(Suit.CLUBS, 0);
            suitCount.put(Suit.HEARTS, 0);            
            for (int i = 0; i < currentHand.size(); i++) { //loop through each card in your current hand  
                suitCount.put(currentHand.get(i).suit, (int) (suitCount.get(currentHand.get(i).suit))+1); //populate the map holding how many cards of each suit are left                 
            }
            Suit commonSuit = Suit.SPADES; 
            for(Suit suit : Suit.values()) {
               if ((int) suitCount.get(suit) > (int) suitCount.get(commonSuit)) {
                   commonSuit = suit;
               }
            }     
            //System.out.println(suitCount.get(Suit.SPADES) + " " + suitCount.get(Suit.DIAMONDS) + " " + suitCount.get(Suit.CLUBS) + " " + suitCount.get(Suit.HEARTS));
            for (int i = 0; i < currentHand.size(); i++) { //loop through each card in your current hand  
                if (currentHand.get(i).suit == commonSuit && i != currentHand.size()-1) {
                    while (currentHand.get(i+1).suit == currentHand.get(i).suit) {
                        i++; //increase the index by 1
                        if (i == currentHand.size()-1) { //if the last card of the hand
                            break; //break the while loop
                        }
                    }
                    cardToPlay = currentHand.get(i);
                    currentHand.remove(cardToPlay);
                    return cardToPlay;
                }             
            }
            
            cardToPlay = currentHand.get(currentHand.size()-1); //set the card to play as the last card
            for (int i = 0; i < currentHand.size(); i++) { //loop through each card in your current hand
                if ((currentHand.get(i).suit != Suit.SPADES) && currentHand.get(i).rank > cardToPlay.rank) { //if the card is not a spade and the rank is greater than the current card being play
                    cardToPlay = currentHand.get(i); //update the card to be played
                }
            } 
            if (cardToPlay.suit == Suit.SPADES) { //is the card to be played is a spade (this means you only have spades left)
                cardToPlay = currentHand.get(0); //set the card to play as the highest spade
            }            
            currentHand.remove(cardToPlay); //remove the card to be played from the hand
            return cardToPlay; 
        } 
        //this always plays the lowest card that gaurantees winning the trick
        else if (leader.equals(leftAgent) == true) { //if your turn is the last of the trick
            for (int i = 0; i < currentHand.size(); i++) { //loop through each card in your current hand  
                if (currentHand.get(i).suit == leaderCard.suit) { //if the suit is the same as the leaders cards suit
                    cardToPlay = currentHand.get(i); //sets the card to be played as the first card of the same suit                    
                    if ((i != currentHand.size()-1) && currentHand.get(i).rank > highestCardPlayed.rank) { //if it's not the last card and the rank is the higher than the highest card this trick
                        while ((currentHand.get(i+1).suit == leaderCard.suit) && (currentHand.get(i+1).rank > highestCardPlayed.rank)) { //while the next card in the hand is the same suit and a higher rank
                            i++; //increase the index by 1
                            if (i == currentHand.size()-1) { //if the last card of the hand
                                break; //break the while loop
                            }
                        } 
                    } else if ((i != currentHand.size()-1) && currentHand.get(i).rank < highestCardPlayed.rank) { //if it's not the last card and the rank is lower than the highest card this trick
                        while ((currentHand.get(i+1).suit == leaderCard.suit) && (currentHand.get(i+1).rank < highestCardPlayed.rank)) { //while the next card in the hand is the same suit and a lower rank
                            i++; //increase the index by 1
                            if (i == currentHand.size()-1) { //if the last card of the hand
                                break; //break the while loop
                            }
                        } 
                    }
                    cardToPlay = currentHand.get(i); //update the card to be played 
                    currentHand.remove(i); //remove the card to be played from the hand
                    return cardToPlay;
                }                
            }
        } 
        //this always plays the highest card of the same suit to maximise your chance of winning the trick
        else if (leader.equals(rightAgent) == true) { //if your turn is the second turn of the trick
            
            //CHANGE THIS TO USE THE SUIT REMAINING FEATURE IF POSSIBLE
            
            for (int i = 0; i < currentHand.size(); i++) { //loop through each card in your current hand
                if (currentHand.get(i).suit == leaderCard.suit) { //if the suit is the same as the leaders cards suit
                    if ((currentHand.get(i).rank < leaderCard.rank) && (i != currentHand.size()-1)) { //if no cards with the same suit have a higher rank and it isn't the last card of the hand
                        while (currentHand.get(i+1).suit == leaderCard.suit) { //while the suit of the next card is the same                                                         
                            i++; //increase the index by one
                            if (i == currentHand.size()-1) { //if the last card of the hand
                                break; //break the while loop
                            }
                        }
                    }
                    cardToPlay = currentHand.get(i); //set the card to be played
                    currentHand.remove(i); //remove the card to be played from the hand
                    return cardToPlay;
                }                
            }
            //if it makes it here then you have no cards of the same suit            
            if (currentHand.get(0).suit == Suit.SPADES) {
                int i = -1;
                while (currentHand.get(i+1).suit == Suit.SPADES) {     
                    i++; //increase the index by one
                    if (i == currentHand.size()-1) { //if the last card of the hand
                        break; //break the while loop
                    }                    
                }                
                cardToPlay = currentHand.get(i); //set the card to be played
                currentHand.remove(i); //remove the card to be played from the hand
                return cardToPlay;
            }      
            Card opponentsHighestSpade = null;
            for (int i = 0; i < cardsLeft.size(); i++) { //loop through all cards left in the game
                if (cardsLeft.get(i).suit == Suit.SPADES && currentHand.contains(cardsLeft.get(i)) == false) { //if the card is a spade and not in your hand (opponents spade)
                    opponentsHighestSpade = cardsLeft.get(i); //sets the variable opponentsHighestSpade to this card
                    break; //breaks the for loop
                }
            }
            if (currentHand.get(0).suit != Suit.SPADES) { //if you have no spades left
                for (int i = 0; i < currentHand.size(); i++) { //for each card in your current hand
                    if (currentHand.get(i).rank < cardToPlay.rank) { //if the rank of the card is lower than the card to play
                        cardToPlay = currentHand.get(i); //set the card to play                        
                    }
                }
                currentHand.remove(cardToPlay); //remove the card to play
                return cardToPlay; //return the card to play
            }  
            if (opponentsHighestSpade != null) { //if the opponents have a spade
                for (int i = 0; i < currentHand.size(); i++) { //loop through all cards in the current hand
                    if (currentHand.get(i).rank > opponentsHighestSpade.rank && currentHand.get(i).suit == Suit.SPADES) { //if the card is a spade and its rank is higher than the highest opponents spade
                        cardToPlay = currentHand.get(i); //set the card to play          
                    }                    
                }
                currentHand.remove(cardToPlay); //remove the card to play
                return cardToPlay; //return the card to play
            }
        }        
        cardToPlay = currentHand.get(currentHand.size()-1); //Change this to play the lowest card if you do not have a card of the same suit.?
        currentHand.remove(currentHand.size()-1);                       
        return cardToPlay;
    }

    /**
    * Sees an Agent play a card.
    * A 50 ms timelimit is given to this function.
    * @param card, the Card played.
    * @param agent, the name of the agent who played the card.
    * */
    public void seeCard(Card card, String agent){
        // //probability table code
        // if (agent.equals(name)) {
            // probCard = card;
            // played.put(card, (int) played.get(card)+1);
        // }
        
        if (agent.equals(leader)) { //if the agent that just played a card is the leader
            firstCardPlayed = card; //probably the same as leaderCard?
            highestCardPlayed = card;
            leaderCard = card; //remember the card as the card the leader played
        } else if (leftAgent.equals(leader) && agent.equals(rightAgent)) { //if it is the second card played and I haven't played yet
            secondCardPlayed = card;                     
            //updating the highest card played
            if ((card.suit == firstCardPlayed.suit && card.rank > highestCardPlayed.rank) || (card.suit == Suit.SPADES && firstCardPlayed.suit != Suit.SPADES)) { //updates the highest card played
                highestCardPlayed = card;
            }
        }        
        //updating the remaining suits in each players hand
        if (card.suit != leaderCard.suit) {            
            if (agent.equals(leftAgent)) {                
                for (int i = 0; i < 4;  i++) { 
                    if (leaderCard.suit == (remainingSuits[0][i])) {
                        remainingSuits[0][i] = null;
                    }
                }
            } else if (agent.equals(rightAgent)) {                
                for (int i = 0; i < 4;  i++) {
                    if (leaderCard.suit == (remainingSuits[1][i])) {
                        remainingSuits[1][i] = null;
                    }
                }
            }                
        }
        //removes cards played from potential cards
        leftAgentsPotentialCards.remove(card);
        rightAgentsPotentialCards.remove(card);
        cardsLeft.remove(card);
    }

    /**
    * See the result of the trick. 
    * A 50 ms timelimit is given to this method.
    * This method will be called on each eagent at the end of each trick.
    * @param winner, the player who played the winning card.
    * */
    public void seeResult(String winner){ 
        // //probability table code
        // if (winner.equals(name)) {
            // won.put(probCard, (int) won.get(probCard)+1);
        // }
        
        if (trickNumber == 0) {
            cardsLeft = new ArrayList<Card>(Arrays.asList(Card.values()));
        }        
        leader = winner; //set the leader to the winner of the previous trick
    }

    /**
    * See the score for each player.
    * A 50 ms timelimit is given to this method
    * @param scoreboard, a Map from agent names to their score.
    **/
    public void seeScore(Map<String, Integer> scoreboard){ //Called after each game
        // //prob code
        // for (Object card : played.keySet()) {            
            // System.out.println(card + " " + played.get(card) + " " + won.get(card) + " " + ((double) (int) won.get(card)/((double) (int) played.get(card)))*100 + "%");
        // }    
    }
    
    /**
    * Returns the Agents name.
    * A 10ms timelimit is given here.
    * This method will only be called once.
    * */
    public String sayName(){    
        name = "MemoryAgentNL"; //name of the agent
        return name;
    }

}
