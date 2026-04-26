package es.iesagora.fitjourney.utils;

import java.util.Calendar;

public class FitnessCalculator {

    public static int calculateAge(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.length() < 4) return 25; // Por defecto
        try {
            int birthYear = Integer.parseInt(birthDateStr.substring(0, 4));
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return currentYear - birthYear;
        } catch (Exception e) {
            return 25;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }


    public static float calculateBMI(float weightKg, float heightCm) {
        if (heightCm <= 0) return 0f;
        float heightM = heightCm / 100f;
        return weightKg / (heightM * heightM);
    }

    public static float calculateBodyFat(float bmi, int age, String gender, String goal) {
        boolean isMale = "masculino".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender) || "hombre".equalsIgnoreCase(gender);
        float bodyFat = isMale ? (1.39f * bmi) + (0.16f * age) - 19.34f
                : (1.39f * bmi) + (0.16f * age) - 9f;

        if (goal != null) {
            String g = goal.toLowerCase();
            if (g.contains("músculo") || g.contains("muscle") || g.contains("fuerza") || g.contains("strength") || g.contains("masa") || g.contains("muscular")) {
                bodyFat -= (isMale ? 6.5f : 4.5f);
            }
        }

        return Math.max(bodyFat, isMale ? 4f : 12f);
    }

    public static float calculateLeanMass(float weightKg, float bodyFatPercentage) {
        return weightKg * (1f - (bodyFatPercentage / 100f));
    }

    public static String getBMICategory(float bmi, String goal) {
        if (bmi < 18.5) return "Bajo peso";

        if (goal != null) {
            String g = goal.toLowerCase();
            if (g.contains("músculo") || g.contains("muscle") || g.contains("fuerza") || g.contains("strength") || g.contains("masa") || g.contains("muscular")) {
                if (bmi < 27.5) return "✓ Atlético / Musculado";
                if (bmi < 32) return "Sobrepeso (Volumen)";
                return "Obesidad";
            }
        }

        if (bmi < 25) return "✓ Normal";
        if (bmi < 30) return "Sobrepeso";
        return "Obesidad";
    }

    public static float calculateCardioMET(float baseMet, float currentSpeedKmh, float inclinePercentage) {
        float speedFactor = clamp(currentSpeedKmh / 8f, 0.6f, 1.8f);
        float inclineFactor = clamp(1f + (inclinePercentage / 10f), 1.0f, 2.0f);
        return baseMet * speedFactor * inclineFactor;
    }

    public static float calculateStrengthMET(float weightLiftedKg, float bodyWeightKg, int sets, int reps, int restSeconds) {
        float baseMet = 6f;
        float intensityFactor = clamp(weightLiftedKg / bodyWeightKg, 0.3f, 1.5f);
        float volumeFactor = clamp((sets * reps) / 30f, 0.5f, 2.0f);

        float safeRestSeconds = restSeconds > 0 ? restSeconds : 1f;
        float restFactor = clamp(60f / safeRestSeconds, 0.7f, 1.1f);

        return baseMet * (1f + intensityFactor) * (1f + volumeFactor) * restFactor;
    }

    public static float calculateStretchingMET(boolean isDynamic, String intensity, String experienceLevel) {
        float baseMet = isDynamic ? 3.5f : 2.5f;

        float intensityFactor = 1.0f;
        if (intensity != null) {
            if (intensity.equalsIgnoreCase("Normal")) intensityFactor = 1.2f;
            else if (intensity.equalsIgnoreCase("Intenso")) intensityFactor = 1.5f;
        }

        float experienceFactor = 1.0f;
        if (experienceLevel != null) {
            if (experienceLevel.equalsIgnoreCase("Principiante")) experienceFactor = 0.9f;
            else if (experienceLevel.equalsIgnoreCase("Avanzado")) experienceFactor = 1.1f;
        }

        return baseMet * intensityFactor * experienceFactor;
    }

    public static float calculateCaloriesBurned(float dynamicMet, float weightKg, float durationHours) {
        return dynamicMet * weightKg * durationHours;
    }
}