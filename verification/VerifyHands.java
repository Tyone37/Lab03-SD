import ca.mcgill.solitaire.cards.*;
import ca.mcgill.solitaire.cards.Hand.PokerType;

/** Standalone checks: no JUnit dependency is required. */
public final class VerifyHands {
    private static int checks;
    private static Hand hand(int[] ranks, Suit... suits) {
        Hand hand = new Hand();
        for (int i = 0; i < ranks.length; i++) {
            hand.add(Card.get(Rank.values()[ranks[i] - 1], suits[i]));
        }
        return hand;
    }
    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
        checks++;
    }
    private static void rejects(Class<? extends Throwable> expected, Runnable action) {
        try { action.run(); }
        catch (Throwable error) {
            if (!expected.isInstance(error)) throw new AssertionError("Wrong exception", error);
            checks++;
            return;
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }
    public static void main(String[] args) {
        Suit c = Suit.CLUBS, d = Suit.DIAMONDS, h = Suit.HEARTS, s = Suit.SPADES;
        Hand[] categories = {
            hand(new int[]{2, 5, 8, 11, 13}, c,d,h,s,c),
            hand(new int[]{2, 2, 8, 11, 13}, c,d,h,s,c),
            hand(new int[]{2, 2, 8, 8, 13}, c,d,h,s,c),
            hand(new int[]{2, 2, 2, 11, 13}, c,d,h,s,c),
            hand(new int[]{1, 2, 3, 4, 5}, c,d,h,s,c),
            hand(new int[]{2, 5, 8, 11, 13}, c,c,c,c,c),
            hand(new int[]{2, 2, 2, 13, 13}, c,d,h,s,c),
            hand(new int[]{2, 2, 2, 2, 13}, c,d,h,s,c),
            hand(new int[]{5, 6, 7, 8, 9}, h,h,h,h,h)
        };
        for (int i = 0; i < categories.length; i++) {
            check(categories[i].pokerType() == PokerType.values()[i], "Wrong category " + i);
            System.out.println("PASS: " + categories[i].pokerType());
            for (int j = 0; j < categories.length; j++) {
                check(Integer.signum(categories[i].compareTo(categories[j])) == Integer.signum(i-j),
                    "Wrong comparison " + i + ", " + j);
            }
        }
        Hand kings = hand(new int[]{13,13,13,5,9}, c,d,h,c,s);
        check(kings.compareTo(categories[3]) == 0, "Same type should tie regardless of rank");
        check(hand(new int[]{10,11,12,13,1}, c,d,h,s,c).pokerType() == PokerType.HIGH_CARD,
            "Ace-high straight should not be recognized in the simplified version");
        check(hand(new int[]{13,1,2,3,4}, c,d,h,s,c).pokerType() == PokerType.HIGH_CARD,
            "Straight cannot wrap around King and Ace");
        check(hand(new int[]{6,4,7,3,5}, c,d,h,s,c).pokerType() == PokerType.STRAIGHT,
            "Input order should not matter");
        for (int size : new int[]{0,1,4,6}) {
            Hand invalid = new Hand();
            for (int i = 0; i < size; i++) invalid.add(Card.get(Rank.values()[i], c));
            check(invalid.size() == size, "Wrong size");
            rejects(IllegalStateException.class, invalid::pokerType);
            rejects(IllegalStateException.class, () -> invalid.compareTo(categories[0]));
            rejects(IllegalStateException.class, () -> categories[0].compareTo(invalid));
        }
        Hand duplicate = new Hand();
        Card ace = Card.get(Rank.ACE, c);
        duplicate.add(ace);
        rejects(IllegalArgumentException.class, () -> duplicate.add(ace));
        check(duplicate.size() == 1, "Duplicate changed hand");
        rejects(NullPointerException.class, () -> duplicate.add(null));
        rejects(NullPointerException.class, () -> categories[0].compareTo(null));
        System.out.println("PASS: " + checks + " checks (categories, comparisons, Ace-low, invalid sizes, duplicate/null cards).");
    }
}
