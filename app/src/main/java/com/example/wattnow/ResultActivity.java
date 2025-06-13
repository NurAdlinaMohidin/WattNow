package com.example.wattnow;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private Database database;
    private int billId;

    private TextView textDetailMonth, textDetailUnits, textDetailTotalCharge,
            textDetailRebate, textDetailSavings, textDetailCost;
    private Button buttonUpdate, buttonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        database = new Database(this);

        // Get bill ID passed from previous activity
        billId = getIntent().getIntExtra("billId", -1);

        textDetailMonth = findViewById(R.id.textDetailMonth);
        textDetailUnits = findViewById(R.id.textDetailUnits);
        textDetailTotalCharge = findViewById(R.id.textDetailTotalCharge);
        textDetailRebate = findViewById(R.id.textDetailRebate);
        textDetailSavings = findViewById(R.id.textDetailSavings);
        textDetailCost = findViewById(R.id.textDetailCost);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);

        buttonBack.setOnClickListener(v -> finish());

        // Handle missing or invalid bill ID
        if (billId == -1) {
            Toast.makeText(this, "Invalid Bill ID", Toast.LENGTH_SHORT).show();
            buttonUpdate.setEnabled(false);
            buttonDelete.setEnabled(false);
            return;
        }

        // Load bill details into layout
        loadBillDetails();

        // Update button opens UpdateActivity with the current bill ID
        buttonUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, UpdateActivity.class);
            intent.putExtra("bill_id", billId);
            startActivity(intent);
        });

        // Delete button
        buttonDelete.setOnClickListener(v -> confirmAndDelete());
    }

    // Delete confirmation
    private void confirmAndDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean deleted = database.deleteBillById(billId);
                    if (deleted) {
                        Toast.makeText(this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("isDeleted", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Refresh ViewList on return from update
    @Override
    protected void onResume() {
        super.onResume();
        if (billId != -1) {
            loadBillDetails();
        }
    }

    // Load bill details
    private void loadBillDetails() {
        Cursor cursor = null;
        try {
            cursor = database.getBillById(billId);
            if (cursor != null && cursor.moveToFirst()) {
                String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                double units = cursor.getDouble(cursor.getColumnIndexOrThrow("unit"));
                int rebate = cursor.getInt(cursor.getColumnIndexOrThrow("rebate"));
                double totalCharge = cursor.getDouble(cursor.getColumnIndexOrThrow("totalCharges"));

                double rebateAmount = totalCharge * rebate / 100.0;
                double finalCost = totalCharge - rebateAmount;

                NumberFormat rmFormat = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));

                textDetailMonth.setText("Month: " + month);
                textDetailUnits.setText(String.format("Unit used: %.0f kWh", units));
                textDetailRebate.setText(String.format("Rebate (%%): %d%%", rebate));
                textDetailTotalCharge.setText(rmFormat.format(totalCharge));
                textDetailSavings.setText(rmFormat.format(rebateAmount));
                textDetailCost.setText(rmFormat.format(finalCost));
            } else {
                // Handle null or empty cursor
                Toast.makeText(this, "Failed to load bill details", Toast.LENGTH_SHORT).show();
                buttonUpdate.setEnabled(false);
                buttonDelete.setEnabled(false);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            buttonUpdate.setEnabled(false);
            buttonDelete.setEnabled(false);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
