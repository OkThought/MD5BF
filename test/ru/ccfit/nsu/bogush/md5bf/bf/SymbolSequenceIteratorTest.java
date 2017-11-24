package ru.ccfit.nsu.bogush.md5bf.bf;


import org.junit.Test;

import static org.junit.Assert.*;

public class SymbolSequenceIteratorTest {
    private static final char[] a = new char[] {'a'};
    private static final char[] b = new char[] {'b'};
    private static final char[] d = new char[] {'d'};
    private static final char[] aa = new char[] {'a', 'a'};
    private static final char[] ab = new char[] {'a', 'b'};
    private static final char[] ba = new char[] {'b', 'a'};
    private static final char[] bb = new char[] {'b', 'b'};
    private static final char[] dd = new char[] {'d', 'd'};
    private static final char[] abcd = new char[] {'a', 'b', 'c', 'd'};
    private static final char[] empty = new char[0];

    public SymbolSequenceIteratorTest() {}

    @Test
    public void empty() {
        SymbolSequenceIterator iterator;
        iterator = new SymbolSequenceIterator(abcd, empty, empty);
        assertTrue(iterator.hasNext());
        assertArrayEquals(empty, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void firstToA() {
        SymbolSequenceIterator iterator;
        iterator = new SymbolSequenceIterator(abcd, empty, a);
        assertTrue(iterator.hasNext());
        assertArrayEquals(empty, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(a, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void firstToABCD() {
        SymbolSequenceIterator iterator;
        iterator = new SymbolSequenceIterator(abcd, empty, d);
        assertTrue(iterator.hasNext());
        assertArrayEquals(empty, iterator.next());
        for (char character : abcd) {
            assertTrue(iterator.hasNext());
            assertArrayEquals(new char[]{character}, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void lengths0To3() {
        SymbolSequenceIterator iterator;
        for (int i = 0; i <= 3; i++) {
            iterator = new SymbolSequenceIterator(abcd, SymbolSequenceIterator.firstSequence(abcd, i), i);
            char[] chars = null;
            for (; iterator.hasNext();) {
                chars = iterator.next();
                assertNotNull(chars);
                assertEquals(i, chars.length);
            }
            for (char c : chars) {
                assertEquals('d', c);
            }
        }
    }

    @Test
    public void checkOrderAB() {
        SymbolSequenceIterator iterator;
        iterator = new SymbolSequenceIterator(ab, empty, 2);
        assertTrue(iterator.hasNext());
        assertArrayEquals(empty, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(a, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(b, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(aa, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(ab, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(ba, iterator.next());
        assertTrue(iterator.hasNext());
        assertArrayEquals(bb, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void firstSequence() {
        assertArrayEquals(empty, SymbolSequenceIterator.firstSequence(empty, 0));
        assertArrayEquals(empty, SymbolSequenceIterator.firstSequence(a, 0));
        assertArrayEquals(a, SymbolSequenceIterator.firstSequence(a, 1));
        assertArrayEquals(a, SymbolSequenceIterator.firstSequence(abcd, 1));
    }

    @Test
    public void lastSequence() {
        assertArrayEquals(empty, SymbolSequenceIterator.lastSequence(empty, 0));
        assertArrayEquals(empty, SymbolSequenceIterator.lastSequence(a, 0));
        assertArrayEquals(a, SymbolSequenceIterator.lastSequence(a, 1));
        assertArrayEquals(d, SymbolSequenceIterator.lastSequence(abcd, 1));
    }

}