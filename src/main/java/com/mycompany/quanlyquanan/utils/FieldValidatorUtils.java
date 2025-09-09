package com.mycompany.quanlyquanan.utils;

public class FieldValidatorUtils {


        // Kiểm tra rỗng
        public static boolean isEmpty(String value) {
            return value == null || value.trim().isEmpty();
        }

        // Kiểm tra định dạng số
        public static boolean isNumeric(String value) {
            if (isEmpty(value)) return false;
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // Ví dụ: validate độ dài
        public static boolean isLengthBetween(String value, int min, int max) {
            return !isEmpty(value) && value.length() >= min && value.length() <= max;
        }
    }




