package ru.ccfit.nsu.bogush.md5bf.bf;

import java.math.BigInteger;
import java.util.*;

public class SymbolSequenceIterator implements Iterator<char[]> {
    private char[] alphabet;
    private char[] current;
    private char[] last;
    private Map<Character, Integer> charIndexInAlphabetMap;
    private int maxLength;
    private boolean isFirst = true;
    private boolean hasNext = true;
    private char firstAlphabetSymbol;
    private char lastAlphabetSymbol;

    public SymbolSequenceIterator(char[] alphabet) {
        this(alphabet, Integer.MAX_VALUE);
    }

    public SymbolSequenceIterator(char[] alphabet, int maxLength) {
        this(alphabet, new char[0], maxLength);
    }

    public SymbolSequenceIterator(char[] alphabet, int startingLength, int maxLength) {
        this(alphabet, firstSequence(alphabet, startingLength), maxLength);
    }

    public SymbolSequenceIterator(char[] alphabet, int startingLength, char[] lastSequence) {
        this(alphabet, firstSequence(alphabet, startingLength), lastSequence);
    }

    public SymbolSequenceIterator(char[] alphabet, char[] firstSequence, int maxLength) {
        this(alphabet, firstSequence, lastSequence(alphabet, maxLength));
    }

    public SymbolSequenceIterator(char[] alphabet, char[] firstSequence, char[] lastSequence) {
        this.alphabet = alphabet;
        if (alphabet.length > 0) {
            this.firstAlphabetSymbol = alphabet[0];
            this.lastAlphabetSymbol = alphabet[alphabet.length - 1];
        }
        this.current = firstSequence;
        this.last = lastSequence;
        this.maxLength = lastSequence.length;
        this.charIndexInAlphabetMap = new HashMap<>();
        for (int i = 0; i < alphabet.length; ++i) {
            this.charIndexInAlphabetMap.put(alphabet[i], i);
        }
    }

    public static int numberOfSequencesOfLength(int length, int alphabetSize) {
        return (int) Math.pow(alphabetSize, length);
    }

    public static char[] firstSequence(char[] alphabet, int length) {
        char[] chars = new char[length];
        if (alphabet.length > 0) {
            Arrays.fill(chars, alphabet[0]);
        }
        return chars;
    }

    public static char[] lastSequence(char[] alphabet, int length) {
        char[] chars = new char[length];
        if (alphabet.length > 0) {
            Arrays.fill(chars, alphabet[alphabet.length - 1]);
        }
        return chars;
    }

    @Override
    public boolean hasNext() {
        return hasNext && isFirst ?
                compare(current, last) <= 0 :
                compare(current, last) < 0;

    }

    @Override
    public char[] next() {
        if (isFirst) {
            isFirst = false;
            return current;
        }

        if (alphabet.length == 0) {
            hasNext = false;
            return current;
        }

        int currentPosition = current.length - 1;

        if (hasNext) while (!tryIncrement(current, alphabet, currentPosition)) {
            if (currentPosition >= 0)
                current[currentPosition] = firstAlphabetSymbol;
            --currentPosition;

            if (currentPosition < 0) {
                if (current.length == maxLength) {
                    hasNext = false;
                    break;
                }
                current = new char[current.length + 1];
                Arrays.fill(current, firstAlphabetSymbol);
                break;
            }
        }

        return current;
    }

    private boolean tryIncrement(char[] current, char[] alphabet, int position) {
        if (position < 0) return false;
        char currentChar = current[position];
        if (currentChar == lastAlphabetSymbol) {
            return false;
        }
        int currentCharIndex = charIndexInAlphabetMap.get(currentChar);
        current[position] = alphabet[currentCharIndex + 1];
        return true;
    }

    private int compare(char[] a, char[] b) {
        if (a.length < b.length) return -1;
        if (a.length > b.length) return +1;
        for (int i = 0; i < a.length; ++i) {
            int ai = charIndexInAlphabetMap.get(a[i]);
            int bi = charIndexInAlphabetMap.get(b[i]);
            if (ai < bi) return -1;
            if (ai > bi) return +1;
        }
        return 0;
    }

    public int numberOfSequencesOfLength(int length) {
        return (int) BigInteger.valueOf((long) alphabet.length).pow(length).longValueExact();
    }
}
