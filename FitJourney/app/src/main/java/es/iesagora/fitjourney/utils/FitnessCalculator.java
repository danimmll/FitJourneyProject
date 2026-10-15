package es.iesagora.fitjourney.utils;

import java.util.Calendar;

/**
 * Utilidad encargada de realizar cálculos relacionados con fitness y rendimiento físico.
 * Esta clase proporciona métodos estáticos para calcular métricas como:
 * edad, IMC, porcentaje de grasa corporal, masa magra, categorías de IMC,
 * equivalentes metabólicos (MET) para distintos tipos de ejercicio y calorías consumidas.
 * No mantiene estado interno y actúa únicamente como clase de utilidades matemáticas.
 */
public class FitnessCalculator {
    private FitnessCalculator() {
        /*No se deben crear instancias de esta clase*/
    }


    /**
     * Calcula la edad a partir de una fecha de nacimiento en formato String.
     * Se espera que la cadena contenga al menos el año en los primeros 4 caracteres (YYYY).
     * Si el formato es inválido, devuelve una edad por defecto.
     *
     * @param birthDateStr fecha de nacimiento en formato texto (YYYY-MM-DD o similar)
     * @return edad calculada o 25 como valor por defecto si hay error
     */
    public static int calculateAge(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.trim().isEmpty()) return 25;
        try {
            String normalized = birthDateStr.trim();
            int birthYear;

            if (normalized.matches("^\\d{4}[-/].*")) {
                birthYear = Integer.parseInt(normalized.substring(0, 4));
            } else if (normalized.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                birthYear = Integer.parseInt(normalized.substring(6, 10));
            } else if (normalized.length() >= 4) {
                birthYear = Integer.parseInt(normalized.substring(0, 4));
            } else {
                return 25;
            }

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int age = currentYear - birthYear;
            return Math.max(0, Math.min(age, 120));
        } catch (Exception e) {
            return 25;
        }
    }

    /**
     * Limita un valor dentro de un rango mínimo y máximo.
     *
     * @param value valor a limitar
     * @param min valor mínimo permitido
     * @param max valor máximo permitido
     * @return valor ajustado dentro del rango
     */
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calcula el índice de masa corporal (BMI).
     * Fórmula: peso(kg) / altura(m)^2
     *
     * @param weightKg peso en kilogramos
     * @param heightCm altura en centímetros
     * @return valor del BMI o 0 si la altura es inválida
     */
    public static float calculateBMI(float weightKg, float heightCm) {
        if (heightCm <= 0) return 0f;
        float heightM = heightCm / 100f;
        return weightKg / (heightM * heightM);
    }

    /**
     * Calcula el porcentaje estimado de grasa corporal.
     * Tiene en cuenta el BMI, la edad y el género.
     * Además ajusta el resultado según el objetivo del usuario.
     *
     * @param bmi índice de masa corporal
     * @param age edad del usuario
     * @param gender género del usuario (masculino/femenino)
     * @param goal objetivo del usuario (fuerza, músculo, pérdida de peso, etc.)
     * @return porcentaje estimado de grasa corporal
     */
    public static float calculateBodyFat(float bmi, int age, String gender, String goal) {
        boolean isMale = "masculino".equalsIgnoreCase(gender)
                || "male".equalsIgnoreCase(gender)
                || "hombre".equalsIgnoreCase(gender);

        float bodyFat = isMale
                ? (1.39f * bmi) + (0.16f * age) - 19.34f
                : (1.39f * bmi) + (0.16f * age) - 9f;

        if (goal != null) {
            String g = goal.toLowerCase();
            if (g.contains("músculo") || g.contains("muscle")
                    || g.contains("fuerza") || g.contains("strength")
                    || g.contains("masa") || g.contains("muscular")) {
                bodyFat -= (isMale ? 6.5f : 4.5f);
            }
        }

        return Math.max(bodyFat, isMale ? 4f : 12f);
    }

    /**
     * Calcula la masa magra del usuario.
     *
     * @param weightKg peso total en kg
     * @param bodyFatPercentage porcentaje de grasa corporal
     * @return masa magra en kg
     */
    public static float calculateLeanMass(float weightKg, float bodyFatPercentage) {
        return weightKg * (1f - (bodyFatPercentage / 100f));
    }

    /**
     * Determina la categoría del BMI según el objetivo del usuario.
     * Si el objetivo es ganar músculo o fuerza, las categorías se ajustan
     * para reflejar un contexto de volumen.
     *
     * @param bmi índice de masa corporal
     * @param goal objetivo del usuario
     * @return categoría del BMI en formato texto
     */
    public static String getBMICategory(float bmi, String goal) {
        if (bmi < 18.5) return "Bajo peso";

        if (goal != null) {
            String g = goal.toLowerCase();
            if (g.contains("músculo") || g.contains("muscle")
                    || g.contains("fuerza") || g.contains("strength")
                    || g.contains("masa") || g.contains("muscular")) {

                if (bmi < 27.5) return "✓ Atlético / Musculado";
                if (bmi < 32) return "Sobrepeso (Volumen)";
                return "Obesidad";
            }
        }

        if (bmi < 25) return "✓ Normal";
        if (bmi < 30) return "Sobrepeso";
        return "Obesidad";
    }

    /**
     * Calcula el MET (equivalente metabólico) para ejercicios cardiovasculares.
     * Ajusta la intensidad según velocidad e inclinación.
     *
     * @param baseMet valor base del ejercicio
     * @param currentSpeedKmh velocidad actual en km/h
     * @param inclinePercentage inclinación en porcentaje
     * @return MET ajustado
     */
    public static float calculateCardioMET(float baseMet, float currentSpeedKmh, float inclinePercentage) {
        float speedFactor = clamp(currentSpeedKmh / 8f, 0.6f, 1.8f);
        float inclineFactor = clamp(1f + (inclinePercentage / 10f), 1.0f, 2.0f);
        return baseMet * speedFactor * inclineFactor;
    }

    /**
     * Rango del entrenamiento con pesas según el Compendium of Physical Activities
     * (Ainsworth et al., 2011): unos 3,5 MET con esfuerzo ligero y 6 MET con esfuerzo
     * vigoroso, contando los descansos entre series.
     */
    private static final float MET_FUERZA_LIGERO = 3.5f;
    private static final float MET_FUERZA_VIGOROSO = 6.0f;

    /**
     * Calcula el MET para entrenamiento de fuerza.
     * Coloca el ejercicio dentro del rango del Compendium según la carga relativa al peso
     * corporal y el descanso entre series. Las series y las repeticiones no cambian el MET:
     * ya se notan en la duración, que es la que multiplica en {@link #calculateCaloriesBurned}.
     *
     * @param weightLiftedKg peso medio levantado en las series
     * @param bodyWeightKg peso corporal del usuario
     * @param restSeconds descanso entre series en segundos
     * @return MET estimado, entre 3,5 y 6
     */
    public static float calculateStrengthMET(float weightLiftedKg, float bodyWeightKg, int restSeconds) {
        float safeBodyWeight = bodyWeightKg > 0 ? bodyWeightKg : 70f;
        float relativeLoad = weightLiftedKg / safeBodyWeight;
        // 0 con un cuarto del peso corporal o menos; 1 a partir del propio peso corporal
        float intensity = clamp((relativeLoad - 0.25f) / 0.75f, 0f, 1f);

        // 0 con dos minutos de descanso o más; 1 con medio minuto o menos
        float density = clamp((120f - restSeconds) / 90f, 0f, 1f);

        float effort = 0.75f * intensity + 0.25f * density;
        return MET_FUERZA_LIGERO + (MET_FUERZA_VIGOROSO - MET_FUERZA_LIGERO) * effort;
    }

    /**
     * Calcula el MET para ejercicios de estiramiento.
     * Depende del tipo (dinámico o estático), intensidad y nivel de experiencia.
     *
     * @return MET estimado del stretching
     */
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

    /**
     * Calcula las calorías quemadas durante un ejercicio.
     * Fórmula: MET × peso × tiempo (horas)
     *
     * @param dynamicMet MET del ejercicio
     * @param weightKg peso del usuario
     * @param durationHours duración del ejercicio en horas
     * @return calorías estimadas consumidas
     */
    public static float calculateCaloriesBurned(float dynamicMet, float weightKg, float durationHours) {
        return dynamicMet * weightKg * durationHours;
    }
}