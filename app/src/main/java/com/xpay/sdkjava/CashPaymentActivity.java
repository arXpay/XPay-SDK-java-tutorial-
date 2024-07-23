package com.xpay.sdkjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CashPaymentActivity extends AppCompatActivity {

    TextView txtStatus, txtMsg, txtUid;
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_payment);

        txtStatus = findViewById(R.id.textView3);
        txtMsg = findViewById(R.id.txtMsg);
        txtUid = findViewById(R.id.txtUid);
        btnDone = findViewById(R.id.btnDone);

        String uuid = getIntent().getStringExtra("UUID");
        String message = getIntent().getStringExtra("MESSAGE");
        txtUid.setText(uuid);
        txtMsg.setText(message);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }
}