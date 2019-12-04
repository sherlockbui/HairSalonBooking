package com.example.hairsalonbooking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hairsalonbooking.Common.Common;
import com.example.hairsalonbooking.Common.MySocket;
import com.example.hairsalonbooking.Fragments.HomeFragment;
import com.example.hairsalonbooking.Fragments.ShopingFragment;
import com.example.hairsalonbooking.Model.MyToken;
import com.example.hairsalonbooking.Model.User;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    BottomSheetDialog bottomSheetDialog;
    FirebaseUser firebaseUser = null;
    private boolean isFirstBackPressed = false;
    private Socket mSocket = MySocket.getmSocket();
    private Emitter.Listener onCheckEmail = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Handler handler = new Handler(Looper.getMainLooper());
            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                try {
                    Common.currentUser = new User();
                    Common.currentUser.setEmail(object.getString("email"));
                    Common.currentUser.setFullName(object.getString("name"));
                    Common.currentUser.setAddress(object.getString("adress"));
                    Common.currentUser.setPhoneNumber(object.getString("phone"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Common.currentUser != null) {
                                Log.d("AAA", "run: " + Common.currentUser.getFullName());
                                initControl();
                            }
                        }
                    });
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        return;
                                    } else {
                                        String token = task.getResult().getToken();
                                        MyToken myToken = new MyToken();
                                        myToken.setToken(token);
                                        myToken.setPhoneCustomer(Common.currentUser.getPhoneNumber());
                                        String jsonToken = new Gson().toJson(myToken);
                                        mSocket.emit("updateToken", jsonToken);
                                        Log.d("token", "onComplete: " + token);
                                    }
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showUpdateDialog();
                    }
                });

            }

        }
    };
    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Handler handler = new Handler(Looper.getMainLooper());
            JSONObject object = (JSONObject) args[0];
            if (object != null) {
                try {
                    Common.currentUser = new User();
                    Common.currentUser.setEmail(object.getString("email"));
                    Common.currentUser.setFullName(object.getString("name"));
                    Common.currentUser.setAddress(object.getString("adress"));
                    Common.currentUser.setPhoneNumber(object.getString("phone"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Common.currentUser != null) {
                                bottomSheetDialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Thành Công", Toast.LENGTH_SHORT).show();
                                initControl();
                            }

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mSocket.connect();
        mSocket.on("register", onRegister);
        mSocket.on("checkemail", onCheckEmail);
        checkLogin();
        initView();
    }

    @Override
    public void onBackPressed() {
        if (isFirstBackPressed) {
            super.onBackPressed();
        } else {
            isFirstBackPressed = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFirstBackPressed = false;
                }
            }, 1500);
        }

    }

    private void checkLogin() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getIntent() != null) {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if (isLogin) {
                if (firebaseUser.getPhoneNumber() != null && firebaseUser.getPhoneNumber() != "") {
                    Log.d("AAA", "checkLogin: " + firebaseUser.getPhoneNumber());
                    mSocket.emit("checkemail", firebaseUser.getEmail(), "0" + firebaseUser.getPhoneNumber().substring(3));
                } else {
                    mSocket.emit("checkemail", firebaseUser.getEmail(), "");
                }
            }
        }
    }


    private void initControl() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_home) {
                    fragment = new HomeFragment();
                } else if (item.getItemId() == R.id.action_shoping) {
                    fragment = new ShopingFragment();
                }
                return loadFragment(fragment);
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    private void initView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void showUpdateDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("Vui Lòng Cung Cấp Thêm Thông Tin!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_infomation, null);
        Button btn_update = sheetView.findViewById(R.id.btn_Update);
        final TextInputEditText edt_name = sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_phoneNumber = sheetView.findViewById(R.id.edt_phoneNumber);
        final TextInputEditText edt_adress = sheetView.findViewById(R.id.edt_adress);
        final TextInputEditText edt_email = sheetView.findViewById(R.id.edt_email);
        if (firebaseUser.getEmail() != "" && firebaseUser.getEmail() != null) {
            edt_email.setText(firebaseUser.getEmail());
            edt_email.setEnabled(false);
        }
        if (firebaseUser.getPhoneNumber() != "" && firebaseUser.getPhoneNumber() != null) {
            edt_phoneNumber.setText("0" + firebaseUser.getPhoneNumber().substring(3));
            edt_phoneNumber.setEnabled(false);
        }
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User(
                        edt_email.getText().toString(),
                        edt_name.getText().toString(),
                        edt_phoneNumber.getText().toString(),
                        edt_adress.getText().toString());
                mSocket.emit("register", user.getEmail(), user.getFullName(), user.getPhoneNumber(), user.getAddress());

            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }


    //CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN CHECK LAI LOGIN


}
