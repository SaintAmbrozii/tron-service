package com.example.walletservice.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class MaskingUtil {

    private static final Map<String, Function<Object, String>> FIELD_NAME_TO_MASK = new HashMap<>();

    public static String maskIfNeeded(String fieldName, Object value) {
        if (value == null) {
            return "null";
        }

        fieldName = fieldName.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("_", "")
                .replaceAll("-", "");
        return Optional.ofNullable(FIELD_NAME_TO_MASK.get(fieldName))
                .map(maskFunction -> maskFunction.apply(value))
                .orElse(value.toString());
    }

    public static <T> String maskIfNeeded(String fieldName, List<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return "[]";
        }
        return Optional.ofNullable(FIELD_NAME_TO_MASK.get(fieldName))
                .map(maskFunction ->
                        collection.stream()
                                .map(maskFunction)
                                .collect(Collectors.joining(", ", "[", "]")))
                .orElseGet(() -> {

                    return "[" + collection.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ", "", "")) + "]";
                });
    }

    private static Function<Object, String> maskPhone() {
        return (phoneObj) -> {
            String phone = (String) phoneObj;
            phone = normalizePhone(phone);
            Assert.isTrue(phone.length() == 11, "Length of normalized phone number must be 11 digits.");
            int fifthDigitIdx = 4;
            String prefix = phone.substring(0, fifthDigitIdx);
            int eighthDigitIdx = 7;
            String suffix = phone.substring(eighthDigitIdx);
            return prefix + "***" + suffix;
        };
    }

    private static String normalizePhone(String phone) {
        StringBuilder sb = new StringBuilder();
        for(Character ch: phone.toCharArray()) {
            if (Character.isDigit(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static Function<Object, String> maskEmail() {
        return (emailObj) -> {
            String email = (String) emailObj;
            int atIdx = email.indexOf('@');
            String prefix = email.substring(0, atIdx);
            String suffix = email.substring(atIdx);
            return prefix.charAt(0) + "***" + prefix.charAt(prefix.length() - 1) + suffix;
        };
    }

    static {
        FIELD_NAME_TO_MASK.put("email", maskEmail());
        FIELD_NAME_TO_MASK.put("phone", maskPhone());
        FIELD_NAME_TO_MASK.put("phonenumber", maskPhone());
    }

}
