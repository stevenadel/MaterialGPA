package material.gpa.calculator;

public class Course {
    private float grade;
    private final int creditHours;

    public Course(String gradeLetter, int creditHours) {
        this.creditHours = creditHours;

        switch (gradeLetter) {
            case "A+":
            case "A":  grade = 4.00f;
                       break;
            case "A-": grade = 3.70f;
                       break;
            case "B+": grade = 3.30f;
                       break;
            case "B":  grade = 3.00f;
                       break;
            case "B-": grade = 2.70f;
                       break;
            case "C+": grade = 2.30f;
                       break;
            case "C":  grade = 2.00f;
                       break;
            case "C-": grade = 1.70f;
                       break;
            case "D+": grade = 1.30f;
                       break;
            case "D":  grade = 1.00f;
                       break;
        }
    }

    public int getCreditHours() {
        return creditHours;
    }

    public float calculateQualityPoints() {
        return grade * creditHours;
    }
}