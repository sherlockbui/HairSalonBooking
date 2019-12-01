package com.example.hairsalonbooking;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hairsalonbooking.Common.MySocket;
import com.github.nkzawa.socketio.client.Socket;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class RatingBarberActivity extends AppCompatActivity implements RatingDialogListener {
    private Socket mSocket = MySocket.getmSocket();
    private String idBarber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_barber);
        mSocket.connect();
        Intent intent = getIntent();
        idBarber = intent.getStringExtra("ID_BARBER");
        showDialog();

    }

    @Override
    public void onNegativeButtonClicked() {
        finish();

    }

    @Override
    public void onNeutralButtonClicked() {
        finish();
    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {
        mSocket.emit("Rating", i, s, idBarber);
        finish();
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Đồng ý")
                .setNegativeButtonText("Đóng")
                .setNeutralButtonText("Để sau")
                .setNoteDescriptions(Arrays.asList("Rất tệ", "Chưa tốt", "Tạm ổn", "Rất tốt", "Tuyệt vời !!!"))
                .setTitle("Đánh giá nhân viên!")
                .setDescription("Chọn sao tương ứng với múc độ hài lòng của bạn!")
                .setCommentInputEnabled(true)
                .setHint("Vui lòng đánh giá nhân viên của chúng tôi!")
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(RatingBarberActivity.this)
                .show();
    }
}
