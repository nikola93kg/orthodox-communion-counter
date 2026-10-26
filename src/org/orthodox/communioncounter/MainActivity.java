package org.orthodox.communioncounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.orthodox.communioncounter.db.LiturgyDbHelper;
import org.orthodox.communioncounter.model.LiturgyRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "CommunionCounterPrefs";
    private static final String KEY_COUNT = "saved_count";

    private TextView tvCounterDisplay;
    private TextView tvCurrentDate;
    private FrameLayout tapCounterArea;
    private Button btnUndo;
    private Button btnFinishSave;
    private Button btnReset;
    private Button btnOpenChronicle;

    private int currentCount = 0;
    private Vibrator vibrator;
    private LiturgyDbHelper dbHelper;

    private static final String[] SERBIAN_DAYS = {
            "Недеља", "Понедељак", "Уторак", "Среда", "Четвртак", "Петак", "Субота"
    };

    private static final String[] SERBIAN_MONTHS = {
            "јануар", "фебруар", "март", "април", "мај", "јун",
            "јул", "август", "септембар", "октобар", "новембар", "децембар"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on during Holy Liturgy
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        dbHelper = new LiturgyDbHelper(this);

        initViews();
        restoreSavedState();
        updateDateDisplay();
        setupListeners();
    }

    private void initViews() {
        tvCounterDisplay = findViewById(R.id.tv_counter_display);
        tvCurrentDate = findViewById(R.id.tv_current_date);
        tapCounterArea = findViewById(R.id.tap_counter_area);
        btnUndo = findViewById(R.id.btn_undo);
        btnFinishSave = findViewById(R.id.btn_finish_save);
        btnReset = findViewById(R.id.btn_reset);
        btnOpenChronicle = findViewById(R.id.btn_open_chronicle);
    }

    private void restoreSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentCount = prefs.getInt(KEY_COUNT, 0);
        updateCounterDisplay();
    }

    private void saveCurrentCount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_COUNT, currentCount).apply();
    }

    private void updateCounterDisplay() {
        if (tvCounterDisplay != null) {
            tvCounterDisplay.setText(String.valueOf(currentCount));
        }
        if (btnUndo != null) {
            btnUndo.setEnabled(currentCount > 0);
            btnUndo.setAlpha(currentCount > 0 ? 1.0f : 0.4f);
        }
    }

    private void updateDateDisplay() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        String serbianDate = SERBIAN_DAYS[dayOfWeek] + ", " + dayOfMonth + ". " + SERBIAN_MONTHS[month] + " " + year + ".";
        tvCurrentDate.setText(serbianDate);
    }

    private void triggerHapticFeedback(int durationMs) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        }
    }

    private void setupListeners() {
        // Main huge tap target (+1)
        tapCounterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCount++;
                updateCounterDisplay();
                saveCurrentCount();
                triggerHapticFeedback(35);
            }
        });

        // Undo button (-1)
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentCount > 0) {
                    currentCount--;
                    updateCounterDisplay();
                    saveCurrentCount();
                    triggerHapticFeedback(25);
                }
            }
        });

        // Reset button with confirmation
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmationDialog();
            }
        });

        // Finish and save to Church Chronicle
        btnFinishSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveLiturgyDialog();
            }
        });

        // Open Chronicle activity
        btnOpenChronicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChronicleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this, R.style.LiturgicalDialogTheme)
                .setTitle(R.string.confirm_reset_title)
                .setMessage(R.string.confirm_reset_msg)
                .setPositiveButton("Да, ресетуј", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentCount = 0;
                        updateCounterDisplay();
                        saveCurrentCount();
                        triggerHapticFeedback(50);
                    }
                })
                .setNegativeButton("Откажи", null)
                .show();
    }

    private void showSaveLiturgyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.LiturgicalDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_save_liturgy, null);
        builder.setView(dialogView);

        final TextView tvDialogCount = dialogView.findViewById(R.id.tv_dialog_count);
        final TextView tvDialogDate = dialogView.findViewById(R.id.tv_dialog_date);
        final EditText etFeastName = dialogView.findViewById(R.id.et_feast_name);
        final EditText etNotes = dialogView.findViewById(R.id.et_notes);

        final Button btnPresetSunday = dialogView.findViewById(R.id.btn_preset_sunday);
        final Button btnPresetFeast = dialogView.findViewById(R.id.btn_preset_feast);
        final Button btnPresetSlava = dialogView.findViewById(R.id.btn_preset_slava);
        final Button btnPresetPresanctified = dialogView.findViewById(R.id.btn_preset_presanctified);

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        String dayName = SERBIAN_DAYS[dayOfWeek];
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault());
        final String formattedDate = sdf.format(new Date());

        tvDialogCount.setText(String.valueOf(currentCount));
        tvDialogDate.setText(dayName + ", " + formattedDate);

        // Pre-select default feast based on Sunday or weekday
        if (dayOfWeek == 0) {
            etFeastName.setText(getString(R.string.feast_preset_sunday));
        } else {
            etFeastName.setText(getString(R.string.feast_preset_feast));
        }

        // Preset button listeners
        btnPresetSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFeastName.setText(getString(R.string.feast_preset_sunday));
            }
        });
        btnPresetFeast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFeastName.setText(getString(R.string.feast_preset_feast));
            }
        });
        btnPresetSlava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFeastName.setText(getString(R.string.feast_preset_slava));
            }
        });
        btnPresetPresanctified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFeastName.setText(getString(R.string.feast_preset_presanctified));
            }
        });

        final AlertDialog dialog = builder.create();

        Button btnSaveEntry = dialogView.findViewById(R.id.btn_save_entry);
        Button btnCancelEntry = dialogView.findViewById(R.id.btn_cancel_entry);

        final String finalDayName = dayName;
        btnSaveEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feast = etFeastName.getText().toString().trim();
                if (feast.isEmpty()) {
                    feast = getString(R.string.feast_preset_sunday);
                }
                String notes = etNotes.getText().toString().trim();

                LiturgyRecord record = new LiturgyRecord(
                        System.currentTimeMillis(),
                        formattedDate,
                        finalDayName,
                        feast,
                        currentCount,
                        notes
                );

                dbHelper.insertRecord(record);
                Toast.makeText(MainActivity.this, R.string.toast_saved, Toast.LENGTH_SHORT).show();

                // Reset counter for next service
                currentCount = 0;
                updateCounterDisplay();
                saveCurrentCount();

                dialog.dismiss();

                // Open chronicle to show saved entry
                Intent intent = new Intent(MainActivity.this, ChronicleActivity.class);
                startActivity(intent);
            }
        });

        btnCancelEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.58);
            if (dialogWidth < 500) dialogWidth = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
