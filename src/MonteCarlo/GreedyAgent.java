import java.util.*;

/**
    GreedyLow is a rational decison making agent for the game Moss Side Whist.

    Developed by:
        Peter Joseph (21284021)
        Srdjan Kusmuk (21705391)
*/

public class GreedyAgent implements MSWAgent{

    private List<Card> hand; // A list of cards our agent has been dealt
    private String[] position; // A string array with the names of each player in the order they are playing
    private int rootOrder; // The position our agent is playing

    // Store the agent names so we can determine their positions
    private String agentName = "GreedyAgent Peasant";
    private String agentLeftName;
    private String agentRightName;

    // Our storage class for player cards
    cardStorage storage;

    // Enums for calculating card ranks
    enum Rank { BIGGER, SAME, SMALLER }

    /**
     * Agent Setup:
     * This method is called when the agent class is created for the first time.
     * It stores the names of the other players in a private variable for future use.
     * It also initializes a new cardStorage class for documenting player moves across the game.
     * @param agentLeft The name of the player left to our agent.
     * @param agentRight The name of the player right of our agent.
     */
    public void setup(String agentLeft, String agentRight){
        this.agentLeftName  = agentLeft;
        this.agentRightName = agentRight;

        // Setup our CardStorage Class
        storage = new cardStorage(agentName, agentLeft, agentRight);
    }

    /**
     * Deal Hand:
     * A round of the game is started by dealing the cards to each of the agents.
     * Our agent is told the cards it has through the seeHand function (16 cards, or 20 for the leader).
     * The order integer specifies where we are playing (0: leader, 1: left of the leader, 2: right of the leader).
     * We use the order integer and the names of the other two players to calculate the positions of all players.
     * Player positions are stored as their names in a String[] array called position.
     * @param hand The list containing the dealt cards for the user.
     * @param order An integer with our position relative to the other players.
     * */
    public void seeHand(List<Card> hand, int order){
        this.hand  = hand;
        this.rootOrder = order;

        // Method to calculate and store position of all players at the start of the game
        position = rootPosition();
    }

    /**
     * Card Discard:
     * If the round leader has been decided, this method is called afterward.
     * The leader is given 20 cards and is required to discard 4 cards.
     * If the player is not the leader, an empty array is returned.
     * We rank the cards from lowest to highest and discard the worst 4 cards from the set.
     * @return  Card[] Array of cards to be discarded.
     */
    public Card[] discard(){
        // Return 4 card array if we are the leader
        if (rootOrder == 0) {
            // Rank our cards
            Card[] cards = rankCards(hand, null);
            // Create Array for Discarded Cards
            Card[] discard = new Card[4];
            for(int i = 0; i < 4; i++) {
                // Store worst 4 cards in the discard array
                discard[i] = hand.remove(hand.indexOf(cards[i]));
            }
            return discard;
        } else {
            // Return empty array if we are not the leader
            Card[] discard = new Card[0];
            return discard;
        }
    }

    /**
     * Play Card:
     * The agent uses heuristics to decide the best card to play for the trick.
     * There are 3 potential heuristic methods to call depending on the position of the play.
     * @return Card Return the best card to play.
     */
    public Card playCard(){
        // Decision Tree if leader for the trick
        if (position[0] == agentName) {
            return firstPositionPlay();
            // Decision tree if second player
        } else if (position[1] == agentName) {
            return secondPositionPlay();
            // Decision tree if last player
        } else {
            return thirdPositionPlay();
        }
    }

    /**
     * Observe Card:
     * When a card is played, this method is called so all agents can see what was played.
     * We want to keep track of the cards that have been played so we call the storeCard method.
     * @param card The card that has been played.
     * @param agent The name of the agent that played the card.
     * */
    public void seeCard(Card card, String agent){
        storage.storeCard(agent, card); // Store the card in our storage class
    }

    /**
     * See Result:
     * When a trick has been completed, the agent should know who won the trick.
     * This method is called after a trick is complete.
     * We take the winning player position information to update the starting position of players for the next round.
     * @param winner Name of the winning agent for the trick.
     * */
    public void seeResult(String winner){
        position = generateNextRoundPosition(winner, position); // Update the position of all players based on the winning result
    }

