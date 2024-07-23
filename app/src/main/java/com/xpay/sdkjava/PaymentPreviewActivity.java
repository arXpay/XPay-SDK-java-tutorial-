package com.xpay.sdkjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xpay.kotlinutils.XpayUtils;
import com.xpay.kotlinutils.models.api.pay.PayData;

import org.jetbrains.annotations.NotNull;

import dmax.dialog.SpotsDialog;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Unconfined;

public class PaymentPreviewActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private boolean isCardPayment = false;
    private String uuid;
    TextView txt_name, txt_mail, txt_phone, txtcomm_id, txt_varid, txt_method, txt_total;
    Button done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_preview);

        dialog = new SpotsDialog.Builder().setContext(this).build();

        txt_name = findViewById(R.id.textName);
        txt_mail = findViewById(R.id.textEmail);
        txt_phone = findViewById(R.id.txtPhone);
        txtcomm_id = findViewById(R.id.txtCommunity);
        txt_varid = findViewById(R.id.textVariableID);
        txt_method = findViewById(R.id.txtMethod);
        txt_total = findViewById(R.id.totalAmount);
        done = findViewById(R.id.Confirmbtn);

        // 01-start
        txt_name.setText("Name: \n" + XpayUtils.INSTANCE.getBillingInfo().getName());
        txt_mail.setText("Email: \n" + XpayUtils.INSTANCE.getBillingInfo().getEmail());
        txt_phone.setText("Phone: \n" + XpayUtils.INSTANCE.getBillingInfo().getPhone());
        txtcomm_id.setText("Community Id: \n" + XpayUtils.INSTANCE.getCommunityId());
        txt_varid.setText("Variable amount: \n" + XpayUtils.INSTANCE.getApiPaymentId());
        txt_method.setText("Payment method: \n" + XpayUtils.INSTANCE.getPayUsing());
        txt_total.setText("Total Amount: \n" + getIntent().getStringExtra("TOTAL_AMOUNT"));
        // 01-end

        // Confirm button method
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double totalAmount = Double.parseDouble(getIntent().getStringExtra("TOTAL_AMOUNT"));
                if (totalAmount != null) {
                    // 02-start
                    try {
                        dialog.show();
                        XpayUtils.INSTANCE.pay(payCallback);
                    } catch (Exception e) {
                        Toast.makeText(PaymentPreviewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // 02-end
                }
            }
        });

    }

    // when Payment successful
    @SuppressLint("ResourceAsColor")
    private void completePayment(PayData response) {
        dialog.dismiss();
        // 03-start
        if (response.getIframe_url() != null) {
            // if iframe_url inside the returned response is not null, launch a web view to display the payment form
            isCardPayment = true;
            uuid = response.getTransaction_uuid();
            // start a web view and navigate the user to the credit card form
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(R.color.colorPrimary);
            builder.setShowTitle(true);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(response.getIframe_url()));
        } else if (response.getMessage() != null) {
            // if iframe_url inside the returned response is null while the message is not null
            // this is a kiosk or cash collection payment, just display the message to the user
            isCardPayment = false;
            Intent intent = new Intent(this, CashPaymentActivity.class);
            intent.putExtra("UUID", response.getTransaction_uuid());
            intent.putExtra("MESSAGE", response.getMessage());
            startActivity(intent);
        }
        // 03-end
    }

    // method to handle web view dismiss case
    // when the user dismisses the web view then navigate to transaction activity which shows him the transaction info
    @Override
    protected void onRestart() {
        super.onRestart();
        // 04-start
        if (isCardPayment) {
            Intent intent = new Intent(this, TransactionActivity.class);
            intent.putExtra("UUID", uuid);
            startActivity(intent);
        }
        // 04-end
    }

    Continuation<PayData> payCallback = new Continuation<PayData>() {
        @NotNull
        @Override
        public CoroutineContext getContext() {
            return Unconfined.INSTANCE;
        }

        @Override
        public void resumeWith(@NotNull Object o) {
            try {
                PayData res = (PayData) o;
                completePayment(res);
            } catch (Exception e) {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    };

}