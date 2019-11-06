package com.example.hairsalonbooking;

import android.os.Bundle;
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
import com.example.hairsalonbooking.Model.User;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    BottomSheetDialog bottomSheetDialog;
    User user = null;
    private Socket mSocket = MySocket.getmSocket();
    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String result = args[0].toString();
            if (result.equalsIgnoreCase("Exits")) {
                toast("Tồn tại");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetDialog.hide();
                    }
                });

            } else if (result.equalsIgnoreCase("Success")) {
                toast("Thank you");
                Common.currentUser = user;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetDialog.hide();
                    }
                });
            } else {
                toast("Lỗi");
            }
        }
    };


    private void toast(final String ss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), ss, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mSocket.on("register", onRegister);
        mSocket.connect();
        checkLogin();
        initView();
        initControl();
    }


    private void checkLogin() {
        if (getIntent() != null) {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if (isLogin) {
                final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                mSocket.emit("checkemail", email);
                mSocket.on("checkemail", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject object = (JSONObject) args[0];
                                if (object != null) {
                                    try {
                                        user = new User(
                                                object.getString("email"),
                                                object.getString("name"),
                                                object.getString("phone"),
                                                object.getString("adress"));
                                        Common.currentUser = user;
                                        bottomNavigationView.setSelectedItemId(R.id.action_home);
                                        Log.d("AAA", "call: " + user);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    showUpdateDialog(email);
                                }
                            }
                        });

                        }
                });
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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    private void initView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void showUpdateDialog(final String email) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_infomation, null);
        Button btn_update = sheetView.findViewById(R.id.btn_Update);
        final TextInputEditText edt_name = sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_phoneNumber = sheetView.findViewById(R.id.edt_phoneNumber);
        final TextInputEditText edt_adress = sheetView.findViewById(R.id.edt_adress);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Click", Toast.LENGTH_SHORT).show();
                user = new User(
                        email,
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
