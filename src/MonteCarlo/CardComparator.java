import java.util.Comparator;

public class CardComparator implements Comparator<Card> {
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
