package material.gpa.calculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    float semesterQualityPoints;
    int semesterCreditHours;
    List<Spinner> spinners;
    List<EditText> editTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPrefs.getString("theme", "system");
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        setContentView(R.layout.activity_main);

        Spinner spinner1 = findViewById(R.id.grade1_spinner);
        Spinner spinner2 = findViewById(R.id.grade2_spinner);
        Spinner spinner3 = findViewById(R.id.grade3_spinner);
        Spinner spinner4 = findViewById(R.id.grade4_spinner);
        Spinner spinner5 = findViewById(R.id.grade5_spinner);
        Spinner spinner6 = findViewById(R.id.grade6_spinner);
        spinners = new ArrayList<>(Arrays.asList(spinner1, spinner2, spinner3, spinner4, spinner5, spinner6));
        for (Spinner spinner: spinners) {
            populateSpinner(spinner, sharedPrefs);
        }

        EditText editText1 = findViewById(R.id.credit_hours1);
        EditText editText2 = findViewById(R.id.credit_hours2);
        EditText editText3 = findViewById(R.id.credit_hours3);
        EditText editText4 = findViewById(R.id.credit_hours4);
        EditText editText5 = findViewById(R.id.credit_hours5);
        EditText editText6 = findViewById(R.id.credit_hours6);
        editTexts = new ArrayList<>(Arrays.asList(editText1, editText2, editText3, editText4, editText5, editText6));

        int startingNumber = Integer.parseInt(sharedPrefs.getString("min", "6"));
        if (startingNumber == 7) {
            LinearLayout extraLayout = findViewById(R.id.extra_layout);
            ConstraintLayout newCourse =
                    (ConstraintLayout) LayoutInflater.from(this)
                    .inflate(R.layout.extra_course, extraLayout, false);

            TextView courseExtra = newCourse.findViewById(R.id.course_extra);
            Spinner spinnerExtra = newCourse.findViewById(R.id.grade_spinner_extra);
            courseExtra.setText("Course " + startingNumber);
            populateSpinner(spinnerExtra, sharedPrefs);

            extraLayout.addView(newCourse);
        }

        List<ConstraintLayout> extraCoursesLayouts = new ArrayList<>();
        Context context = this;
        int maxExtraCourses = Integer.parseInt(sharedPrefs.getString("max", "1"));
        final Button addCourseButton = findViewById(R.id.add_course);
        addCourseButton.setOnClickListener(new View.OnClickListener()
        {
            int counterExtra = 0;
            @Override
            public void onClick(View v) {
                if (counterExtra < maxExtraCourses) {
                    LinearLayout extraLayout = findViewById(R.id.extra_layout);
                    ConstraintLayout newCourse =
                            (ConstraintLayout) LayoutInflater.from(context)
                            .inflate(R.layout.extra_course, extraLayout, false);

                    TextView courseExtra = newCourse.findViewById(R.id.course_extra);
                    Spinner spinnerExtra = newCourse.findViewById(R.id.grade_spinner_extra);
                    counterExtra++;
                    courseExtra.setText("Course " + (startingNumber + counterExtra));
                    populateSpinner(spinnerExtra, sharedPrefs);

                    extraLayout.addView(newCourse);
                    extraCoursesLayouts.add(newCourse);
                } else {
                    sendToastMessage("Are you really taking more than "
                                       + (startingNumber + maxExtraCourses) + " courses?");
                }
            }
        });

        final Button calculateButton = findViewById(R.id.calculate);
        calculateButton.setOnClickListener(v -> {
            Course[] courses = new Course[6 + extraCoursesLayouts.size()];
            boolean creditHoursMissing = false;

            if (editTexts.size() != courses.length) {
                addExtraCourses(extraCoursesLayouts);
            }

            for (int i =0; i < editTexts.size(); i++) {
                String creditHoursString = editTexts.get(i).getText().toString();
                creditHoursMissing = creditHoursString.isEmpty();
                if (!creditHoursMissing) {
                    Course course = new Course(spinners.get(i).getSelectedItem().toString(),
                                               Integer.parseInt(creditHoursString));
                    courses[i] = course;
                } else {
                    break;
                }
            }

            if (!creditHoursMissing) {
                semesterQualityPoints = 0.00f;
                semesterCreditHours = 0;
                for (Course course : courses) {
                    semesterQualityPoints += course.calculateQualityPoints();
                    semesterCreditHours += course.getCreditHours();
                }

                if (semesterCreditHours > 0) {
                    float newSemesterGPA = roundGPA(semesterQualityPoints / semesterCreditHours);
                    float newCumulativeGPA = calculateNewGPA();
                    if (newCumulativeGPA != 0.00f) {
                        TextView semesterGPA = findViewById(R.id.semester_GPA);
                        TextView cumulativeGPA = findViewById(R.id.cumulative_GPA);
                        semesterGPA.setText(Float.toString(newSemesterGPA));
                        cumulativeGPA.setText(Float.toString(newCumulativeGPA));
                    }
                } else {
                    sendToastMessage("Courses with zero credit do not affect GPA");
                }
            } else {
                sendToastMessage("Please enter all relevant credit hours");
            }
        });
    }

    private float calculateNewGPA() {
        EditText creditEditText = findViewById(R.id.previous_credit);
        EditText pGPAEditText = findViewById(R.id.previous_GPA);
        String creditString = creditEditText.getText().toString();
        String pGPAString = pGPAEditText.getText().toString();

        if (!creditString.isEmpty() && !pGPAString.isEmpty()) {
            int oldCreditHours = Integer.parseInt(creditString);
            float oldGPA = Float.parseFloat(pGPAString);

            if (oldGPA <= 4.00f) {
                return roundGPA((oldGPA * oldCreditHours + semesterQualityPoints)
                                   / (oldCreditHours + semesterCreditHours));
            } else {
                sendToastMessage("GPA cannot be more than 4.00");
                return 0.00f;
            }
        } else {
            sendToastMessage("Please enter valid credit hours & GPA");
            return 0.00f;
        }
    }

    private float roundGPA(float d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void sendToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void populateSpinner(Spinner spinner, SharedPreferences sharedPrefs) {
        boolean useAPlus = sharedPrefs.getBoolean("a_plus", false);
        ArrayAdapter<CharSequence> adapter;
        if (useAPlus) {
            adapter = ArrayAdapter.createFromResource(this, R.array.grades_array_plus, android.R.layout.simple_spinner_item);
        } else {
            adapter = ArrayAdapter.createFromResource(this, R.array.grades_array, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());
    }

    private void addExtraCourses(List<ConstraintLayout> extraCoursesLayouts) {
        for (ConstraintLayout extraCourse: extraCoursesLayouts) {
            Spinner spinnerExtra = extraCourse.findViewById(R.id.grade_spinner_extra);
            EditText editTextExtra = extraCourse.findViewById(R.id.credit_hours_extra);
            spinners.add(spinnerExtra);
            editTexts.add(editTextExtra);
        }
    }

    private static class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {}

        public void onNothingSelected(AdapterView<?> parent) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearAll();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearAll() {
        EditText creditEditText = findViewById(R.id.previous_credit);
        EditText pGPAEditText = findViewById(R.id.previous_GPA);
        creditEditText.getText().clear();
        pGPAEditText.getText().clear();
        for (EditText editText: editTexts) {
            editText.getText().clear();
        }
        for (Spinner spinner: spinners) {
            spinner.setSelection(0);
        }
        TextView semesterGPA = findViewById(R.id.semester_GPA);
        TextView cumulativeGPA = findViewById(R.id.cumulative_GPA);
        semesterGPA.setText("0.00");
        cumulativeGPA.setText("0.00");
    }
}