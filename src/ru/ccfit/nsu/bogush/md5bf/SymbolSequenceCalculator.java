package ru.ccfit.nsu.bogush.md5bf;

import java.util.Arrays;

public class SymbolSequenceCalculator {
    public static long pow(int a, int b) {
        if (a == 0) return 0;
        if (b == 0) return 1;
        if (b == 1) return a;
        if (b % 2 == 0) return pow(a*a, b/2);
        return a * pow(a*a, b/2);
    }

    public static long numberOfSequences(int sequenceLength, int alphabetSize) {
        long numberOfSequences = 1;
        for (int i = 1; i <= sequenceLength; ++i) {
            numberOfSequences += pow(alphabetSize, i);
        }
        return numberOfSequences;
    }

    public static int sequenceLength(long sequenceIndex, int alphabetSize) {
        if (sequenceIndex == 0) return 0;
        if (sequenceIndex <= alphabetSize) return 1;
        int length1 = (int) Math.floor(Math.log(sequenceIndex) / Math.log(alphabetSize));
        int length2 = length1+1;
        if (sequenceIndex < numberOfSequences(length1, alphabetSize)) {
            return length1;
        } else {
            return length2;
        }
    }

    public static int[] sequence(long sequenceIndex, int alphabetSize) {
        int length = sequenceLength(sequenceIndex, alphabetSize);
        int[] sequence = new int[length];
        if (length == 0) return sequence;
        long lastIndexOfPreviousLength = numberOfSequences(length-1, alphabetSize);
        long cur = sequenceIndex - lastIndexOfPreviousLength;
        long next;
        for (int i = length-1; i >= 0; --i) {
            next = cur / alphabetSize;
            int rest = (int) (cur % alphabetSize);
            sequence[i] = rest;
            cur = next;
        }
        return sequence;
    }

    public static String stringFromSequenceIndex(long sequenceIndex, String alphabet) {
        int[] sequence = sequence(sequenceIndex, alphabet.length());
        char[] chars = new char[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            chars[i] = alphabet.charAt(sequence[i]);
        }
        return new String(chars);
    }

    public static long sequenceIndex(String sequence, String alphabet) {
        if (sequence.length() == 0) return 0;
        long sequenceIndex = numberOfSequences(sequence.length()-1, alphabet.length());
        char[] chars = sequence.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sequenceIndex += pow(alphabet.length(), chars.length-i-1) * alphabet.indexOf(chars[i]);
        }
        return sequenceIndex;
    }
}
