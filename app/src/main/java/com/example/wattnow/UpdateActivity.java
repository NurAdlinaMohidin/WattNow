package com.example.wattnow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnit;
    private RadioGroup radioGroupRebate;
    private Button buttonCalculate;

    private Database database;
    private int billId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update); // make sure your layout exists and is correct

        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnit = findViewById(R.id.editTextUnit);
        radioGroupRebate = findViewById(R.id.radioGroupRebate);
        buttonCalculate = findViewById(R.id.buttonCalculateBill);

        database = new Database(this);

        // Get bill ID passed from previous screen
        Intent intent = getIntent();
        billId = intent.getIntExtra("bill_id", -1);
        if (billId == -1) {
            Toast.makeText(this, "Invalid bill ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load current bill detail into form
        loadBillData(billId);

        // Handle update action
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBill();
            }
        });
    }

    // Load existing bill data into the form
    private void loadBillData(int id) {
        Cursor cursor = database.getBillById(id);
        if (cursor != null && cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
            double unit = cursor.getDouble(cursor.getColumnIndexOrThrow("unit"));
            int rebate = cursor.getInt(cursor.getColumnIndexOrThrow("rebate"));

            cursor.close();

            setSpinnerToMonth(month);
            editTextUnit.setText(String.valueOf((int) unit));
            setRebateRadio(rebate);
        }
    }

    private void setSpinnerToMonth(String month) {
        for (int i = 0; i < spinnerMonth.getCount(); i++) {
            if (spinnerMonth.getItemAtPosition(i).toString().equalsIgnoreCase(month)) {
                spinnerMonth.setSelection(i);
                break;
            }
        }
    }

    private void setRebateRadio(int rebate) {
        for (int i = 0; i < radioGroupRebate.getChildCount(); i++) {
            RadioButton rb = (RadioButton) radioGroupRebate.getChildAt(i);
            String text = rb.getText().toString();
            if (text.equals(rebate + "%")) {
                rb.setChecked(true);
                break;
            }
        }
    }

    // Perform update logic with proper validation and tiered rate calculation
    private void updateBill() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitStr = editTextUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            editTextUnit.setError("Please enter electricity units used");
            editTextUnit.requestFocus();
            return;
        }

        double unit;
        try {
            unit = Double.parseDouble(unitStr);
        } catch (NumberFormatException e) {
            editTextUnit.setError("Invalid number");
            editTextUnit.requestFocus();
            return;
        }

        int selectedRebateId = radioGroupRebate.getCheckedRadioButtonId();
        if (selectedRebateId == -1) {
            Toast.makeText(this, "Please select a rebate percentage", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedRebateId);
        int rebate = Integer.parseInt(selectedRadio.getText().toString().replace("%", ""));

        // Calculations
        double totalCharges = calculateCharges(unit);
        double rebateSavings = totalCharges * rebate / 100.0;
        double finalCost = totalCharges - rebateSavings;

        // Update database
        boolean success = database.updateBill(billId, month, unit, rebate, totalCharges, finalCost, rebateSavings);

        if (success) {
            Toast.makeText(this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
        }
    }

    // Tiered electricity charge calculation (same as CalculationActivity)
    private double calculateCharges(double units) {
        double charges = 0;
        if (units <= 200) {
            charges = units * 0.218;
        } else if (units <= 300) {
            charges = 200 * 0.218 + (units - 200) * 0.334;
        } else if (units <= 600) {
            charges = 200 * 0.218 + 100 * 0.334 + (units - 300) * 0.516;
        } else if (units <= 900) {
            charges = 200 * 0.218 + 100 * 0.334 + 300 * 0.516 + (units - 600) * 0.546;
        } else {
            charges = 200 * 0.218 + 100 * 0.334 + 300 * 0.516 + 300 * 0.546 + (units - 900) * 0.546;
        }
        return charges;
    }
}
