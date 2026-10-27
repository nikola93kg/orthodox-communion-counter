package org.orthodox.communioncounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.orthodox.communioncounter.db.LiturgyDbHelper;
import org.orthodox.communioncounter.model.LiturgyRecord;

import java.util.List;
import java.util.Locale;

public class ChronicleActivity extends Activity {

    private LiturgyDbHelper dbHelper;
    private ListView lvRecords;
    private TextView tvEmptyRecords;
    private TextView tvStatTotalCommunicants;
    private TextView tvStatTotalLiturgies;
    private TextView tvStatAverage;
    private Button btnBackToCounter;
    private LiturgyAdapter adapter;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen awake while reviewing the chronicle
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_chronicle);

        dbHelper = new LiturgyDbHelper(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        lvRecords = findViewById(R.id.lv_records);
        tvEmptyRecords = findViewById(R.id.tv_empty_records);
        tvStatTotalCommunicants = findViewById(R.id.tv_stat_total_communicants);
        tvStatTotalLiturgies = findViewById(R.id.tv_stat_total_liturgies);
        tvStatAverage = findViewById(R.id.tv_stat_average);
        btnBackToCounter = findViewById(R.id.btn_back_to_counter);
    }

    private void loadData() {
        List<LiturgyRecord> records = dbHelper.getAllRecords();

        if (adapter == null) {
            adapter = new LiturgyAdapter(this, records);
            adapter.setOnRecordActionListener(new LiturgyAdapter.OnRecordActionListener() {
                @Override
                public void onDelete(LiturgyRecord record) {
                    triggerHapticFeedback(45);
                    showDeleteDialog(record);
                }
            });
            lvRecords.setAdapter(adapter);
        } else {
            adapter.updateData(records);
        }

        if (records.isEmpty()) {
            tvEmptyRecords.setVisibility(View.VISIBLE);
            lvRecords.setVisibility(View.GONE);
        } else {
            tvEmptyRecords.setVisibility(View.GONE);
            lvRecords.setVisibility(View.VISIBLE);
        }

        // Update statistics
        LiturgyDbHelper.SummaryStats stats = dbHelper.getSummaryStats();
        tvStatTotalCommunicants.setText(String.valueOf(stats.totalCommunicants));
        tvStatTotalLiturgies.setText(String.valueOf(stats.totalLiturgies));
        tvStatAverage.setText(String.format(Locale.getDefault(), "%.1f", stats.averagePerLiturgy));
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
        btnBackToCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Backup long click on ListView
        lvRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final LiturgyRecord record = adapter.getItem(position);
                triggerHapticFeedback(45);
                showDeleteDialog(record);
                return true;
            }
        });
    }

    private void showDeleteDialog(final LiturgyRecord record) {
        String fullDate = (record.getDayName() != null && !record.getDayName().isEmpty())
                ? record.getDayName() + ", " + record.getDateFormatted()
                : record.getDateFormatted();

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.LiturgicalDialogTheme)
                .setTitle("🗑️ " + getString(R.string.dialog_delete_title))
                .setMessage("Да ли сте сигурни да желите да обришете овај запис из Летописа?\n\n" +
                        "• Служба: " + record.getFeastName() + "\n" +
                        "• Датум: " + fullDate + "\n" +
                        "• Причешћено: " + record.getCount() + " верника")
                .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dbHelper.deleteRecord(record.getId());
                        Toast.makeText(ChronicleActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.55);
            if (dialogWidth < 500) dialogWidth = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