    /**
     * See Score:
     * This method is called containing the scores of each player after a trick has been completed.
     * As a rational agent, this information is not useful to us.
     * @param scoreboard, a Map from agent names to their score.
     **/
    public void seeScore(Map<String, Integer> scoreboard){}

    /**
     * Agent Name:
     * This method returns the name of our agent so the game system and other agents know what to call us.
     * This method is only called once for the game.
     * @return agentName The name of our agent.
     * */
    public String sayName(){
        return agentName;
    }

    /**
     * Compare Cards:
     * This method compares two cards and determines if the first card is higher, lower or of the same rank.
     * The value of a card can change depending on the leading suit played for the set.
     * If the cards are the same suit and the same rank we return an enum of Rank.Same
     * If the first card has a higher rank than the second card we return Rank.Bigger
     * If the first card has a smaller rank than the second card return Rank.Smaller
     * @param uno The first played card
     * @param due The comparison card
     * @param suit The leading suit to compare both cards against
     * @return Rank An Enum with the rank of the first card (uno) relative to the second card (due)
     */
    public Rank compareCard(Card uno, Card due, Suit suit) {
        // If uno has spade and due does not, uno is larger
        if (uno.suit == Suit.SPADES && due.suit != Suit.SPADES) {
            return Rank.BIGGER;
        }
        // If uno does not have a spade and due does, uno is smaller
        if (uno.suit != Suit.SPADES && due.suit == Suit.SPADES) {
            return Rank.SMALLER;
        }
        // If both suits are spades, compare the sizing
        if (uno.suit == Suit.SPADES && due.suit == Suit.SPADES) {
            // if both suits are spades and uno larger ranking than due, uno is larger
            if (uno.rank > due.rank) {
                return Rank.BIGGER;
            } else if (uno.rank == due.rank) {
                // If both suits spades and uno has same ranking as due, return same rank
                return Rank.SAME;
                // if both suits are spades and due larger, uno is smaller
            } else {
                return Rank.SMALLER;
            }
        }
        // If no spades in both cards we check the ranking
        if (uno.suit != Suit.SPADES && due.suit != Suit.SPADES) {
            if (suit != null) {
                // If uno is the suit and the other is not, uno is larger
                if (uno.suit == suit && due.suit != suit) {
                    return Rank.BIGGER;
                }
                // if due is the suit and uno is not, uno is smaller
                if (uno.suit != suit && due.suit == suit) {
                    return Rank.SMALLER;
                }
                // if uno and due are both the suits, compare the ranking
                if (uno.suit == suit && due.suit == suit) {
                    if (uno.rank > due.rank) {
                        // if uno larger ranking than due, uno is larger
                        return Rank.BIGGER;
                    } else if (uno.rank == due.rank) {
                        // If uno has same ranking as due, return same rank
                        return Rank.SAME;
                        // if due larger, uno is smaller
                    } else {
                        return Rank.SMALLER;
                    }
                }
            }
            // If no cards are suits simply check highest value
            if (uno.rank > due.rank) {
                // if both suits are not spades and uno larger ranking than due, uno is larger
                return Rank.BIGGER;
            } else if (uno.rank == due.rank) {
                // If both suits are not spades and uno has same ranking as due, return same rank
                return Rank.SAME;
                // if both suits are not spades and due larger, uno is smaller
            } else {
                return Rank.SMALLER;
            }
        }
        return Rank.SAME;
    }

    /**
     * Rank Cards:
     * Rank a set of cards from lowest to highest value.
     * You can specify a leading suit which will adjust the rank value of the cards.
     * Spades always have the highest ranking.
     * If we don't know the currently played card, other card suit types are irrelevant.
     * If we do know the currently played card types, we rank the cards as follows
     * (Other Card Suit, Leading Suit, Spades ).
     * If the card type is also spades, we adjust our rank accordingly.
     * @param hand A list of the cards our agent has in their hand
     * @param suit The second parameter specifies whether we know the currently played card type.
     * @return A Card[] array of the cards in ranked order from lowest to highest.
     */
    public Card[] rankCards(List<Card> hand, Suit suit){
        // Store List cards in an array
        Card[] cards = new Card[hand.size()];
        for (int i = 0; i < hand.size(); i++) {
            cards[i] = hand.get(i);
        }
        // Insertion Sort Algorithm to rank each value
        for (int i = 0; i < hand.size(); i++) {
            Card key = cards[i];
            int j = i - 1;
            // If the currently played card type is "unknown" rank the cards without any special provisions
            if (suit == null) {
                while (j >= 0 && (compareCard(cards[j], key, null) == Rank.BIGGER)) {
                    cards[j + 1] = cards[j];
                    j = j - 1;
                }
            } else {
                // Rank cards by currently played suit
                while (j >= 0 && (compareCard(cards[j], key, suit) == Rank.BIGGER)) {
                    cards[j + 1] = cards[j];
                    j = j - 1;
                }
            }
            cards[j + 1] = key;
        }
        return cards;
    }

