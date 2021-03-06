package com.example.method.worksurge.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.method.worksurge.Enum.FragmentEnum;
import com.example.method.worksurge.Enum.IntentEnum;
import com.example.method.worksurge.Location.LocationService;
import com.example.method.worksurge.Model.VacancyDetailModel;
import com.example.method.worksurge.Model.VacancyMapDetail;
import com.example.method.worksurge.Model.VacancyModel;
import com.example.method.worksurge.R;
import com.example.method.worksurge.WebsiteConnector.WebsiteConnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private WebsiteConnector wc = null;
    private FragmentEnum chosen = FragmentEnum.LIST;
    private LocationManager locManager;
    private List<VacancyModel> list = null;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Spinner radiiSpinner = createRadiiSpinner();
        readViewPreferenceFile();
        wc = new WebsiteConnector();

        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        dialog = new ProgressDialog(SearchActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.action_about:
                // startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                dlgAlert.setMessage(getResources().getString(R.string.app_about));
                dlgAlert.setTitle(getResources().getString(R.string.app_name));
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                break;
            case R.id.action_favorites:
                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));
                break;
            default:
                break;
        }

        return true;
    }

    private Spinner createRadiiSpinner(){
        //Create new Spinner object
        Spinner spinner = (Spinner) findViewById(R.id.static_spinner);
        //Create an ArrayAdapter for the items
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Radii, android.R.layout.simple_spinner_item);
        //Specify the list when using
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //return spinner
        return spinner;
    }

    public void onClickList(View v)
    {
        chosen = FragmentEnum.LIST;
    }

    public void onClickMap(View v)
    {
        chosen = FragmentEnum.MAP;
    }

    // Go to foundVacanciesActivity.
    public void foundVacanciesActivity(View v) {
        // Can
        // it be more clean / better?
        if(checkConnectivity())
        {
            EditText textSearchBox = (EditText) findViewById(R.id.txtSearch);
            LocationService locService = new LocationService(getApplicationContext());
            Spinner spinnerKm = (Spinner) findViewById(R.id.static_spinner);
            int radius = Integer.parseInt(spinnerKm.getSelectedItem().toString().replaceAll("\\D+", "")); // KM radius, convert if non-standard
            String location = locService.getLocationAddress();
            String activityChoice = "";

            new ReadWebsiteAsync(this.getApplicationContext()).execute(
                    new UserParam(textSearchBox.getText().toString(), radius, location)
            );

            new ReadWebsiteMapAsync(this.getApplicationContext()).execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
        }


    }

    public void readViewPreferenceFile(){
        try {
            StringBuilder sb = new StringBuilder();
            FileInputStream fis = openFileInput("storeViewText.txt");
            Reader r = new InputStreamReader(fis, "UTF-8");
            int i = r.read();
            while(i >= 0){
                sb.append((char)i);
                i = r.read();
            }
            if(sb.toString().equalsIgnoreCase("map")){
                chosen = FragmentEnum.MAP;
            }else{
                chosen = FragmentEnum.LIST;
            }
        }catch(IOException fne) {

        }
    }

    private boolean checkConnectivity()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private class ReadWebsiteAsync extends AsyncTask<UserParam, Void, Boolean> {
        private Context context;

        private ReadWebsiteAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(UserParam... params) {
            list = wc.readWebsite(params[0].searchCrit, params[0].radius, params[0].location);
            return true; // Return false if reading is unsuccesful
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                if(list == null ? true : list.size() == 0)
                {
                    Toast.makeText(context, getResources().getString(R.string.no_vacancy), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            else
            {
                Toast.makeText(context, getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getResources().getString(R.string.loading));
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class ReadWebsiteMapAsync extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private List<VacancyMapDetail> model = new ArrayList<VacancyMapDetail>();

        private ReadWebsiteMapAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            model = wc.readWebsiteMap(list);
            return true; // Return false if reading is unsuccesful
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(dialog.isShowing())
                dialog.dismiss();

            if(!result)
            {
                Toast.makeText(context, getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            }
            else
            {
                Intent iFoundVacanciesActivity = new Intent(context, FoundVacanciesActivity.class);
                iFoundVacanciesActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                iFoundVacanciesActivity.putParcelableArrayListExtra(IntentEnum.FOUND_MULTIPLE_VACANCIES.toString(), (ArrayList<VacancyModel>) list);
                iFoundVacanciesActivity.putParcelableArrayListExtra(IntentEnum.FOUND_MULTIPLE_MAP_VACANCIES.toString(), (ArrayList<VacancyMapDetail>) model);
                iFoundVacanciesActivity.putExtra(IntentEnum.DECISION.toString(), chosen);
                context.startActivity(iFoundVacanciesActivity);
            }


        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class UserParam {
        String searchCrit;
        int radius;
        String location;

        UserParam(String searchCrit, int radius, String location) {
            this.searchCrit = searchCrit;
            this.radius = radius;
            this.location = location;
        }
    }
}
