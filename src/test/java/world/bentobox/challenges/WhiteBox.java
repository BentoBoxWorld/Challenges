package world.bentobox.challenges;

public class WhiteBox {
    /**
     * Sets the value of a private static field using Java Reflection.
     */
    public static void setInternalState(Class<?> targetClass, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set static field '" + fieldName + "' on class " + targetClass.getName(), e);
        }
    }
}