    /**
     * Play Trick:
     * This method plays a single trick of the game and determines who the winner is.
     * The leading suit is determined from the leader who plays the first card.
     * @param leader The card the leader is playing, will be used to determine leading suit.
     * @param player2 The card the player positioned second will play.
     * @param Player3 The card the last positioned played will play.
     * @return The winning card
     */
    public Card playTrick(Card leader, Card player2, Card player3) {
        // Define the suit for the trick
        Suit suit = leader.suit;
        // If Leader is Higher Rank than Player 2 card
        if (compareCard(leader, player2, suit) == Rank.BIGGER) {
            // Compare Leader and Player 3
            if (compareCard(leader, player3, suit) == Rank.BIGGER) {
                return leader;
            } else {
                return player3;
            }
            // If Leader is Worse Rank than Player 2
        } else {
            // Compare Player 2 and Player 3 card
            if (compareCard(player2, player3, suit) == Rank.BIGGER) {
                return player2;
            } else {
                return player3;
            }
        }
    }

    /**
     * Get Card Set:
     * This method returns a set of cards that the agent has in its hand for a particular card suit.
     * If no card of the suit can be found in the hand, an empty list is returned.
     * @param suit The card suit we are searching for in the list
     * @param hand The list of cards to search through.
     * @return A list of cards for the particular suit.
     */
    public List<Card> cardSet(Suit suit, List<Card> hand) {
        List<Card> availableCards = new ArrayList<Card>();
        int i = 0;
        while (i < hand.size()) { //Iterate over the list and add any cards if found
            if ((hand.get(i).suit == suit)) {
                availableCards.add(hand.get(i));
            }
            i++;
        }
        return availableCards;
    }

