package ca.mcgill.solitaire.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of distinct cards that can be evaluated as a five-card poker hand.
 * Straights use Ace as the lowest rank only. Comparison considers hand type only.
 */
public final class Hand implements Comparable<Hand> {
    /** Poker categories, ordered from weakest to strongest. */
    public enum PokerType {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
        FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH
    }

    private final List<Card> cards = new ArrayList<>();

    /**
     * Adds a distinct card. Hands may have any size, but evaluation requires five.
     * @param card the card to add
     * @throws NullPointerException if card is null
     * @throws IllegalArgumentException if the card is already present
     */
    public void add(Card card) {
        Objects.requireNonNull(card, "Card cannot be null");
        if (cards.contains(card)) {
            throw new IllegalArgumentException("Duplicate card");
        }
        cards.add(card);
    }

    /** @return the number of cards in this hand */
    public int size() {
        return cards.size();
    }

    /**
     * Evaluates this hand, with Ace-low straights only.
     * @return the poker category
     * @throws IllegalStateException if this hand does not have exactly five cards
     */
    public PokerType pokerType() {
        if (cards.size() != 5) {
            throw new IllegalStateException("A poker hand must contain exactly five cards");
        }
        int[] counts = new int[Rank.values().length];
        for (Card card : cards) {
            counts[card.rank().ordinal()]++;
        }
        boolean flush = cards.stream()
            .allMatch(card -> card.suit() == cards.get(0).suit());
        int[] ranks = cards.stream().mapToInt(card -> card.rank().ordinal()).sorted().toArray();
        boolean straight = true;
        for (int i = 1; i < ranks.length; i++) {
            if (ranks[i] != ranks[i - 1] + 1) {
                straight = false;
                break;
            }
        }
        int pairs = 0;
        boolean three = false;
        boolean four = false;
        for (int count : counts) {
            if (count == 2) pairs++;
            else if (count == 3) three = true;
            else if (count == 4) four = true;
        }
        // Check stronger categories first: e.g. a full house also contains a pair.
        if (straight && flush) return PokerType.STRAIGHT_FLUSH;
        if (four) return PokerType.FOUR_OF_A_KIND;
        if (three && pairs == 1) return PokerType.FULL_HOUSE;
        if (flush) return PokerType.FLUSH;
        if (straight) return PokerType.STRAIGHT;
        if (three) return PokerType.THREE_OF_A_KIND;
        if (pairs == 2) return PokerType.TWO_PAIR;
        if (pairs == 1) return PokerType.ONE_PAIR;
        return PokerType.HIGH_CARD;
    }

    /**
     * Compares poker categories only; different card values do not break ties.
     * @param other the hand to compare with
     * @return negative, zero, or positive if this hand is weaker, equal, or stronger
     * @throws NullPointerException if other is null
     * @throws IllegalStateException if either hand does not have exactly five cards
     */
    @Override
    public int compareTo(Hand other) {
        Objects.requireNonNull(other, "Hand cannot be null");
        return pokerType().compareTo(other.pokerType());
    }
}
