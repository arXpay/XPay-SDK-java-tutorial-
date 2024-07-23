package com.xpay.sdkjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xpay.kotlinutils.XpayUtils;
import com.xpay.kotlinutils.models.BillingInfo;
import com.xpay.kotlinutils.models.PaymentMethods;
import com.xpay.kotlinutils.models.ShippingInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UserInfoActivity extends AppCompatActivity {

    EditText name, email, phone, street, building, apartment, floor;
    Spinner paymentMethodsDropdown, countryDropdown, state;
    ConstraintLayout parent;
    TextView totalAmount;
    Number amount = 0;
    Button submit;
    private List<PaymentMethods> mSpinnerData = XpayUtils.INSTANCE.getActivePaymentMethods();
    private List<String> mStateData = null;
    ArrayAdapter<String> adapterCity = null;
    Boolean validForm = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_info);
        name = findViewById(R.id.userName);
        email = findViewById(R.id.userEmail);
        phone = findViewById(R.id.userPhone);
        street = findViewById(R.id.et_street);
        building = findViewById(R.id.et_building);
        apartment = findViewById(R.id.et_apartment);
        floor = findViewById(R.id.et_floor);

        paymentMethodsDropdown = findViewById(R.id.paymentMethodsDropdown);
        countryDropdown = findViewById(R.id.sp_country);
        state = findViewById(R.id.sp_state);

        totalAmount = findViewById(R.id.totalAmountTxt);
        parent = findViewById(R.id.constraint_shipping);
        submit = findViewById(R.id.btnSubmit);
        //  01-start
        // Populate paymentMethodsDropdown with available active payment methods
        ArrayAdapter<String> adapter = null;
        List<String> paymentMethodsList = new ArrayList<String>();
        // get the available active payment methods and convert it to List<String>
        for (PaymentMethods i : mSpinnerData) {
            paymentMethodsList.add(i.toString());
        }


        Set<String> set = new LinkedHashSet<>(paymentMethodsList);
        paymentMethodsList.clear();
        paymentMethodsList.addAll(set);

        // Populate paymentMethodsDropdown with available active payment methods
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, paymentMethodsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodsDropdown.setAdapter(adapter);
        //  01-end
        // set actual amount for different payment methods
        paymentMethodsDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (XpayUtils.INSTANCE.getActivePaymentMethods().get(i)) {
                    // 02-start
                    case CASH: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getCash();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.CASH);
                        showView(parent);
                        break;
                    }
                    case CARD: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getCard();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.CARD);
                        hideView(parent);
                        validForm = true;
                        break;
                    }
                    case KIOSK: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getKiosk();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.KIOSK);
                        hideView(parent);
                        validForm = true;
                        break;
                    }
                    // version 2
                    case MEEZA: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getMeeza();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.MEEZA);
                        hideView(parent);
                        validForm = true;
                        break;
                    }
                    case FAWRY: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getFawry();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.FAWRY);
                        hideView(parent);
                        validForm = true;
                        break;
                    }
                    case VALU: {
                        amount = Objects.requireNonNull(XpayUtils.INSTANCE.getPaymentOptionsTotalAmounts()).getValu();
                        XpayUtils.INSTANCE.setPayUsing(PaymentMethods.VALU);
                        hideView(parent);
                        validForm = true;
                        break;
                    }
                }
                amount = Double.parseDouble(String.format("%.2f", amount));
                totalAmount.setText("Total Amount: " + amount + " Egp");
                // 02-end
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // 03-start
        // get the value of countries-cities combinations from assets
        JSONObject obj = new JSONObject();
        String jsonFileString = getJsonDataFromAsset(this, "countries.json");
        try {
            assert jsonFileString != null;
            obj = new JSONObject(jsonFileString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<String> finalAllCountries = populateCountries(obj);
        JSONObject finalObj = (JSONObject) obj;
        // 03-end

        /// when a country is selected, populate its cities
        countryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 04-start
                populateStates(finalObj, finalAllCountries.get(i));
                // 04-end
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // submit button method
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate shipping info(in case cash collection method is selected)
                // 05-start
                boolean validShippingInfo = true;
                if (parent.getVisibility() == View.VISIBLE) {
                    if (validateShippingInfo()) {
                        validShippingInfo = true;
                        // set payment shipping info
                        XpayUtils.INSTANCE.setShippingInfo(new ShippingInfo(
                                "EG",
                                state.getSelectedItem().toString(),
                                countryDropdown.getSelectedItem().toString(),
                                apartment.getText().toString(),
                                building.getText().toString(),
                                floor.getText().toString(),
                                street.getText().toString()
                        ));
                    } else validShippingInfo = false;
                }

                if (validateBillingInfo() && validShippingInfo) {
                    // set payment billing info
                    try {
                        XpayUtils.INSTANCE.setBillingInfo(new BillingInfo(name.getText().toString(), email.getText().toString(), "+2" + phone.getText().toString()));
                        Intent intent = new Intent(getApplicationContext(), PaymentPreviewActivity.class);
                        intent.putExtra("TOTAL_AMOUNT", amount.toString());
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                // 05-end
            }
        });
    }

    // validate email check method
    boolean isEmailValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private String getJsonDataFromAsset(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
        return jsonString;
    }

    // populate countries dropdown list and return a list of countries
    ArrayList<String> populateCountries(JSONObject obj) {
        ArrayAdapter<String> countryAdapter = null;
        Iterator<String> keys = obj.keys();
        ArrayList<String> countriesList = new ArrayList<String>();
        for (int i = 0; i < obj.names().length(); i++) {
            countriesList.add(keys.next());
        }
        countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countriesList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryDropdown.setAdapter(countryAdapter);
        return countriesList;
    }


    // populate cities dropdown list
    void populateStates(JSONObject obj, String key) {
        JSONArray sessionArray = obj.optJSONArray(key);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < sessionArray.length(); i++) {
            try {
                list.add(sessionArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mStateData = list;
        adapterCity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStateData);
        adapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(adapterCity);
    }

    // Show shipping info form
    void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    // Hide shipping info form
    void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    // validate shipping info form
    boolean validateShippingInfo() {
        // get shipping info
        String streetInfo = street.getText().toString();
        String buildingInfo = building.getText().toString();
        String apartmentInfo = apartment.getText().toString();
        String floorInfo = floor.getText().toString();

        if (!streetInfo.isEmpty() && !buildingInfo.isEmpty() && !apartmentInfo.isEmpty() && !floorInfo.isEmpty()) {
            return true;
        } else {
            if (streetInfo.isEmpty()) {
                street.setError("Enter valid street name");
            }
            if (buildingInfo.isEmpty()) {
                building.setError("Enter valid building number");
            }
            if (apartmentInfo.isEmpty()) {
                apartment.setError("Enter valid apartment number");
            }
            if (floorInfo.isEmpty()) {
                floor.setError("Enter valid floor number");
            }
            return false;
        }
    }

    // validate billing info form
    boolean validateBillingInfo() {
        // validate billing info

        String fullName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userPhone = "+2" + phone.getText().toString();

        if (!fullName.isEmpty() && !userEmail.isEmpty() && userPhone.length() >= 9) {
            return true;
        } else {
            if (fullName.isEmpty())
                name.setError("Enter valid Full Name");
            if (!isEmailValid(userEmail))
                email.setError("Enter valid Email");
            if (userPhone.isEmpty() || phone.length() < 9)
                phone.setError("Enter valid Phone Number");
            return false;
        }
    }

}