package com.xpay.sdkjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xpay.kotlinutils.XpayUtils;
import com.xpay.kotlinutils.models.api.transaction.TransactionData;

import org.jetbrains.annotations.NotNull;

import dmax.dialog.SpotsDialog;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Unconfined;

public class TransactionActivity extends AppCompatActivity {

    AlertDialog dialog;
    String uuid;
    TextView transactionStatus, transactionTotalAmount, transactionUid;
    Button submitButton;
    ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        dialog = new SpotsDialog.Builder().setContext(this).build();
        uuid = getIntent().getStringExtra("UUID");

        transactionStatus = findViewById(R.id.txt_trans_status);
        transactionTotalAmount = findViewById(R.id.txt_status_egp);
        transactionUid = findViewById(R.id.txt_trans_uid);
        submitButton = findViewById(R.id.trans_btn);
        avatar = findViewById(R.id.status_imageView);

        // go to login screen
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void loadTransaction(String Uid) {
        // 02-start
        dialog.show();
        try {
            XpayUtils.INSTANCE.getTransaction(Uid, TransactionCallback);
        } catch (Exception e) {
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // 02-end
    }

    // Load transaction when activity launches
    @Override
    protected void onStart() {
        super.onStart();
        // 01-start
        loadTransaction(uuid);
        // 01-end
    }

    private void updateTransaction(TransactionData res) {
        transactionStatus.setText(res.getStatus() + " TRANSACTION");
        transactionUid.setText(res.getUuid());
        transactionTotalAmount.setText(res.getTotal_amount() + " EGP");

        switch (res.getStatus()) {
            case "SUCCESSFUL": {
                avatar.setImageResource(R.drawable.ic_transaction_success);
                transactionStatus.setTextColor(Color.parseColor("#36DC68"));
                transactionTotalAmount.setTextColor(Color.parseColor("#36DC68"));
                avatar.setColorFilter(ContextCompat.getColor(this, R.color.trans_successs), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            }
            case "FAILED": {
                avatar.setImageResource(R.drawable.ic_transaction_failed);
                transactionStatus.setTextColor(Color.parseColor("#DB0012"));
                transactionTotalAmount.setTextColor(Color.parseColor("#DB0012"));
                avatar.setColorFilter(ContextCompat.getColor(this, R.color.trans_failed), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            }
            case "PENDING": {
                avatar.setImageResource(R.drawable.ic_transaction_pending);
                transactionStatus.setTextColor(Color.parseColor("#B7B7B7"));
                transactionTotalAmount.setTextColor(Color.parseColor("#B7B7B7"));
                avatar.setColorFilter(ContextCompat.getColor(this, R.color.trans_pending));
                break;
            }
        }
        dialog.dismiss();
    }

    Continuation<TransactionData> TransactionCallback = new Continuation<TransactionData>() {

        @NotNull
        @Override
        public CoroutineContext getContext() {
            return Unconfined.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object o) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TransactionData res = (TransactionData) o;
                        updateTransaction(res);
                    }
                });

            } catch (Exception e) {
                dialog.dismiss();
                displayError(e.getMessage());
            }

        }

    };

    void displayError(String res) {
        dialog.dismiss();
        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
    }

}