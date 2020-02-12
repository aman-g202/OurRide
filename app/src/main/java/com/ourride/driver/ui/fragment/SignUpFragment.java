package com.ourride.driver.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.ourride.driver.R;
import com.ourride.driver.apis.ApiConfig;
import com.ourride.driver.constant.Constant;
import com.ourride.driver.ui.activity.RiderequestActivity;
import com.ourride.driver.utils.Alerts;
import com.ourride.driver.utils.BaseFragment;
import com.ourride.driver.utils.ConnectionDirector;
import com.ourride.driver.utils.EmailChecker;
import com.ourride.driver.utils.SharedPrefrence;
import com.ourride.driver.volley.ApiRequest;
import com.ourride.driver.volley.IApiResponse;
import com.ourride.driver.volley.MultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.android.volley.VolleyLog.TAG;
import static com.ourride.driver.ui.activity.LoginActivity.loginfragmentManager;


public class SignUpFragment extends BaseFragment implements View.OnClickListener, IApiResponse, AdapterView.OnItemSelectedListener {
    private static final int LOAD_IMAGE_GALLERY = 123;
    private static int PICK_IMAGE_CAMERA = 124;
    private static int PERMISSION_REQUEST_CODE = 456;
    private static final int LOAD_IMAGE_GALLERY1 = 120;
    private static int PICK_IMAGE_CAMERA1 = 121;
    private static int PERMISSION_REQUEST_CODE1 = 455;
    private File finalFile = null;
    private String strMedicineImage;
    private View rootview;
    private Button btn_signUp;
    private TextView submitUser;
    private EditText userName,phone,password,rePassword,address,drivingLicense,workPhone,homePhone,email,homeAddress,make,model,
            year,color,issuesState,licenseExpiryDate,expirationDate,licensePlate, insuranceCarrier, dateOfBirthInput,
            issueDateInput, ssn, mileage, licenseClass, bankName, bankAddress, bankBranch, bankRouting, bankAccountNumber,
            holderName, state;
    private ImageView ivBack, ivPolicy,ivCarPhoto, licensebtn, expirationbtn, ivVoidedCheck, dateOfBirthbtn, issueDatebtn;
    private Spinner spinner1, spinner2;
    private CheckBox checkWorkedBefore;
    private ApiRequest apiRequest;
    private Bitmap policyPhoto = null;
    private Bitmap carPhoto = null;
    private Bitmap voidedCheck = null;
    private Bitmap insurancePhoto = null;
    private Bitmap licensePhoto = null;
    DatePickerDialog picker;
    private static final String[] paths1 = {"Background Check", "Y", "N"};
    private static final String[] paths2 = {"Saving", "Current"};
    private String backGroundCheck = "--", accountType = "--", workedBefore = "--";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_signup_layout, container, false);
        activity = getActivity();
        mContext = getActivity();
        cd = new ConnectionDirector(mContext);
        if (checkPermission()) {
            Alerts.show(mContext, "Permission granted");
        } else {
            requestPermission();
        }
        apiRequest= new ApiRequest(mContext, (IApiResponse) this);
        init();
        return rootview;
    }

    private void init() {
        submitUser = rootview.findViewById(R.id.submitUser);
        userName = rootview.findViewById(R.id.userNameInput);
        phone = rootview.findViewById(R.id.phoneInput);
        ssn = rootview.findViewById(R.id.ssnInput);
        password = rootview.findViewById(R.id.passwordInput);
        address = rootview.findViewById(R.id.addressInput);
        drivingLicense = rootview.findViewById(R.id.drivingLicenseInput);
        workPhone = rootview.findViewById(R.id.workPhoneInput);
        homePhone = rootview.findViewById(R.id.homePhoneInput);
        email = rootview.findViewById(R.id.emailInput);
        homeAddress = rootview.findViewById(R.id.homeAddressInput);
        make = rootview.findViewById(R.id.makeInput);
        model = rootview.findViewById(R.id.modelInput);
        color = rootview.findViewById(R.id.colorInput);
        mileage = rootview.findViewById(R.id.mileageInput);
        licenseClass = rootview.findViewById(R.id.licenseClassInput);
        state = rootview.findViewById(R.id.stateInput);
        issuesState = rootview.findViewById(R.id.issuedStateInput);
        year = rootview.findViewById(R.id.yearInput);
        expirationDate = rootview.findViewById(R.id.expirationDateInput);
        licenseExpiryDate = rootview.findViewById(R.id.licenseExpiryDateInput);
        dateOfBirthInput = rootview.findViewById(R.id.dateOfBirthInput);
        issueDateInput = rootview.findViewById(R.id.issueDateInput);
        rePassword = rootview.findViewById(R.id.rePasswordInput);
        expirationbtn = rootview.findViewById(R.id.expirationbtn);
        licensebtn = rootview.findViewById(R.id.licensebtn);
        dateOfBirthbtn = rootview.findViewById(R.id.dateOfBirthbtn);
        issueDatebtn = rootview.findViewById(R.id.issueDatebtn);
        licensePlate = rootview.findViewById(R.id.licensePlateInput);
        insuranceCarrier = rootview.findViewById(R.id.insuranceCarrierInput);
        spinner1 = rootview.findViewById(R.id.spinner1);
        spinner2 = rootview.findViewById(R.id.spinner2);
        checkWorkedBefore = rootview.findViewById(R.id.checkWorkedBefore);


        ivBack = rootview.findViewById(R.id.ivBack);
        ivPolicy = rootview.findViewById(R.id.ivPolicy);
        ivCarPhoto = rootview.findViewById(R.id.ivCarPhoto);
        ivVoidedCheck = rootview.findViewById(R.id.ivVoidedCheck);

        ivPolicy.setOnClickListener(this);
        ivCarPhoto.setOnClickListener(this);
        submitUser.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivVoidedCheck.setOnClickListener(this);

        bankName = rootview.findViewById(R.id.bankNameInput);
        bankAddress = rootview.findViewById(R.id.bankAddressInput);
        bankBranch = rootview.findViewById(R.id.bankBranchInput);
        bankRouting = rootview.findViewById(R.id.bankRoutingInput);
        bankAccountNumber = rootview.findViewById(R.id.bankAccountNumberInput);
        holderName = rootview.findViewById(R.id.holderNameInput);

        userName.addTextChangedListener(new MyTextWatcher(userName));
        phone.addTextChangedListener(new MyTextWatcher(phone));
        password.addTextChangedListener(new MyTextWatcher(password));
        address.addTextChangedListener(new MyTextWatcher(address));
        drivingLicense.addTextChangedListener(new MyTextWatcher(drivingLicense));
        workPhone.addTextChangedListener(new MyTextWatcher(workPhone));
        homePhone.addTextChangedListener(new MyTextWatcher(homePhone));
        email.addTextChangedListener(new MyTextWatcher(email));
        homeAddress.addTextChangedListener(new MyTextWatcher(homeAddress));
        make.addTextChangedListener(new MyTextWatcher(make));
        model.addTextChangedListener(new MyTextWatcher(model));
        year.addTextChangedListener(new MyTextWatcher(year));
        color.addTextChangedListener(new MyTextWatcher(color));
        issuesState.addTextChangedListener(new MyTextWatcher(issuesState));
        rePassword.addTextChangedListener(new MyTextWatcher(rePassword));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item,paths1);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item,paths2);

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter1);
        spinner2.setOnItemSelectedListener(this);

        checkWorkedBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkWorkedBefore.isChecked()){
                    workedBefore = "Yes";
                } else {
                    workedBefore = "No";
                }
            }
        });

        expirationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        expirationDate.setText(i + "-" + i1 + "-" + i2);
                    }
                }, year, month, day);
                picker.show();


            }
        });

        licensebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        licenseExpiryDate.setText(i + "-" + i1 + "-" + i2);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        dateOfBirthbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        dateOfBirthInput.setText(i + "-" + i1 + "-" + i2);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        issueDatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        issueDateInput.setText(i + "-" + i1 + "-" + i2);
                    }
                }, year, month, day);
                picker.show();
            }
        });

    }

    private void startFragment(String tag, Fragment fragment) {
        loginfragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.login_frame, fragment, tag).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitUser:
                submitUser();
                break;
            case R.id.ivBack:
                startFragment(Constant.SignIn, new LoginFragment());
                break;
            case R.id.ivPolicy:
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE_GALLERY);
                    } else {
                        selectImage2();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ivCarPhoto:
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE_GALLERY1);
                    } else {
                        selectImage3();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ivVoidedCheck:
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                    } else {
                        selectImage4();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOAD_IMAGE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.selectImage2();
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
            case LOAD_IMAGE_GALLERY1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.selectImage3();
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.selectImage4();
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Camera Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
        }
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void submitUser() {
        if (!validateUsername() || !validatePhone() || !validateEmail() || !validateExpirationDate() ||
              !validatelicenseExpiryDate() || !validatePassword() || !validateRePassword()) {
            return;
        }
        if (backGroundCheck.equals("Background Check")){
            Toast.makeText(mContext, "Select Background Check Y or N", Toast.LENGTH_LONG).show();
            return;
        }

//        Toast.makeText(mContext, "submit clicked", Toast.LENGTH_LONG).show();

        Map<String, String> paramsReq = new HashMap<>();
        paramsReq.put("name", userName.getText().toString().trim());
        paramsReq.put("phone", phone.getText().toString().trim());
        paramsReq.put("phone_work", workPhone.getText().toString().trim());
        paramsReq.put("phone_home", homePhone.getText().toString().trim());
        paramsReq.put("email", email.getText().toString().trim());
        paramsReq.put("work", address.getText().toString().trim());
        paramsReq.put("home", homeAddress.getText().toString().trim());
        paramsReq.put("dob", dateOfBirthInput.getText().toString().trim());
        paramsReq.put("ssn", ssn.getText().toString().trim());
        paramsReq.put("background_check", backGroundCheck);
        paramsReq.put("password", password.getText().toString().trim());
        paramsReq.put("re-password", rePassword.getText().toString().trim());
        paramsReq.put("worked_before", workedBefore);
        paramsReq.put("make", make.getText().toString().trim());
        paramsReq.put("model", model.getText().toString().trim());
        paramsReq.put("color", color.getText().toString().trim());
        paramsReq.put("driver_licence", drivingLicense.getText().toString().trim());
        paramsReq.put("issuance_state", issuesState.getText().toString().trim());
        if("".equals(year.getText().toString().trim())){
            paramsReq.put("year", "0000");
        }else{
            paramsReq.put("year", year.getText().toString().trim());
        }
        paramsReq.put("licence_expire_date", licenseExpiryDate.getText().toString().trim());
        paramsReq.put("expiration", expirationDate.getText().toString().trim());
        paramsReq.put("issue_date", issueDateInput.getText().toString().trim());
        paramsReq.put("user_group", "driver");
        paramsReq.put("licence_plate", licensePlate.getText().toString().trim());
        paramsReq.put("insurance_carrier", insuranceCarrier.getText().toString().trim());
        paramsReq.put("mileage", mileage.getText().toString().trim());
        paramsReq.put("licence_class", licenseClass.getText().toString().trim());
        paramsReq.put("state", state.getText().toString().trim());
        paramsReq.put("bank_name", bankName.getText().toString().trim());
        paramsReq.put("bank_address", bankAddress.getText().toString().trim());
        paramsReq.put("bank_branch", bankBranch.getText().toString().trim());
        paramsReq.put("bank_routing", bankRouting.getText().toString().trim());
        paramsReq.put("bank_account_no", bankAccountNumber.getText().toString().trim());
        paramsReq.put("bank_account_holder", holderName.getText().toString().trim());
        paramsReq.put("bank_account_type", accountType);

        Map<String, MultipartRequest.DataPart> paramsImage = new HashMap<>();

        if (policyPhoto != null){
            paramsImage.put("policy", new MultipartRequest.DataPart("policy.png", getFileDataFromDrawable(policyPhoto)));
        }
        if (carPhoto != null) {
            paramsImage.put("car_photo", new MultipartRequest.DataPart("car.png", getFileDataFromDrawable(carPhoto)));
        }
        if (voidedCheck != null){
            paramsImage.put("voided_check", new MultipartRequest.DataPart("voidedcheck.png", getFileDataFromDrawable(voidedCheck)));
        }

        apiRequest.postRequestWithImage(ApiConfig.baseUrl+ApiConfig.signUpDriver, "SignupUser" ,paramsReq,paramsImage, Request.Method.POST);


    }

    private void selectImage2() {
        try {
            PackageManager pm = mContext.getPackageManager();
            int permission = pm.checkPermission(Manifest.permission.CAMERA, mContext.getPackageName());
            if (permission == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] choose = {"Pick From Camera", "Choose From Gallery", "Cancel"};
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                builder.setTitle("Select Option");
                builder.setItems(choose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choose[which].equals("Pick From Camera")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 102);
                        } else if (choose[which].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, 103);
                        } else if (choose[which].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void selectImage3() {
        try {
            PackageManager pm = mContext.getPackageManager();
            int permission = pm.checkPermission(Manifest.permission.CAMERA, mContext.getPackageName());
            if (permission == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] choose = {"Pick From Camera", "Choose From Gallery", "Cancel"};
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                builder.setTitle("Select Option");
                builder.setItems(choose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choose[which].equals("Pick From Camera")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 104);
                        } else if (choose[which].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, 105);
                        } else if (choose[which].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void selectImage4() {
        try {
            PackageManager pm = mContext.getPackageManager();
            int permission = pm.checkPermission(Manifest.permission.CAMERA, mContext.getPackageName());
            if (permission == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] choose = {"Pick From Camera", "Choose From Gallery", "Cancel"};
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                builder.setTitle("Select Option");
                builder.setItems(choose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choose[which].equals("Pick From Camera")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 106);
                        } else if (choose[which].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, 107);
                        } else if (choose[which].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Alerts.show(mContext, "Permission granted");
            return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 4);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (mContext.getContentResolver() != null) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 102) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    int nh = (int) ( photo.getHeight() * (512.0 / photo.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                    policyPhoto = scaled;
                    ivPolicy.setImageBitmap(scaled);
                    Uri tempUri = getImageUri(mContext, photo);
                    finalFile = new File(getRealPathFromURI(tempUri));

                    //api hit

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 103 && resultCode == RESULT_OK && null != data) {
                final Uri uriImage = data.getData();
                final InputStream inputStream;
                try {
                    inputStream = mContext.getContentResolver().openInputStream(uriImage);
                    final Bitmap imageMap = BitmapFactory.decodeStream(inputStream);
                    int nh = (int) ( imageMap.getHeight() * (512.0 / imageMap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(imageMap, 512, nh, true);
                    policyPhoto = scaled;
                    ivPolicy.setImageBitmap(scaled);

                    String imagePath2 = getPath(uriImage);
                    File imageFile = new File(imagePath2);


                    //api hit
                } catch (FileNotFoundException e) {
                    Toast.makeText(mContext, "Image not found", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            else if (requestCode == 104) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    int nh = (int) ( photo.getHeight() * (512.0 / photo.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                    carPhoto = scaled;
                    ivCarPhoto.setImageBitmap(scaled);
                    Uri tempUri = getImageUri(mContext, photo);
                    finalFile = new File(getRealPathFromURI(tempUri));

                    //api hit

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 105 && resultCode == RESULT_OK && null != data) {
                final Uri uriImage = data.getData();
                final InputStream inputStream;
                try {
                    inputStream = mContext.getContentResolver().openInputStream(uriImage);
                    final Bitmap imageMap = BitmapFactory.decodeStream(inputStream);
                    int nh = (int) ( imageMap.getHeight() * (512.0 / imageMap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(imageMap, 512, nh, true);
                    carPhoto = scaled;
                    ivCarPhoto.setImageBitmap(scaled);

                    String imagePath2 = getPath(uriImage);
                    File imageFile = new File(imagePath2);


                    //api hit
                } catch (FileNotFoundException e) {
                    Toast.makeText(mContext, "Image not found", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            else if (requestCode == 106) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    int nh = (int) ( photo.getHeight() * (512.0 / photo.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                    voidedCheck = scaled;
                    ivVoidedCheck.setImageBitmap(scaled);
                    Uri tempUri = getImageUri(mContext, photo);
                    finalFile = new File(getRealPathFromURI(tempUri));

                    //api hit

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 107 && resultCode == RESULT_OK && null != data) {
                final Uri uriImage = data.getData();
                final InputStream inputStream;
                try {
                    inputStream = mContext.getContentResolver().openInputStream(uriImage);
                    final Bitmap imageMap = BitmapFactory.decodeStream(inputStream);
                    int nh = (int) ( imageMap.getHeight() * (512.0 / imageMap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(imageMap, 512, nh, true);
                    voidedCheck = scaled;
                    ivVoidedCheck.setImageBitmap(scaled);

                    String imagePath2 = getPath(uriImage);
                    File imageFile = new File(imagePath2);


                    //api hit
                } catch (FileNotFoundException e) {
                    Toast.makeText(mContext, "Image not found", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

                else {

            }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String strPath = cursor.getString(column_index);
        cursor.close();
        return strPath;
    }

    private boolean validatePassword() {
        if (password.getText().toString().trim().isEmpty()) {
            password.setError(getString(R.string.emptypassword));
            requestFocus(password);
            return false;
        } else {
            password.setError(null);
        }

        return true;
    }

    private boolean validateUsername() {
        if (userName.getText().toString().trim().isEmpty()) {
            userName.setError(getString(R.string.emptyusername));
            requestFocus(userName);
            return false;
        } else {
            userName.setError(null);
        }

        return true;
    }

    private boolean validatePhone() {
        if (phone.getText().toString().trim().isEmpty()) {
            phone.setError(getString(R.string.emptyphone));
            requestFocus(phone);
            return false;
        } else if (phone.getText().toString().trim().length() != 10){
            phone.setError(getString(R.string.errorphone));
            requestFocus(phone);
            return false;
        } else {
            phone.setError(null);
        }

        return true;
    }

    private boolean validateExpirationDate() {
        if (expirationDate.getText().toString().trim().isEmpty()) {
            expirationDate.setError(getString(R.string.emptyexpirationdate));
            requestFocus(expirationDate);
            return false;
        } else {
            expirationDate.setError(null);
        }

        return true;
    }

    private boolean validateWorkPhone(){

        if (workPhone.getText().toString().trim().isEmpty()) {
            workPhone.setError(getString(R.string.emptyphone));
            requestFocus(workPhone);
            return false;
        } else if (workPhone.getText().toString().trim().length() != 10){
            workPhone.setError(getString(R.string.errorphone));
            requestFocus(workPhone);
            return false;
        } else {
            workPhone.setError(null);
        }

        return true;

    }

    private boolean validateHomePhone(){

        if (homePhone.getText().toString().trim().isEmpty()) {
            homePhone.setError(getString(R.string.emptyphone));
            requestFocus(homePhone);
            return false;
        } else if (homePhone.getText().toString().trim().length() != 10){
            homePhone.setError(getString(R.string.errorphone));
            requestFocus(homePhone);
            return false;
        } else {
            homePhone.setError(null);
        }

        return true;

    }

    private boolean validatelicenseExpiryDate(){
        if (licenseExpiryDate.getText().toString().trim().isEmpty()) {
            licenseExpiryDate.setError(getString(R.string.enterfield));
            requestFocus(licenseExpiryDate);
            return false;
        } else {
            licenseExpiryDate.setError(null);
        }

        return true;

    }

    private boolean validateRePassword(){
        if (rePassword.getText().toString().trim().isEmpty()) {
            rePassword.setError(getString(R.string.enterfield));
            requestFocus(rePassword);
            return false;
        } else if (!rePassword.getText().toString().trim().equals(password.getText().toString().trim())){
            rePassword.setError("Password does'nt matched");
            requestFocus(rePassword);
            return false;
        } else {
            rePassword.setError(null);
        }

        return true;

    }

    private boolean validateEmail(){
        if (email.getText().toString().trim().isEmpty()) {
            email.setError(getString(R.string.enterfield));
            requestFocus(email);
            return false;
        } else if (!EmailChecker.isValid(email.getText().toString().trim())){
            email.setError("Enter correct email");
            requestFocus(email);
            return false;
        } else {
            email.setError(null);
        }

        return true;

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {

        try {
            if (!"".equals(tag_json_obj)) {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject responseObject = jsonObject.getJSONObject("response");
                if (responseObject.getString("status").equalsIgnoreCase("success")) {
                    Toast.makeText(mContext, "Registered Successfully", Toast.LENGTH_LONG).show();
                    SharedPrefrence.saveInt(mContext,"user_id",responseObject.getInt("user_id"));
                    startActivity(new Intent(mContext, RiderequestActivity.class));
                } else if (responseObject.getString("status").equals("error")){
                    JSONObject errorObject = responseObject.getJSONObject("message");
                    JSONObject emailObject = null;
                    JSONObject phoneObject = null;
                    if(errorObject.has("email")){
                        emailObject = errorObject.getJSONObject("email");
                    }
                    if(errorObject.has("phone")){
                        phoneObject = errorObject.getJSONObject("phone");
                    }

                    if (emailObject != null){
                        Toast.makeText(mContext, emailObject.getString("unique"), Toast.LENGTH_SHORT).show();
                    }else if (phoneObject != null){
                        Toast.makeText(mContext, phoneObject.getString("unique"), Toast.LENGTH_SHORT).show();
                    }else{
                        String jsonString = responseObject.getJSONObject("message").toString();
                        StringBuffer stringBuffer = new StringBuffer();
                        JSONObject resobj = new JSONObject(jsonString);
                        Iterator<?> keys = resobj.keys();
                        while(keys.hasNext() ) {
                            String key = (String)keys.next();
                            if ( resobj.get(key) instanceof JSONObject ) {
                                JSONObject xx = new JSONObject(resobj.get(key).toString());
                                Iterator<?> innerkeys = xx.keys();
                                if (innerkeys.hasNext()){
                                    String innerkey = (String)innerkeys.next();
                                    stringBuffer.append(key +" - "+xx.getString(innerkey)+"\n");
                                }
                            }
                        }
                        Toast.makeText(mContext, stringBuffer, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                        Toast.makeText(mContext, "Oops, some error occured!", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("aman", "onResultReceived: " + error.getMessage());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getItemAtPosition(position).toString().equals("Saving")){
            accountType = parent.getItemAtPosition(position).toString();
        } else if (parent.getItemAtPosition(position).toString().equals("Current")){
            accountType = parent.getItemAtPosition(position).toString();
        } else {
            backGroundCheck = parent.getItemAtPosition(position).toString();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()){
                case R.id.phoneInput:
                    validatePhone();
                    break;
                case R.id.emailInput:
                    validateEmail();
                    break;
                case R.id.rePasswordInput:
                    validateRePassword();
                    break;
                case R.id.workPhoneInput:
                    validateWorkPhone();
                    break;
                case R.id.homePhoneInput:
                    validateHomePhone();
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.passwordInput:
                    validatePassword();
                    break;
                case R.id.userNameInput:
                    validateUsername();
                    break;
                case R.id.phoneInput:
                    validatePhone();
                    break;
                case R.id.workPhoneInput:
                    validateWorkPhone();
                    break;
                case R.id.homePhoneInput:
                    validateHomePhone();
                    break;
                case R.id.rePasswordInput:
                    validateRePassword();
                    break;
                case R.id.emailInput:
                    validateEmail();
                    break;
//                case R.id.addressInput:
//                    validateAddress();
//                    break;
//                case R.id.drivingLicenseInput:
//                    validateDrivingLicense();
//                    break;
//                case R.id.makeInput:
//                    validatemake();
//                    break;
//                case R.id.modelInput:
//                    validateModel();
//                    break;
//                case R.id.colorInput:
//                    validateColor();
//                    break;
//                case R.id.yearInput:
//                    validateYear();
//                    break;
//                case R.id.homeAddressInput:
//                    validateHomeAddress();
//                    break;
//                case R.id.issuedStateInput:
//                    validateIssuedState();
//                    break;
//                case R.id.licensePlateInput:
//                    validateLicensePlate();
//                    break;
//                case R.id.insuranceCarrierInput:
//                    validateInsuranceCarrier();
//                    break;
            }
        }
    }
}