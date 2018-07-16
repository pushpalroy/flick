package com.pushpal.popularmoviesstage1.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.model.Person;
import com.pushpal.popularmoviesstage1.networking.RESTClient;
import com.pushpal.popularmoviesstage1.networking.RESTClientInterface;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.DateUtil;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class CastActivity extends AppCompatActivity {

    private static final String TAG = CastActivity.class.getSimpleName();

    @BindView(R.id.iv_cast_image)
    ImageView castProfileImage;

    @BindView(R.id.tv_cast_name)
    TextView castName;

    @BindView(R.id.tv_cast_dob)
    TextView castDob;

    @BindView(R.id.tv_biography)
    TextView castBiography;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ll_birth)
    LinearLayout birthLayout;

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cast);
        ButterKnife.bind(this);
        context = this;

        setUpActionBar();

        Bundle extras = getIntent().getExtras();
        int personId = 0;

        if (extras != null) {
            personId = extras.getInt("PERSON_ID");
            birthLayout.setVisibility(View.INVISIBLE);
            fetchPerson(personId);
        }
    }

    private void setUpActionBar() {
        supportPostponeEnterTransition();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setTitle(R.string.cast);
    }

    private void fetchPerson(int personId) {
        RESTClientInterface restClientInterface = RESTClient.getClient().create(RESTClientInterface.class);
        Call<Person> call = restClientInterface.getPersonDetails(personId, Constants.API_KEY);

        if (call != null) {
            call.enqueue(new retrofit2.Callback<Person>() {
                @Override
                public void onResponse(@NonNull Call<Person> call,
                                       @NonNull Response<Person> response) {
                    int statusCode = response.code();

                    if (statusCode == 200) {
                        if (response.body() != null) {
                            Person person = response.body();
                            if (person != null) {

                                castName.setText(person.getName());
                                if (person.getBirthDay() != null) {
                                    birthLayout.setVisibility(View.VISIBLE);
                                    castDob.setText(DateUtil.getFormattedDate(person.getBirthDay()));
                                }
                                castBiography.setText(person.getBiography());
                                String imageURL = Constants.IMAGE_BASE_URL
                                        + Constants.IMAGE_SIZE_185
                                        + person.getProfileImagePath();
                                Picasso.with(context)
                                        .load(imageURL)
                                        .placeholder(R.drawable.person)
                                        .into(castProfileImage);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Person> call, @NonNull Throwable throwable) {
                    // Log error here since request failed
                    Log.e(TAG, throwable.toString());
                }
            });
        }
    }
}
