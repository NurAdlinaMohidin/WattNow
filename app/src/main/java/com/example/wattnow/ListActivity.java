package com.example.wattnow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ListActivity extends AppCompatActivity {

    private Database database;
    private ListView listViewBills;
    private ArrayList<String> months;
    private ArrayList<String> costs;
    private ArrayList<Integer> billIds;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean isDeleted = result.getData().getBooleanExtra("isDeleted", false);
                    if (isDeleted) {
                        loadBillList(); // Refresh the list if a bill was deleted
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listViewBills = findViewById(R.id.listViewBills);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        database = new Database(this);

        // Load bill records from database
        loadBillList();

        // Floating action button to add new bill
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, CalculationActivity.class);
            startActivity(intent);
        });

        buttonBack.setOnClickListener(v -> finish());

        // Open ResultActivity after clicking on one of the ID
        listViewBills.setOnItemClickListener((parent, view, position, id) -> {
            int selectedBillId = billIds.get(position);
            Intent intent = new Intent(ListActivity.this, ResultActivity.class);
            intent.putExtra("billId", selectedBillId);
            resultLauncher.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBillList(); // Refresh list when activity is resumed
    }

    // Loads bill records form the database and updates the ListView
    private void loadBillList() {
        months = new ArrayList<>();
        costs = new ArrayList<>();
        billIds = new ArrayList<>();

        Cursor cursor = database.getAllBills();
        if (cursor != null && cursor.moveToFirst()) {
            NumberFormat rmFormat = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));

            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("finalCost"));

                months.add(month);
                costs.add(rmFormat.format(finalCost));
                billIds.add(id);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Show month and final cost calculation of the ID
        CustomBillAdapter adapter = new CustomBillAdapter(this, months, costs);
        listViewBills.setAdapter(adapter);
    }

    private static class CustomBillAdapter extends ArrayAdapter<String> {
        private final ArrayList<String> months;
        private final ArrayList<String> costs;
        private final LayoutInflater inflater;

        public CustomBillAdapter(@NonNull AppCompatActivity context, ArrayList<String> months, ArrayList<String> costs) {
            super(context, R.layout.list_item_bill, months);
            this.months = months;
            this.costs = costs;
            this.inflater = context.getLayoutInflater();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = inflater.inflate(R.layout.list_item_bill, parent, false);
            }

            TextView textMonth = itemView.findViewById(R.id.textMonth);
            TextView textCost = itemView.findViewById(R.id.textFinalCost);

            textMonth.setText(months.get(position));
            textCost.setText(costs.get(position));

            return itemView;
        }
    }
}
