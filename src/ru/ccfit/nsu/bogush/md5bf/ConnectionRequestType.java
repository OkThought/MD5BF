package ru.ccfit.nsu.bogush.md5bf;

import java.util.HashMap;

enum ConnectionRequestType {
    TASK_REQUEST, TASK_DONE, UNKNOWN;

    private static final byte TASK_REQUEST_BYTE = 0;
    private static final byte TASK_DONE_BYTE = 1;

    private static HashMap<ConnectionRequestType, Byte> state2byteMap = new HashMap<>();
    private static HashMap<Byte, ConnectionRequestType> byte2stateMap = new HashMap<>();

    static {
        state2byteMap.put(TASK_REQUEST, (byte) TASK_REQUEST_BYTE);
        state2byteMap.put(TASK_DONE, (byte) TASK_DONE_BYTE);
        byte2stateMap.put(TASK_REQUEST_BYTE, TASK_REQUEST);
        byte2stateMap.put(TASK_DONE_BYTE, TASK_DONE);
    }

    static ConnectionRequestType forByte(Byte b) {
        return byte2stateMap.getOrDefault(b, UNKNOWN);
    }

    byte toByte() {
        return state2byteMap.get(this);
    }
}
