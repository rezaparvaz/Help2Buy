package com.einkaufsheld.help2buy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneNumberVerificationActivity extends AppCompatActivity {

    //declare object
    LinearLayout mode0LinearLayout, mode1LinearLayout;
    EditText phoneNumberEditText, codeEditText;
    TextView textViewRemainingTime;
    FirebaseAuth mAuth;
    int mode = 0;
    String codeSent;
    CountryCodePicker countryCodePicker;
    Button getCodeButton, resendCodeButton, confirmButton;
    private int counter = 60;
    public String phoneNumber = "";
    String countryCode;
    CountDownTimer countDownTimer;
    String phoneNumberWithoutCountryCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verification);

        mAuth = FirebaseAuth.getInstance();
        mode0LinearLayout = findViewById(R.id.phone_number_verification_mode0_linear_layout);
        mode1LinearLayout = findViewById(R.id.phone_number_verification_mode1_linear_layout);
        phoneNumberEditText = findViewById(R.id.phone_number_verification_phone_number_edit_text);
        codeEditText = findViewById(R.id.phone_number_verification_verification_code_edit_text);
        textViewRemainingTime = findViewById(R.id.phone_number_verification_remaining_time);
        getCodeButton = findViewById(R.id.phone_number_get_code_button);
        resendCodeButton = findViewById(R.id.phone_number_verification_resend_code_button);
        confirmButton = findViewById(R.id.phone_number_verification_confirm_button);
        countryCodePicker = findViewById(R.id.phone_number_verification_ccp);
        countryCodePicker.setAutoDetectedCountry(true);
        linearLayoutsVisibilityCheck();
        getCodeButton.setOnClickListener(view -> {

            sendVerificationCode(); //call method
            startCountDown();

        });

        confirmButton.setOnClickListener(view -> {

            verifySignInCode();
        });

        resendCodeButton.setOnClickListener(view -> {
            mode = 0;
            linearLayoutsVisibilityCheck();
            countryCodePicker.setCountryForPhoneCode(Integer.parseInt(countryCode));
            phoneNumberEditText.setText(phoneNumberWithoutCountryCode);
        });
    }


    //for verify code
    private void verifySignInCode() {
        String code = codeEditText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        LinkBothCredentials(credential); //call for check code
    }


    private void LinkBothCredentials(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "linkWithCredential:success");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(user.getUid()).update("PhoneNumber", user.getPhoneNumber());

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.w("TAG", "linkWithCredential:failure", task.getException());

                    }

                    // ...
                });
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        //here you can open new activity
//                        Toast.makeText(getApplicationContext(), "Verification Successful", Toast.LENGTH_LONG).show();
//                        textViewRemainingTime.setText("SMS Verification Successful");
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(intent);
//                    } else {
//                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                            Toast.makeText(getApplicationContext(),
//                                    "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
    }


    //for getting verification code
    private void sendVerificationCode() {

        countryCode = countryCodePicker.getSelectedCountryCode();
        phoneNumberWithoutCountryCode = phoneNumberEditText.getText().toString();
        StringBuilder phoneNumberBuilder = new StringBuilder().append("+").append(countryCode).append(phoneNumberWithoutCountryCode);
        phoneNumber = phoneNumberBuilder.toString();
        if (phoneNumber.isEmpty()) {
            phoneNumberEditText.setError("Phone number is required");
            phoneNumberEditText.requestFocus();
            return;
        }

        if (phoneNumber.length() < 10) {
            phoneNumberEditText.setError("Please enter a valid phone");
            phoneNumberEditText.requestFocus();
            return;
        }

        mode = 1;
        Log.d("TAG", "KOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOS: "+phoneNumber);
        linearLayoutsVisibilityCheck();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };


    public void startCountDown() {

        counter = 60;
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
        //60_000=60 sec or 1 min and another is interval of count down is 1 sec
        countDownTimer = new CountDownTimer(60_000, 1_000) {
            @Override
            public void onTick(long l) {

                textViewRemainingTime.setText("Remaining Time : " + String.valueOf(counter) + " Seconds");
                textViewRemainingTime.setTextColor(Color.BLACK);
                counter--;

            }

            @Override
            public void onFinish() {

                textViewRemainingTime.setText("The code is expired");
                textViewRemainingTime.setTextColor(Color.RED);
            }
        }.start();
    }

    public void linearLayoutsVisibilityCheck() {
        if (mode == 0) {
            mode0LinearLayout.setVisibility(View.VISIBLE);
            mode1LinearLayout.setVisibility(View.GONE);
        } else if (mode == 1) {
            mode0LinearLayout.setVisibility(View.GONE);
            mode1LinearLayout.setVisibility(View.VISIBLE);

        }
    }

}