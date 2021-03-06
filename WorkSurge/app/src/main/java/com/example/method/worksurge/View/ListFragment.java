package com.example.method.worksurge.View;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.method.worksurge.Enum.FragmentEnum;
import com.example.method.worksurge.Enum.IntentEnum;
import com.example.method.worksurge.Model.VacancyDetailModel;
import com.example.method.worksurge.Model.VacancyModel;
import com.example.method.worksurge.R;
import com.example.method.worksurge.WebsiteConnector.WebsiteConnector;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View view;
    private ListView liv_vacancy;
    private int save = -1;
    private String itemValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list, container, false);

        fillList();

        return view;
    }

    private void fillList() {
        liv_vacancy = (ListView) view.findViewById(R.id.liv_vacancies);

        final ArrayList<VacancyModel> list = ((FoundVacanciesActivity) getActivity()).getVacancyList();
        ArrayList<String> list_temp = new ArrayList<String>();

        for(VacancyModel element : list)
        {
            list_temp.add(element.getTitle());
        }

        if(list != null)
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, list_temp);

            liv_vacancy.setAdapter(adapter);

            liv_vacancy.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            liv_vacancy.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    /*parent.getChildAt(position).setBackgroundColor(
                            Color.parseColor("#A9BCF5"));

                    if (save != -1 && save != position) {
                        parent.getChildAt(save).setBackgroundColor(
                                Color.parseColor("#d6e6ff"));
                    }*/

                    save = position;

                    // ListView Clicked item index
                    int itemPosition = position;

                    // ListView Clicked item value
                    itemValue = (String) liv_vacancy.getItemAtPosition(position);

                    if(((FoundVacanciesActivity) getActivity()).checkConnectivity())
                    {
                        ((FoundVacanciesActivity) getActivity()).ReadWebsiteAsync(itemPosition, FragmentEnum.LIST);
                    }
                    else
                    {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                    }
                }
            });
        }
        else
        {
            Toast.makeText(view.getContext(), getResources().getString(R.string.error_unexpected), Toast.LENGTH_LONG).show();
        }
    }
}
