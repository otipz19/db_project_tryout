package controllerlib.servlet;

public class TypeUtils {
    public static Object getDefaultValue(Class<?> type) {
        if (type.equals(int.class)) {
            return 0;
        } else if (type.equals(boolean.class)) {
            return false;
        } else if (type.equals(char.class)) {
            return 0;
        } else if (type.equals(double.class)) {
            return 0.0;
        } else if (type.equals(float.class)) {
            return 0.0f;
        } else if (type.equals(long.class)) {
            return 0L;
        } else if (type.equals(short.class)) {
            return (short) 0;
        } else if (type.equals(byte.class)) {
            return (byte) 0;
        } else {
            return null;
        }
    }

    public static Object parsePrimitives(Class<?> type, String stringValue) {
        if (type.equals(String.class)) {
            return stringValue;
        } else if (type.equals(int.class)) {
            return Integer.parseInt(stringValue);
        } else if (type.equals(boolean.class)) {
            return Boolean.parseBoolean(stringValue);
        } else if (type.equals(char.class)) {
            return stringValue.charAt(0);
        } else if (type.equals(double.class)) {
            return Double.parseDouble(stringValue);
        } else if (type.equals(float.class)) {
            return Float.parseFloat(stringValue);
        } else if (type.equals(long.class)) {
            return Long.parseLong(stringValue);
        } else if (type.equals(short.class)) {
            return Short.parseShort(stringValue);
        } else if(type.equals(byte.class)){
            return Byte.parseByte(stringValue);
        } else {
            throw new RuntimeException("Not primitive");
        }
    }
}