    /**
     * Lowest Card Win:
     * This method calculates the lowest card from the card list required to beat the played cards.
     * If card2 param is null, the method will assume our agent is in second position in the trick.
     * @param card1 The card of the trick leader.
     * @param card2 The card of the second player in the trick.
     * @param hand A list of cards to search through for a winning card.
     * @return Card The winning card to beat the first two players. Returns null if no such card exists.
     */
    public Card lowestCardWin(Card card1, Card card2, List<Card> hand) {
        if (hand != null) {
            Card[] cards = rankCards(hand, null);
            if (card2 == null) {
                for (int i = 0; i < cards.length; i++) {
                    if (compareCard(cards[i], card1, card1.suit) == Rank.BIGGER) {
                        return cards[i];
                    }
                }
            } else {
                for (int i = 0; i < cards.length; i++) {
                    if (playTrick(card1, card2, cards[i]) == cards[i]) {
                        return cards[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Highest Card Win:
     * This method calculates the highest card from the card list required to beat the played cards.
     * If card2 param is null, the method will assume our agent is in second position in the trick.
     * @param card1 The card of the trick leader.
     * @param card2 The card of the second player in the trick.
     * @param hand A list of cards to search through for a winning card.
     * @return Card The winning card to beat the first two players. Returns null if no such card exists.
     */
    public Card highestCardWin(Card card1, Card card2, List<Card> hand) {
        if (hand != null) {
            Card[] cards = rankCards(hand, null);
            if (card2 == null) {
                for (int i = cards.length; i > 0; i--) {
                    if (compareCard(cards[i], card1, card1.suit) == Rank.BIGGER) {
                        return cards[i];
                    }
                }
            } else {
                for (int i = cards.length; i > 0; i--) {
                    if (playTrick(card1, card2, cards[i]) == cards[i]) {
                        return cards[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Card Index:
     * Takes a card and searches through our hand for the index position of the card in our agents hand.
     * @param card The card to find the index.
     * @return int The array index of the card in our hand.
     */
    public int cardIndex(Card card) {
        return hand.indexOf(card);
    }

    /**
     * Generate Root Position:
     * When a new round is started, this method takes our position and determines the position of all agents.
     * Array [0] is the Leader [1] is Player 2 [3] is Player 3.
     * @return String[] The names of each agent in order of play.
     */
    public String[] rootPosition() {
        String[] localMap = new String[3];
        if (rootOrder == 0) {
            localMap[0] = sayName();
            localMap[1] = agentLeftName;
            localMap[2] = agentRightName;
        } else if (rootOrder == 1) {
            localMap[0] = agentRightName;
            localMap[1] = sayName();
            localMap[2] = agentLeftName;
        } else {
            localMap[0] = agentLeftName;
            localMap[1] = agentRightName;
            localMap[2] = sayName();
        };
        return localMap;
    }

    /**
     * Next Trick Position:
     * Decide where players should be positioned after the win of a trick.
     * The decision is based on a current position map and a winner.
     * @param winner The winner of the last trick.
     * @param positionMap A String[] of the player names and positions from the previous game round.
     * @return A String[] with the new positions for next round ([0] Leader [1] Player 2 [3] Player 3)
     */
    public String[] generateNextRoundPosition(String winner, String[] positionMap) {
        String[] localMap = new String[3];
        // Find the index position of the winner
        int index = 0;
        for (int i = 0; i < positionMap.length; i++) {
            if (positionMap[i] == winner) {
                index = i;
            }
        }
        // Store the winner in the first position
        localMap[0] = winner;
        // Determine second players based on winner previous position
        if (index == 0) {
            localMap[1] = positionMap[1];
            localMap[2] = positionMap[2];
        } else if (index == 1) {
            localMap[1] = positionMap[2];
            localMap[2] = positionMap[0];
        } else {
            localMap[1] = positionMap[0];
            localMap[2] = positionMap[1];
        }
        return localMap;
    }

    /**
     * First Position Decide Card:
     * Uses a basic heuristic to determine the card that should be played if we are a leader.
     * Select the highest card in our hand if we are a leader.
     * @return The final card
     */
    public Card firstPositionPlay() {
        // Rank all our cards
        Card[] ranked = rankCards(hand, null);
        // Remove lowest ranked card
        return hand.remove(cardIndex(ranked[ranked.length - 1]));
    }

    /**
     * Second Position Decide Card:
     * Uses a basic heuristic to determine the card that should be played if the leader has already played.
     * Select the lowest possible card we have in our hand that will win against the leaders card.
     * @return The final card
     */
    public Card secondPositionPlay() {
        // Get Player 1's played card
        Card p1Card = storage.retrieveCard(position[0]);
        // Check if Card is a Suit
        if (p1Card.suit == Suit.SPADES) {
            // Do we have a Spade?
            List<Card> set = cardSet(Suit.SPADES, hand);
            if (!set.isEmpty()) {
                Card selected = lowestCardWin(p1Card, null, set);
                if (selected != null) {
                    return hand.remove(cardIndex(selected));
                } else {
                    Card[] rankedSet = rankCards(set, null);
                    return hand.remove(cardIndex(rankedSet[0]));
                }
            } else {
                // We don't have a Spade, so might as well throw the worst card we have
                Card[] ranked = rankCards(hand, null);
                return hand.remove(cardIndex(ranked[0]));
            }
        } else {
            List<Card> set = cardSet(p1Card.suit, hand);
            // Do we have the cards in a set
            if (!set.isEmpty()) {
                Card selected = lowestCardWin(p1Card, null, set);
                if (selected != null) {
                    return hand.remove(cardIndex(selected));
                } else {
                    Card[] rankedSet = rankCards(set, null);
                    return hand.remove(cardIndex(rankedSet[0]));
                }
            } else {
                // Do we have a Spade we could play?
                List<Card> setDue = cardSet(Suit.SPADES, hand);
                if (!setDue.isEmpty()) {
                    Card selected = lowestCardWin(p1Card, null, setDue);
                    if (selected != null) {
                        return hand.remove(cardIndex(selected));
                    } else {
                        Card[] ranked = rankCards(hand, null);
                        return hand.remove(cardIndex(ranked[0]));
                    }
                } else {
                    // We don't have a Spade either, at this point we might as well throw the worst card we have
                    Card[] ranked = rankCards(hand, null);
                    return hand.remove(cardIndex(ranked[0]));
                }
            }
        }
    }

    /**
     * Third Position Decide Card:
     * Uses a basic heuristic to determine the card that should be played if first two players have put down their cards.
     * Includes validation checking to ensure that the second card has not played an invalid move.
     * A card is selected based on the suit declared by the leader.
     * Select the lowest possible card we have in our hand that will win against the leaders card.
     * @return The final card
     */
    public Card thirdPositionPlay() {
        // Retrieve previous two cards from storage
        Card p1Card = storage.retrieveCard(position[0]);
        Card p2Card = storage.retrieveCard(position[1]);

        // Are the two cards played of the same set?
        if (p1Card.suit == p2Card.suit) {
            // Do we have the required suit to play
            List<Card> validSet = cardSet(p2Card.suit, hand);
            if (!validSet.isEmpty()) {
                // We want to play the best card to win
                Card chosen = lowestCardWin(p1Card, p2Card, validSet);
                if(chosen != null) {
                    return hand.remove(cardIndex(chosen));
                } else {
                    Card[] ranked = rankCards(validSet, null);
                    return hand.remove(cardIndex(ranked[0]));
                }
            } else {
                // Check we might have a spade we could play
                List<Card> spadeSet = validSet;
                if (!spadeSet.isEmpty()) {
                    // We want to play the best card to win
                    Card chosen = lowestCardWin(p1Card, p2Card, spadeSet);
                    if(chosen != null) {
                        return hand.remove(cardIndex(chosen));
                    } else {
                        Card[] ranked = rankCards(hand, null);
                        return hand.remove(cardIndex(ranked[0]));
                    }
                } else {
                    // No spade. No suit. Throw the worst card away
                    Card[] ranked = rankCards(hand, null);
                    return hand.remove(cardIndex(ranked[0]));
                }
            }
        } else { // If the cards aren't the same, we only play the leader card as this is the played suit
            List<Card> validSet = cardSet(p1Card.suit, hand);
            if (!validSet.isEmpty()) {
                Card selected = lowestCardWin(p1Card, p2Card, validSet);
                if (selected != null) {
                    return hand.remove(cardIndex(selected));
                } else {
                    Card[] ranked = rankCards(validSet, null);
                    return hand.remove(cardIndex(ranked[0]));
                }
            } else { // Don't have a suit so see if we have a spade
                Card selected = lowestCardWin(p1Card, p2Card, hand);
                if (selected != null) {
                    return hand.remove(cardIndex(selected));
                } else {
                    Card[] ranked = rankCards(hand, null);
                    return hand.remove(cardIndex(ranked[0]));
                }
            }
        }
    }

}

/**
 * Card Storage Class:
 * The purpose of this class is to provide a framework for storing the cards other players have played.
 * Keep account of the cards that the other players have made.
 * Store potential cards that could be played in the future.
 * */
class cardStorage {

    // Stores the cards my agent has played
    String p1Name;
    Stack<Card> p1Cards = new Stack<Card>();

    // Stores the cards player 2 has played
    String p2Name;
    Stack<Card> p2Cards = new Stack<Card>();

    // Stores the cards player 3 has played
    String p3Name;
    Stack<Card> p3Cards = new Stack<Card>();

    // Store the names of each player so we know which stack to store and retrieve the cards
    public cardStorage(String p1, String p2, String p3){
        p1Name = p1; p2Name = p2; p3Name = p3;
    }

    // Store a newly played card in the system
    public void storeCard(String playerName, Card card) {
        if (playerName == p1Name) {
            p1Cards.push(card);
        } else if (playerName == p2Name) {
            p2Cards.push(card);
        } else {
            p3Cards.push(card);
        }
    }

    // Retrieve last played card from the store
    public Card retrieveCard(String playerName) {
        if (playerName == p1Name) {
            return p1Cards.peek();
        } else if (playerName == p2Name) {
            return p2Cards.peek();
        } else {
            return p3Cards.peek();
        }
    }

}
