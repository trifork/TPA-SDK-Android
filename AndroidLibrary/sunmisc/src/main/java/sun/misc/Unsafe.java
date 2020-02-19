package sun.misc;

import java.lang.reflect.Field;

@SuppressWarnings({"unused", "RedundantThrows"})
public class Unsafe {

    // Int
    public int getInt(long address) {
        return 0;
    }

    public int getInt(Object obj, long offset) {
        return 0;
    }

    public void putInt(long address, int value) {

    }

    public void putInt(Object obj, long offset, int newValue) {

    }

    // Long
    public long getLong(long address) {
        return 0;
    }

    public long getLong(Object obj, long offset) {
        return 0;
    }

    public void putLong(long address, long value) {

    }

    public void putLong(Object obj, long offset, long newValue) {

    }

    // Byte
    public byte getByte(long address) {
        return 0;
    }

    public byte getByte(Object target, long offset) {
        return 0;
    }

    public void putByte(long address, byte value) {

    }

    public void putByte(Object target, long offset, byte value) {

    }

    // Boolean
    public boolean getBoolean(Object target, long offset) {
        return false;
    }

    public void putBoolean(Object target, long offset, boolean value) {

    }

    // Float
    public float getFloat(Object target, long offset) {
        return 0;
    }

    public void putFloat(Object target, long offset, float value) {

    }

    // Double
    public double getDouble(Object target, long offset) {
        return 0;
    }

    public void putDouble(Object target, long offset, double value) {

    }

    // Object
    public Object getObject(Object obj, long offset) {
        return null;
    }

    public void putObject(Object target, long offset, Object value) {

    }

    // Allocate
    public <T> Object allocateInstance(Class<T> clazz) throws InstantiationException {
        return null;
    }

    // Field Offset
    public long objectFieldOffset(Field field) {
        return 0;
    }

    public int arrayBaseOffset(Class<?> clazz) {
        return 0;
    }

    // Scale
    public int arrayIndexScale(Class<?> clazz) {
        return 0;
    }

    // Memory
    public void copyMemory(Object o, long srcOffset, byte[] target, long l, long length) {

    }

    // Static Field
    public Object staticFieldBase(Field field) {
        return null;
    }

    public long staticFieldOffset(Field field) {
        return 0;
    }
}
