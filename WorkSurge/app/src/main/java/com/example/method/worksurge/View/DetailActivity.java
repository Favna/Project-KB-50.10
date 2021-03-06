package com.example.method.worksurge.View;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.method.worksurge.Enum.IntentEnum;
import com.example.method.worksurge.Model.VacancyDetailModel;
import com.example.method.worksurge.Model.VacancyModel;
import com.example.method.worksurge.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class DetailActivity extends AppCompatActivity {

    private TextView title, meta, details, company;
    private Button telefoon;
    private VacancyDetailModel model;

    //Database stuff for favorites
    static final String PROVIDER_NAME = "com.example.method.worksurge.ContentProvider.FavoriteProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/favorite";
    static final Uri CONTENT_URL = Uri.parse(URL);
    ContentResolver resolver;
    //End Database stuff for favorites


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        resolver = getContentResolver();

        this.model = getIntent().getParcelableExtra(IntentEnum.FOUND_SINGLE_VACANCY.toString());
        setViewText(this.model);

        telefoon = (Button) findViewById(R.id.btn_call);
        if(model.getTelefoon().isEmpty())
            telefoon.setEnabled(false);
    }

    private void setViewText(VacancyDetailModel model) {
        this.title = (TextView) findViewById(R.id.txtCustomTitle);
        this.meta = (TextView) findViewById(R.id.txtCustomMeta);
        this.details = (TextView) findViewById(R.id.txtCustomDetails);
        this.company = (TextView) findViewById(R.id.txtCustomCompany);

        this.title.setText(model.getTitle());
        this.meta.setText(model.getMeta().get(0)); // TODO: show all meta data
        this.details.setText(model.getDetail());
        this.company.setText(model.getCompany());
    }

    public void call(View v) {
        Intent intentCall = new Intent(Intent.ACTION_DIAL);
        intentCall.setData(Uri.parse("tel:" + model.getTelefoon()));
        startActivity(intentCall);
    }

    // TODO: Send email on seperate thread
    public void email(View v) {
        String[] TO = {getEmailFromFile()}; // TODO: Retrieve user email
        TextView title = (TextView) findViewById(R.id.txtCustomTitle);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WorkSurge: " + title.getText());
        emailIntent.putExtra(Intent.EXTRA_TEXT, model.getUrl());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Toast.makeText(getApplicationContext(), "Your email has been sent to: " + TO[0], Toast.LENGTH_LONG).show();
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(DetailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public String getEmailFromFile(){
        String email = "";
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = openFileInput("storeEmailText.txt");
            Reader r = new InputStreamReader(fis, "UTF-8");
            int i = r.read();
            while(i >= 0){
                sb.append((char)i);
                i = r.read();
            }
            email = sb.toString();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        return email;
    }

    //TODO: Working setFavorite function
    public void setFavorite(View v) {
        ContentValues values = new ContentValues();

        values.put("name", title.getText().toString());
        values.put("details", details.getText().toString());
        values.put("CompanyURL", "www.google.nl");
        values.put("meta", meta.getText().toString());

        resolver.insert(CONTENT_URL, values);

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_favorite), Toast.LENGTH_LONG).show();
    }
}
