package com.xpay.sdkjava;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xpay.kotlinutils.XpayUtils;
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dmax.dialog.SpotsDialog;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Unconfined;

public class ProductActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private double itemPrice = 225.50;
    private double totalAmount = itemPrice;
    private Integer shoesCount = 1;
    private String color = "Pink";
    private String size = "38";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 01-start
        // set XpayUtils core settings
        XpayUtils.INSTANCE.setApiKey("Cce74Y3B.J0P4tItq7hGu2ddhCB0WF5ND1eTubkpT");
        XpayUtils.INSTANCE.setCommunityId("m2J7eBK");
        XpayUtils.INSTANCE.setApiPaymentId((Number) 60);
        // 01-end
        setContentView(R.layout.activity_product);
        ChipGroup chipGroupColor = findViewById(R.id.group1);
        ChipGroup chipGroupSize = findViewById(R.id.group);
        FloatingActionButton fab_increase = findViewById(R.id.fab_increase);
        FloatingActionButton fab_decrease = findViewById(R.id.fab_decrease);
        TextView total_price = findViewById(R.id.txt_shoes_amount);
        Button btn_checkout = findViewById(R.id.btnCheckout);
        dialog = new SpotsDialog.Builder().setContext(this).build();

        // increase amount button handler
        fab_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalAmount += itemPrice;
                shoesCount += 1;
                total_price.setText(shoesCount.toString());
            }
        });

        // decrease amount button handler
        fab_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shoesCount > 1) {
                    totalAmount -= itemPrice;
                    shoesCount -= 1;
                    total_price.setText(shoesCount.toString());
                }
            }
        });

        // Get the checked chip instance from sizes chip group
        chipGroupColor.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    for (int i = 0; i < group.getChildCount(); i++) {
                        group.getChildAt(i).setClickable(true);
                    }
                    chip.setClickable(false);
                    size = chip.getText().toString();
                }
            }
        });

        // Get the checked chip instance from colors chip group
        chipGroupSize.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                // Get the checked chip instance from chip group
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    for (int i = 0; i < group.getChildCount(); i++) {
                        group.getChildAt(i).setClickable(true);
                    }
                    chip.setClickable(false);
                    size = chip.getText().toString();
                }
            }
        });

        // Submit button handler
        btn_checkout.setOnClickListener(new View.OnClickListener() {
            // 02-start
            @Override
            public void onClick(View view) {
            // 02-start
                dialog.show();
                XpayUtils.INSTANCE.prepareAmount(totalAmount, prepareCallback);
            // 02-end
            }
        });

    }

    // Prepare amount success case
    void goToCheckout() {
        // 03-start
        // add color and size chosen as a custom fields to be saved with the transaction
        XpayUtils.INSTANCE.addCustomField("color", color);
        XpayUtils.INSTANCE.addCustomField("size", size);
        // 03-end
        dialog.dismiss();

        Intent intent = new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }

    // Prepare amount failure case
    void displayError(String res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG).show();
            }
        });

    }

    Continuation<PrepareAmountData> prepareCallback = new Continuation<PrepareAmountData>() {

        @NotNull
        @Override
        public CoroutineContext getContext() {
            return Unconfined.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object o) {
            try {
                System.out.println(o);
                PrepareAmountData res = (PrepareAmountData) o;
                goToCheckout();
            } catch (Exception e) {
                displayError(e.getMessage());
            }
        }

    };

}