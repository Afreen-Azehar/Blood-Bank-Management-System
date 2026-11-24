package com.bloodbank;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Small helper methods: blood group validation and compatibility rules. */
public class Validator {

    private static final Set<String> VALID_GROUPS =
            Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    // recipient blood group -> donor groups that can safely give (red cells)
    private static final Map<String, List<String>> COMPATIBLE = Map.of(
            "O-", List.of("O-"),
            "O+", List.of("O+", "O-"),
            "A-", List.of("A-", "O-"),
            "A+", List.of("A+", "A-", "O+", "O-"),
            "B-", List.of("B-", "O-"),
            "B+", List.of("B+", "B-", "O+", "O-"),
            "AB-", List.of("AB-", "A-", "B-", "O-"),
            "AB+", List.of("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-")
    );

    public static String normalizeGroup(String input) {
        return input == null ? "" : input.trim().toUpperCase();
    }

    public static boolean isValidGroup(String group) {
        return VALID_GROUPS.contains(group);
    }

    public static List<String> compatibleDonorGroups(String recipientGroup) {
        return COMPATIBLE.getOrDefault(recipientGroup, List.of());
    }
}
