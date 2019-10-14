package com.haltechbd.app.android.qpay;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.haltechbd.app.android.qpay.utils.EncryptionDecryption;
import com.haltechbd.app.android.qpay.utils.GlobalData;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")

public class MP_Through_C_C2M_FromFav extends AppCompatActivity implements OnClickListener {
    private String SOAP_ACTION, METHOD_NAME;
    EncryptionDecryption encryptionDecryption = new EncryptionDecryption();
    private ProgressDialog mProgressDialog = null;

    //#############################################################
    private SharedPreferences mSharedPreferencsOtp;
    private SharedPreferences.Editor mSharedPreferencsOtpEditor;
    //#############################################################

    private EditText mEditTextCaption, mEditTextSourceWallet, mEditTextDestinationWallet,
            mEditTextDestinationAccountName, mEditTextAmount, mEditTextSourceOtp,
            mEditTextCustomerReference;
    private Button mBtnSubmit;
    private TextView mTextViewShowServerResponse;
    private String mStrEncryptCaption, mStrEncryptSourceWallet, mStrEncryptSourcePin,
            mStrEncryptSourceOtp, mStrEncryptAmount, mStrEncryptDestinationWallet,
            mStrEncryptDestinationAccountName, mStrEncryptCustomerReference, mStrEncryptFunctionType,
            mStrServerResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_by_customer_from_fav);
        checkOs();
        initUI();
    }

    private void checkOs() {
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initUI() {
        mEditTextCaption = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavCaption);
        mEditTextCaption.setText(GlobalData.getStrFavCaption());
        mEditTextSourceWallet = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavSourceWallet);
        mEditTextSourceWallet.setText(GlobalData.getStrFavSourceWallet());
        mEditTextDestinationWallet = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavDestinationWallet);
        mEditTextDestinationWallet.setText(GlobalData.getStrFavDestinationWallet());
        mEditTextDestinationAccountName = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavDestinationName);
        mEditTextDestinationAccountName.setText(GlobalData.getStrFavDestinationWalletAccountHolderName());
        mEditTextAmount = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavAmount);
        mEditTextAmount.setText(GlobalData.getStrFavAmount());
        mEditTextAmount.requestFocus();
        mEditTextSourceOtp = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavSourceOtp);
        mEditTextCustomerReference = findViewById(R.id.editTextMerchantPaymentByCustomerFromFavCustomerReference);
        mEditTextCustomerReference.setText(GlobalData.getStrFavReference());
        mEditTextCustomerReference.requestFocus();
        mBtnSubmit = findViewById(R.id.btnMerchantPaymentByCustomerFromFavSubmit);
        mBtnSubmit.setOnClickListener(this);
        mTextViewShowServerResponse = findViewById(R.id.textViewMerchantPaymentByCustomerFromFavServerResponse);

        //################### set OTP #########################
        //################### set OTP #########################
        //################### set OTP #########################
        mSharedPreferencsOtp = getSharedPreferences("otpPrefs", MODE_PRIVATE);
        mSharedPreferencsOtpEditor = mSharedPreferencsOtp.edit();
        mEditTextSourceOtp.setText(mSharedPreferencsOtp.getString("generate_otp", ""));
        //#######################################################################################################

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        checkInternet();
    }


    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            try {
                mSharedPreferencsOtp = getSharedPreferences("otpPrefs", MODE_PRIVATE);
                mSharedPreferencsOtpEditor = mSharedPreferencsOtp.edit();
                String strExpireTime = mSharedPreferencsOtp.getString("otp_expire_time", "");
                String strOtp = mSharedPreferencsOtp.getString("generate_otp", "");

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                // Current Time
                Date currentTime = Calendar.getInstance().getTime();
                String strCurrentTime = df.format(currentTime);
                // Expire Time
                Date timeCurrent = df.parse(strCurrentTime);
                Date timeExpire = df.parse(strExpireTime);

                if (timeCurrent.before(timeExpire)) {
                    //########################
                    if (mEditTextCaption.getText().toString().length() == 0) {
                        mEditTextCaption.setError("Field cannot be empty");
                    } else if (mEditTextSourceWallet.getText().toString().length() == 5) {
                        mEditTextSourceWallet.setError("Field cannot be empty");
                    } else if (mEditTextDestinationWallet.getText().toString().length() == 0) {
                        mEditTextDestinationWallet.setError("Field cannot be empty");
                    } else if (mEditTextDestinationAccountName.getText().toString().length() == 0) {
                        mEditTextDestinationAccountName.setError("Field cannot be empty");
                    } else if (mEditTextAmount.getText().toString().length() == 0) {
                        mEditTextAmount.setError("Field cannot be empty");
                    } else if (mEditTextSourceOtp.getText().toString().length() == 0) {
                        mEditTextSourceOtp.setError("Field cannot be empty");
                    } else {

                        try {
                            mStrEncryptCaption = encryptionDecryption.Encrypt(mEditTextCaption.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptSourceWallet = encryptionDecryption.Encrypt(mEditTextSourceWallet.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptDestinationWallet = encryptionDecryption.Encrypt(mEditTextDestinationWallet.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptDestinationAccountName = encryptionDecryption.Encrypt(mEditTextDestinationAccountName.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptAmount = encryptionDecryption.Encrypt(mEditTextAmount.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptSourceOtp = encryptionDecryption.Encrypt(mEditTextSourceOtp.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptCustomerReference = encryptionDecryption.Encrypt(mEditTextCustomerReference.getText().toString(), GlobalData.getStrSessionId());
                            mStrEncryptFunctionType = encryptionDecryption.Decrypt(GlobalData.getStrFavFunctionType(), GlobalData.getStrSessionId());
                            mStrEncryptSourcePin = encryptionDecryption.Encrypt(GlobalData.getStrPin(), GlobalData.getStrSessionId());

                            // Initialize progress dialog
                            mProgressDialog = ProgressDialog.show(MP_Through_C_C2M_FromFav.this, null, "Processing request...", false, true);
                            // Cancel progress dialog on back key press
                            mProgressDialog.setCancelable(true);

                            Thread t = new Thread(new Runnable() {

                                @Override
                                public void run() {
//                    if (CheckMasterKeyAndSessionId.checkMasterKeyAndSessionId() == true) {
                                    doMakePayment(
                                            mStrEncryptSourceWallet,
                                            mStrEncryptSourcePin,
                                            mStrEncryptDestinationWallet,
                                            mStrEncryptAmount,
                                            mStrEncryptSourceOtp,
                                            mStrEncryptCustomerReference);
//                    } else {
//                        mTextViewShowServerResponse.setText("Session Expire, Please Login Again");
//                    }
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                    mProgressDialog.dismiss();
                                                    //####################### Show Dialog ####################
                                                    //####################### Show Dialog ####################
                                                    //####################### Show Dialog ####################
                                                    int intIndex = mStrServerResponse.indexOf("successful");
                                                    if (intIndex == -1) {
                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(MP_Through_C_C2M_FromFav.this);
                                                        if(mStrServerResponse.equalsIgnoreCase(""))
                                                        {
                                                            mStrServerResponse="Request is in Process";
                                                        }
                                                        myAlert.setMessage(mStrServerResponse);
                                                        myAlert.setNegativeButton(
                                                                "Close",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
//                                                            enableUiComponentAfterClick();
                                                                        startActivity(new Intent(MP_Through_C_C2M_FromFav.this, MainActivity.class));
                                                                        finish();
                                                                    }
                                                                });
                                                        AlertDialog alertDialog = myAlert.create();
                                                        alertDialog.show();
                                                    } else {
                                                        AlertDialog.Builder myAlert = new AlertDialog.Builder(MP_Through_C_C2M_FromFav.this);
                                                        myAlert.setMessage(mStrServerResponse);
                                                        myAlert.setNeutralButton(
                                                                "Continue",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
//                                                            enableUiComponentAfterClick();
//                                                                        clearEditText();
                                                                    }
                                                                });
                                                        myAlert.setNegativeButton(
                                                                "Close",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
//                                                            enableUiComponentAfterClick();
                                                                        startActivity(new Intent(MP_Through_C_C2M_FromFav.this, MainActivity.class));
                                                                        finish();
                                                                    }
                                                                });
                                                        AlertDialog alertDialog = myAlert.create();
                                                        alertDialog.show();
                                                    }

                                                }

                                            } catch (Exception e) {
                                                // TODO: handle exception
                                            }
//                                            try {
//                                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                                                    mProgressDialog.dismiss();
//                                                    //####################### Show Dialog ####################
//                                                    //####################### Show Dialog ####################
//                                                    //####################### Show Dialog ####################
//                                                    AlertDialog.Builder myAlert = new AlertDialog.Builder(MP_Through_C_C2M_FromFav.this);
//                                                    myAlert.setMessage(mStrServerResponse);
//                                                    myAlert.setNegativeButton(
//                                                            "OK",
//                                                            new DialogInterface.OnClickListener() {
//                                                                public void onClick(DialogInterface dialog, int id) {
//                                                                    startActivity(new Intent(MP_Through_C_C2M_FromFav.this, QPayMenuNew.class));
//                                                                    dialog.cancel();
//                                                                }
//                                                            });
//                                                    AlertDialog alertDialog = myAlert.create();
//                                                    alertDialog.show();
//
//                                                }
//
//                                            } catch (Exception e) {
//                                                // TODO: handle exception
//                                            }
                                            // update ui info ( show response message )
                                        }
                                    });
                                }
                            });

                            t.start();


                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }


                    }

                    //#######################################
                } else {
                    // if expire
                    AlertDialog.Builder myAlert = new AlertDialog.Builder(MP_Through_C_C2M_FromFav.this);
                    myAlert.setMessage("OTP is expired. Generate a new OTP?");
                    myAlert.setPositiveButton(
                            "Generate OTP",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    startActivity(new Intent(MP_Through_C_C2M_FromFav.this, GenerateOtp.class));
                                    finish();
                                }
                            });
                    myAlert.setNegativeButton(
                            "Close",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    startActivity(new Intent(MP_Through_C_C2M_FromFav.this, MainActivity.class));
                                    finish();
                                }
                            });
                    AlertDialog alertDialog = myAlert.create();
                    alertDialog.show();
                }
                //####
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }



    public void doMakePayment(String strEncryptAccountNumber,
                              String strEncryptPin,
                              String strEncryptMerchantAccountNumber,
                              String strEncryptAmount,
                              String strEncryptOtp,
                              String strEncryptReference) {
        METHOD_NAME = "QPAY_Merchant_Payment_Customer";
        SOAP_ACTION = "http://www.bdmitech.com/m2b/QPAY_Merchant_Payment_Customer";
        SoapObject request = new SoapObject(GlobalData.getStrNamespace(), METHOD_NAME);
        // Declare the version of the SOAP request
        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        //QPAY_Merchant_Payment_Customer
        // CustomerAccount
        // CustomerPIN
        // MerchantAccountno
        // Amount
        // CustomerOTP
        // CustomerRefID
        // strMasterKey
        PropertyInfo customerAccountNumber = new PropertyInfo();
        customerAccountNumber.setName("E_CustomerAccount");
        customerAccountNumber.setValue(strEncryptAccountNumber);
        customerAccountNumber.setType(String.class);
        request.addProperty(customerAccountNumber);

        PropertyInfo customerPin = new PropertyInfo();
        customerPin.setName("E_CustomerPIN");
        customerPin.setValue(strEncryptPin);
        customerPin.setType(String.class);
        request.addProperty(customerPin);

        PropertyInfo merchantAccountNumber = new PropertyInfo();
        merchantAccountNumber.setName("E_MerchantAccountno");
        merchantAccountNumber.setValue(strEncryptMerchantAccountNumber);
        merchantAccountNumber.setType(String.class);
        request.addProperty(merchantAccountNumber);

        PropertyInfo amount = new PropertyInfo();
        amount.setName("E_Amount");
        amount.setValue(strEncryptAmount);
        amount.setType(String.class);
        request.addProperty(amount);

        PropertyInfo otp = new PropertyInfo();
        otp.setName("E_CustomerOTP");
        otp.setValue(strEncryptOtp);
        otp.setType(String.class);
        request.addProperty(otp);

        PropertyInfo reference = new PropertyInfo();
        reference.setName("E_CustomerRefID");
        reference.setValue(strEncryptReference);
        reference.setType(String.class);
        request.addProperty(reference);

        PropertyInfo encryptMasterKey = new PropertyInfo();
        encryptMasterKey.setName("UserID");
        encryptMasterKey.setValue(GlobalData.getStrUserId());
        encryptMasterKey.setType(String.class);
        request.addProperty(encryptMasterKey);


        PropertyInfo masterKey = new PropertyInfo();
        masterKey.setName("DeviceID");
        masterKey.setValue(GlobalData.getStrDeviceId());
        masterKey.setType(String.class);
        request.addProperty(masterKey);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);
        Log.v("myApp:", request.toString());
        envelope.implicitTypes = true;
        Object objMakePayment = null;
        String strMakePaymentResponse = "";

        try {
            HttpTransportSE androidHttpTransport = new HttpTransportSE(GlobalData.getStrUrl(), 1000);
            androidHttpTransport.call(SOAP_ACTION, envelope);
            objMakePayment = envelope.getResponse();
            strMakePaymentResponse = objMakePayment.toString();
            mStrServerResponse = strMakePaymentResponse;

            //            Merchant Payment successful on 20-Mar-18 04:06:03Sent to: 00611000000561 Amount Tk.5,320.00
//            Balance: Tk.92,480.00
//            TXN ID: 18032000000086
//            QPAY//Merchant Payment Received From: 00612000000532 on 20-Mar-18 04:06:03
//            Amount: Tk.5,320.00
//            Balance: Tk.7,520.00
//            TXN ID: 18032000000086
//            QPAY,*AP180320160903

//            Merchant are not allow for this transactionQPAY,*AP180320160638

//            The customer OTP is wrong please insert correct OTP.,*AP180320161231

            int intIndex = mStrServerResponse.indexOf("successful");
            if (intIndex == -1) {
                int intIndex1 = mStrServerResponse.indexOf("not allow");
                if (intIndex1 == -1) {

                } else {
                    String[] parts = mStrServerResponse.split(",");
                    String strResponse = parts[0];
                    String strExtra = parts[1];
                    mStrServerResponse = strResponse;
                }
                int intIndex2 = mStrServerResponse.indexOf("wrong");
                if (intIndex2 == -1) {

                } else {
                    String[] parts = mStrServerResponse.split(",");
                    String strResponse = parts[0];
                    String strExtra = parts[1];
                    mStrServerResponse = strResponse;
                }
            } else {
                String[] parts = mStrServerResponse.split("//");
                String strResponse = parts[0];//
                String strExtra = parts[1];
                mStrServerResponse = strResponse;
            }
        } catch (Exception exception) {
            mStrServerResponse = strMakePaymentResponse;
        }
    }


    private void checkInternet() {

        if (isNetworkConnected()) {
            enableUiComponents();
        } else {
            disableUiComponents();
            AlertDialog.Builder mAlertDialogBuilder = new AlertDialog.Builder(MP_Through_C_C2M_FromFav.this);
            mAlertDialogBuilder.setTitle("No Internet Connection");
            mAlertDialogBuilder.setMessage("It looks like your internet connection is off. Please turn it on and try again.");
            mAlertDialogBuilder.setNegativeButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog mAlertDialog = mAlertDialogBuilder.create();
            mAlertDialog.show();
        }
    }

    private void enableUiComponents() {
        mEditTextCaption.setEnabled(false);
        mEditTextSourceWallet.setEnabled(false);
        mEditTextDestinationWallet.setEnabled(false);
        mEditTextDestinationAccountName.setEnabled(false);
        mEditTextAmount.setEnabled(true);
        mEditTextSourceOtp.setEnabled(true);
        mEditTextCustomerReference.setEnabled(true);
        mBtnSubmit.setEnabled(true);
        mTextViewShowServerResponse.setEnabled(true);
    }

    private void disableUiComponents() {
        mEditTextCaption.setEnabled(false);
        mEditTextSourceWallet.setEnabled(false);
        mEditTextDestinationWallet.setEnabled(false);
        mEditTextDestinationAccountName.setEnabled(false);
        mEditTextAmount.setEnabled(false);
        mEditTextSourceOtp.setEnabled(false);
        mEditTextCustomerReference.setEnabled(false);
        mBtnSubmit.setEnabled(false);
        mTextViewShowServerResponse.setEnabled(false);
    }

    //########################## Logout ############################
    //########################## Logout ############################
    //########################## Logout ############################
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        MenuItem pinMenuItem = menu.findItem(R.id.actionRewardpoint);
//        pinMenuItem.setTitle("Reward Point:678");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(MP_Through_C_C2M_FromFav.this, MainActivity.class));
                finish();
                return true;
            case R.id.actionRewardpoint:
//                clearDataFromGlobal();
//                Intent intent = new Intent(MP_Through_C_C2M_FromFav.this, Login.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                finish();
//                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearDataFromGlobal() {
        GlobalData.setStrDeviceId("");
        GlobalData.setStrDeviceName("");
        GlobalData.setStrUserId("");
        GlobalData.setStrEncryptUserId("");
        GlobalData.setStrPin("");
        GlobalData.setStrEncryptPin("");
        GlobalData.setStrMasterKey("");
        GlobalData.setStrPackage("");
        GlobalData.setStrEncryptPackage("");
        GlobalData.setStrAccountNumber("");
      //  GlobalData.setStrEncryptAccountNumber("");
        GlobalData.setStrWallet("");
        GlobalData.setStrAccountHolderName("");
        GlobalData.setStrSessionId("");
        GlobalData.setStrQrCodeContent("");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(MP_Through_C_C2M_FromFav.this, MainActivity.class));
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
