package ru.ccfit.nsu.bogush.md5bf.client;

import org.junit.Test;

import static org.junit.Assert.*;
import static ru.ccfit.nsu.bogush.md5bf.client.TaskCreator.concat;

public class TaskCreatorTest {
    private static final char[] empty = new char[0];
    private static final char[] a = new char[] {'a'};
    private static final char[] b = new char[] {'b'};
    private static final char[] ab = new char[] {'a', 'b'};
    private static final char[] aaaa = new char[] {'a', 'a', 'a', 'a'};
    private static final char[] bbbb = new char[] {'b', 'b', 'b', 'b'};
    private static final char[] aaaabbbb = new char[] {'a', 'a', 'a', 'a', 'b', 'b', 'b', 'b'};

    @Test
    public void concatNull() {
        char[] chars;

        chars = concat(null, null);
        assertNull(chars);

        chars = concat(null, a);
        assertArrayEquals(a, chars);

        chars = concat(a, null);
        assertArrayEquals(a, chars);

        chars = concat(null, empty);
        assertArrayEquals(empty, chars);

        chars = concat(empty, null);
        assertArrayEquals(empty, chars);
    }

    @Test
    public void concatEmpty() {
        char[] chars;

        chars = concat(empty, empty);
        assertArrayEquals(empty, chars);

        chars = concat(a, empty);
        assertArrayEquals(a, chars);

        chars = concat(empty, a);
        assertArrayEquals(a, chars);

        chars = concat(aaaa, empty);
        assertArrayEquals(aaaa, chars);

        chars = concat(empty, aaaa);
        assertArrayEquals(aaaa, chars);
    }

    @Test
    public void concatCustom() {
        char[] chars;

        chars = concat(a, b);
        assertArrayEquals(ab, chars);

        chars = concat(aaaa, bbbb);
        assertArrayEquals(aaaabbbb, chars);
    }
}