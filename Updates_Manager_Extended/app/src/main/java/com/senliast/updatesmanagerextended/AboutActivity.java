package com.senliast.updatesmanagerextended;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.color.MaterialColors;
import com.senliast.MyApplication;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutActivity extends AppCompatActivity {

    private Button buttonBack;
    SpannableString spannableString;
    ClickableSpan clickableSpan;
    private TextView textViewAboutPart4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAbout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonBack.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        findViewById(R.id.activityAbout).setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, getColor(R.color.background)));
        textViewAboutPart4 = findViewById(R.id.textViewAboutPart4);

        spannableString = new SpannableString(getText(R.string.about_app_part_4));
        clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_app_part_4)));
                startActivity(browserIntent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, getString(R.string.about_app_part_4).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewAboutPart4.setText(spannableString);
        textViewAboutPart4.setMovementMethod(LinkMovementMethod.getInstance());
    }
}