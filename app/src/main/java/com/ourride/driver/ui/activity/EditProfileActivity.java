package com.ourride.driver.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ourride.driver.R;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.utils.Alerts;
import com.ourride.driver.utils.BaseActivity;
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
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.volley.VolleyLog.TAG;

public class EditProfileActivity extends BaseActivity implements IApiResponse {
    private static final int LOAD_IMAGE_GALLERY = 123;
    private static int PICK_IMAGE_CAMERA = 124;
    private static final int PERMISSION_REQUEST_CODE = 456;
    private File finalFile = null;
    private ImageView ivBack;
    private TextView emailEdit,phoneEdit;
    private EditText nameEdit;
    private Button btnUpdate;
    private String name;
    private String email;
    private CircleImageView imgProfile;
    private String phone;
    private int user_id;
    private ApiRequest apiRequest;
    private Context mContext;
    private Bitmap profilePhoto = null;
    private String userImageUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mContext = EditProfileActivity.this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
        userImageUrl = intent.getStringExtra("user_image_url");

        nameEdit = findViewById(R.id.nameEditInput);
        emailEdit = findViewById(R.id.emailEditInput);
        phoneEdit = findViewById(R.id.phoneEditInput);
        btnUpdate = findViewById(R.id.buttonUpdate);

        if (checkPermission()) {
//            Alerts.show(mContext, "Permission granted");
        } else {
            requestPermission();
        }

        apiRequest = new ApiRequest(mContext, (IApiResponse)this);

        nameEdit.setText(name);
        emailEdit.setText(email);
        phoneEdit.setText(phone);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedPrefrence.getInt(mContext,"user_id") != 0){
                    user_id = SharedPrefrence.getInt(mContext,"user_id");

                    Map<String, String> paramsReq = new HashMap<>();
                    paramsReq.put("name", nameEdit.getText().toString().trim());
                    Map<String, MultipartRequest.DataPart> paramsImage = new HashMap<>();
                    if (profilePhoto != null){
                        paramsImage.put("profile_image", new MultipartRequest.DataPart("profile_image.png", getFileDataFromDrawable(profilePhoto)));
                    }
                    apiRequest.postRequestWithImage("http://3.18.88.39/api/users/edit/"+user_id+".json", "UpdateUser", paramsReq, paramsImage,Request.Method.POST);

                }else{
                    Toast.makeText(mContext, "Oops, some error occured contact admin", Toast.LENGTH_LONG).show();
                }
            }
        });


        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void init() {
        selectProfile();
    }

    private void selectProfile() {
        imgProfile = findViewById(R.id.imgProfile);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_user_profile)
                .error(R.drawable.ic_user_profile);

        Glide.with(EditProfileActivity.this).load(userImageUrl).apply(options).into(imgProfile);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE_GALLERY);
                    } else {
                        selectImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOAD_IMAGE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Alerts.show(mContext, "Camera Permission granted");
                } else {
                    Alerts.show(mContext, "Please give permission");
                }
                break;
        }
    }


    private void selectImage() {
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
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (choose[which].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, LOAD_IMAGE_GALLERY);
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
           // Alerts.show(mContext, "Permission not granted");
            return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
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
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                int nh = (int) ( photo.getHeight() * (512.0 / photo.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                profilePhoto = scaled;
                imgProfile.setImageBitmap(scaled);
                Uri tempUri = getImageUri(mContext, photo);
                finalFile = new File(getRealPathFromURI(tempUri));
                //api hit

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == LOAD_IMAGE_GALLERY && resultCode == RESULT_OK && null != data) {
            final Uri uriImage = data.getData();
            final InputStream inputStream;
            try {
                inputStream = mContext.getContentResolver().openInputStream(uriImage);
                final Bitmap imageMap = BitmapFactory.decodeStream(inputStream);
                int nh = (int) ( imageMap.getHeight() * (512.0 / imageMap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(imageMap, 512, nh, true);
                profilePhoto = scaled;
                imgProfile.setImageBitmap(scaled);

                String imagePath2 = getPath(uriImage);
                File imageFile = new File(imagePath2);


                //api hit
            } catch (FileNotFoundException e) {
                Toast.makeText(mContext, "Image not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {

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

    @Override
    public void onResultReceived(String response, String tag_json_obj) throws JSONException {
        try {
            if (tag_json_obj.equals("UpdateUser")) {
//                Toast.makeText(mContext, "hey", Toast.LENGTH_SHORT).show();
                JSONObject jsonObject = new JSONObject(response);
                JSONObject jsonObject1 = jsonObject.getJSONObject("response");

                if (jsonObject1.has("message")) {
                    if (jsonObject1.getString("status").equals("success")){
                        Toast.makeText(mContext, "Updated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(mContext, MainHomeActivity.class);
                        startActivity(intent);

                    }else {
                        Toast.makeText(mContext, "Some error while updating", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            Log.d("aman", e.getMessage());
        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
    }
}
