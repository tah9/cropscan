package com.nuist.tool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * ->  tah9  2023/4/28 21:09
 */
public class ReflectUtil {
    public static boolean setValue(@Nullable Object source, @NonNull Class<?> target,
                                   @NonNull String name, @Nullable Object value) {
        Field field = null;
        int modify = 0;
        Field modifiersField = null;
        boolean removeFinal = false;
        try {
            field = target.getDeclaredField(name);
            modify = field.getModifiers();
            //final修饰的基本类型不可修改
            if (field.getType().isPrimitive() && Modifier.isFinal(modify)) {
                return false;
            }
            //获取访问权限
            if (!Modifier.isPublic(modify) || Modifier.isFinal(modify)) {
                field.setAccessible(true);
            }
            //static final同时修饰
            removeFinal = Modifier.isStatic(modify) && Modifier.isFinal(modify);
            if (removeFinal) {
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modify & ~Modifier.FINAL);
            }
            //按照类型调用设置方法
            if (value != null && field.getType().isPrimitive()) {
                if ("int".equals(field.getType().getName()) && value instanceof Number) {
                    field.setInt(source, ((Number) value).intValue());
                } else if ("boolean".equals(field.getType().getName()) && value instanceof Boolean) {
                    field.setBoolean(source, (Boolean) value);
                } else if ("byte".equals(field.getType().getName()) && value instanceof Byte) {
                    field.setByte(source, (Byte) value);
                } else if ("char".equals(field.getType().getName()) && value instanceof Character) {
                    field.setChar(source, (Character) value);
                } else if ("double".equals(field.getType().getName()) && value instanceof Number) {
                    field.setDouble(source, ((Number) value).doubleValue());
                } else if ("long".equals(field.getType().getName()) && value instanceof Number) {
                    field.setLong(source, ((Number) value).longValue());
                } else if ("float".equals(field.getType().getName()) && value instanceof Number) {
                    field.setFloat(source, ((Number) value).floatValue());
                } else if ("short".equals(field.getType().getName()) && value instanceof Number) {
                    field.setShort(source, ((Number) value).shortValue());
                } else {
                    return false;
                }
            } else {
                field.set(source, value);
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                //权限还原
                if (field != null) {
                    if (removeFinal && modifiersField != null) {
                        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        modifiersField.setAccessible(false);
                    }
                    if (!Modifier.isPublic(modify) || Modifier.isFinal(modify)) {
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                //
            }
        }
        return true;
    }


}
