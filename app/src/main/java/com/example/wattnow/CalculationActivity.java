package com.example.wattnow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class CalculationActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnit;
    private RadioGroup radioGroupRebate;
    private LinearLayout inputSection, resultSection;
    private TextView textViewInsertedMonth, textViewInsertedUnit, textViewInsertedRebate;
    private TextView textViewTotalCharges, textViewFinalCost, textViewRebateSavings;

    private com.example.wattnow.Database db;
    private MediaPlayer mediaPlayer;

    private static final String NOTIFICATION_CHANNEL_ID = "ICT602";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);

        db = new Database(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnit = findViewById(R.id.editTextUnit);
        radioGroupRebate = findViewById(R.id.radioGroupRebate);

        inputSection = findViewById(R.id.inputSection);
        resultSection = findViewById(R.id.resultSection);

        textViewInsertedMonth = findViewById(R.id.textViewInsertedMonth);
        textViewInsertedUnit = findViewById(R.id.textViewInsertedUnit);
        textViewInsertedRebate = findViewById(R.id.textViewInsertedRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        textViewRebateSavings = findViewById(R.id.textViewRebateSavings);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());
    }

    // Triggered when "Calculate" button is pressed
    public void calculateBill(View view) {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitStr = editTextUnit.getText().toString();

        // Input validation: units
        if (unitStr.isEmpty()) {
            Toast.makeText(this, "Please enter units used", Toast.LENGTH_SHORT).show();
            return;
        }

        double units = Double.parseDouble(unitStr);

        // Input validation: rebate
        int selectedRebateId = radioGroupRebate.getCheckedRadioButtonId();
        if (selectedRebateId == -1) {
            Toast.makeText(this, "Please select rebate percentage", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedRebateId);
        int rebatePercent = Integer.parseInt(selectedRadio.getText().toString().replace("%", ""));

        // Calculations
        double totalCharges = calculateCharges(units);
        double rebateAmount = totalCharges * rebatePercent / 100.0;
        double finalCost = totalCharges - rebateAmount;

        // Save result to database
        db.insertCalculation(month, units, rebatePercent, totalCharges, finalCost, rebateAmount);

        // Hide input section and display result section after calculation
        inputSection.setVisibility(View.GONE);
        resultSection.setVisibility(View.VISIBLE);

        NumberFormat rmFormat = NumberFormat.getCurrencyInstance(new java.util.Locale("ms", "MY"));

        textViewInsertedMonth.setText("Month: " + month);
        textViewInsertedUnit.setText(String.format("Unit used: %.0f kWh", units));
        textViewInsertedRebate.setText(String.format("Rebate (%%): %d%%", rebatePercent));
        textViewTotalCharges.setText(rmFormat.format(totalCharges));
        textViewFinalCost.setText(rmFormat.format(finalCost));
        textViewRebateSavings.setText(rmFormat.format(rebateAmount));

        textViewRebateSavings.setVisibility(View.VISIBLE);

        // Trigger notification
        showNotificationWithSound(rebateAmount, rebatePercent);
    }

    // Tiered electricity charge calculation
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

    // Show notification with rebate amount and play sound
    private void showNotificationWithSound(double rebateAmount, int rebatePercent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Bill Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification about rebate savings");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Intent when notification is tapped
        Intent intent = new Intent(this, CalculationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Notification details
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Rebate Savings")
                .setContentText(String.format("Congrats! You saved RM %.2f from %d%% rebate!", rebateAmount, rebatePercent))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show notification
        notificationManager.notify(1, builder.build());

        // Play sound
        mediaPlayer = MediaPlayer.create(this, R.raw.notification);
        mediaPlayer.start();
        mediaPlayer.setLooping(false);
    }

    // Triggered when "New Bill" button is pressed
    public void newBill(View view) {
        inputSection.setVisibility(View.VISIBLE);
        resultSection.setVisibility(View.GONE);
        editTextUnit.setText("");
        radioGroupRebate.clearCheck();
    }

    // Triggered when "View Bill" button is pressed
    public void viewBill(View view) {
        Intent intent = new Intent(CalculationActivity.this, ListActivity.class);
        startActivity(intent);
    }
}