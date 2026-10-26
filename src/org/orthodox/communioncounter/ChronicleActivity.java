package org.orthodox.communioncounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle);

        dbHelper = new LiturgyDbHelper(this);

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

    private void setupListeners() {
        btnBackToCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Long press to delete a record
        lvRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final LiturgyRecord record = adapter.getItem(position);
                showDeleteDialog(record);
                return true;
            }
        });
    }

    private void showDeleteDialog(final LiturgyRecord record) {
        new AlertDialog.Builder(this, R.style.LiturgicalDialogTheme)
                .setTitle(R.string.dialog_delete_title)
                .setMessage("Да ли сте сигурни да желите да обришете запис за:\n\n" +
                        record.getFeastName() + " (" + record.getDateFormatted() + ") - " +
                        record.getCount() + " причесника?")
                .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteRecord(record.getId());
                        Toast.makeText(ChronicleActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                })
                .setNegativeButton("Откажи", null)
                .show();
    }
}
