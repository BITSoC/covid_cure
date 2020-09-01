package com.example.hp_awareness_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CasesFragment extends AppCompatActivity {

  private RecyclerView recyclerView;
  private List<DistrictCases> districtCases;
  private static Retrofit retrofit;
  private static final String BASE_URL = "https://api.covid19india.org/state_district_wise.json";
  Integer active, rec, cnf, dth;
  Integer chActive, chRec, chCnf, chDth;
  Integer HaActive, HaRec, HaCnf, HaDth;
  Integer KaA, kaR, KaC, KaD;
  Integer kiA, KiR, KiC, KiD;
  Integer KuA, KuR, KuC, KuD;
  Integer LaA, LaR, LaC, LaD;
  Integer MaA, MaR, MaC, MaD;
  Integer ShA, ShR, ShC, ShD;
  Integer SiA, SiR, SiC, SiD;
  Integer SoA, SoR, SoC, SoD;
  Integer UnA, UnR, UnC, UnD;
  Integer TActive, TRec, TCnf, Tdth;
  TextView tvA, tvC, tvR, tvD;
  SharedPreferences preferences;
  SharedPreferences.Editor editor;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cases);
    tvA = findViewById(R.id.ActiveNo);
    tvC = findViewById(R.id.confNo);
    tvR = findViewById(R.id.RecoverNo);
    tvD = findViewById(R.id.DeathNo);
    GetData();
    final LoadingDialog loadingDialog = new LoadingDialog(CasesFragment.this);
    loadingDialog.startLoadingDialog();
    Handler handler = new Handler();
    handler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            loadingDialog.dismissDialog();
          }
        },
        4000);
  }

  private void GetData() {
    CovidApiService service =
        RetrofitclientInstance.getRetrofitInstance().create(CovidApiService.class);
    Call<ModelCovidCase> call = service.getStatus(BASE_URL);
    call.enqueue(
        new Callback<ModelCovidCase>() {
          @Override
          public void onResponse(Call<ModelCovidCase> call, Response<ModelCovidCase> response) {
            if (response.isSuccessful()) {
              active =
                  response.body().getHimachalPradesh().getDistrictData().getBilaspur().getActive();
              cnf =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getBilaspur()
                      .getConfirmed();
              rec =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getBilaspur()
                      .getRecovered();
              dth =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getBilaspur()
                      .getDeceased();
              chActive =
                  response.body().getHimachalPradesh().getDistrictData().getChamba().getActive();
              chCnf =
                  response.body().getHimachalPradesh().getDistrictData().getChamba().getConfirmed();
              chRec =
                  response.body().getHimachalPradesh().getDistrictData().getChamba().getRecovered();
              chDth =
                  response.body().getHimachalPradesh().getDistrictData().getChamba().getDeceased();
              HaActive =
                  response.body().getHimachalPradesh().getDistrictData().getHamirpur().getActive();
              HaCnf =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getHamirpur()
                      .getConfirmed();
              HaRec =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getHamirpur()
                      .getRecovered();
              HaDth =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getHamirpur()
                      .getDeceased();
              KaA = response.body().getHimachalPradesh().getDistrictData().getKangra().getActive();
              KaC =
                  response.body().getHimachalPradesh().getDistrictData().getKangra().getConfirmed();
              kaR =
                  response.body().getHimachalPradesh().getDistrictData().getKangra().getRecovered();
              KaD =
                  response.body().getHimachalPradesh().getDistrictData().getKangra().getDeceased();
              kiA = response.body().getHimachalPradesh().getDistrictData().getKinnaur().getActive();
              KiR =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getKinnaur()
                      .getRecovered();
              KiC =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getKinnaur()
                      .getConfirmed();
              KiD =
                  response.body().getHimachalPradesh().getDistrictData().getKinnaur().getDeceased();
              KuA = response.body().getHimachalPradesh().getDistrictData().getKullu().getActive();
              KuC =
                  response.body().getHimachalPradesh().getDistrictData().getKullu().getConfirmed();
              KuR =
                  response.body().getHimachalPradesh().getDistrictData().getKullu().getRecovered();
              KuD = response.body().getHimachalPradesh().getDistrictData().getKullu().getDeceased();
              LaA =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getLahaulAndSpiti()
                      .getActive();
              LaC =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getLahaulAndSpiti()
                      .getConfirmed();
              LaR =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getLahaulAndSpiti()
                      .getRecovered();
              LaD =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getLahaulAndSpiti()
                      .getDeceased();
              MaA = response.body().getHimachalPradesh().getDistrictData().getMandi().getActive();
              MaC =
                  response.body().getHimachalPradesh().getDistrictData().getMandi().getConfirmed();
              MaR =
                  response.body().getHimachalPradesh().getDistrictData().getMandi().getRecovered();
              MaD = response.body().getHimachalPradesh().getDistrictData().getMandi().getDeceased();
              ShA = response.body().getHimachalPradesh().getDistrictData().getShimla().getActive();
              ShC =
                  response.body().getHimachalPradesh().getDistrictData().getShimla().getConfirmed();
              ShR =
                  response.body().getHimachalPradesh().getDistrictData().getShimla().getRecovered();
              ShD =
                  response.body().getHimachalPradesh().getDistrictData().getShimla().getDeceased();
              SiA = response.body().getHimachalPradesh().getDistrictData().getSirmaur().getActive();
              SiC =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getSirmaur()
                      .getConfirmed();
              SiR =
                  response
                      .body()
                      .getHimachalPradesh()
                      .getDistrictData()
                      .getSirmaur()
                      .getRecovered();
              SiD =
                  response.body().getHimachalPradesh().getDistrictData().getSirmaur().getDeceased();
              SoA = response.body().getHimachalPradesh().getDistrictData().getSolan().getActive();
              SoC =
                  response.body().getHimachalPradesh().getDistrictData().getSolan().getConfirmed();
              SoR =
                  response.body().getHimachalPradesh().getDistrictData().getSolan().getRecovered();
              SoD = response.body().getHimachalPradesh().getDistrictData().getSolan().getDeceased();
              UnA = response.body().getHimachalPradesh().getDistrictData().getUna().getActive();
              UnC = response.body().getHimachalPradesh().getDistrictData().getUna().getConfirmed();
              UnR = response.body().getHimachalPradesh().getDistrictData().getUna().getRecovered();
              UnD = response.body().getHimachalPradesh().getDistrictData().getUna().getDeceased();

              districtCases = new ArrayList<>();
              districtCases.add(new DistrictCases("Bilaspur", active, cnf, rec, dth));
              districtCases.add(new DistrictCases("Chamba", chActive, chCnf, chRec, chDth));
              districtCases.add(new DistrictCases("Hamirpur", HaActive, HaCnf, HaRec, HaDth));
              districtCases.add(new DistrictCases("Kangra", KaA, KaC, kaR, KaD));
              districtCases.add(new DistrictCases("Kinnaur", kiA, KiC, KiR, KiD));
              districtCases.add(new DistrictCases("Kullu", KuA, KuC, KuR, KuD));
              districtCases.add(new DistrictCases("Lahaul & Spiti", LaA, LaC, LaR, LaD));
              districtCases.add(new DistrictCases("Mandi", MaA, MaC, MaR, MaD));
              districtCases.add(new DistrictCases("Shimla", ShA, ShC, ShR, ShD));
              districtCases.add(new DistrictCases("Sirmaur", SiA, SiC, SiR, SiD));
              districtCases.add(new DistrictCases("Solan", SoA, SoC, SoR, SoD));
              districtCases.add(new DistrictCases("Una", UnA, UnC, UnR, UnD));

              recyclerView = (RecyclerView) findViewById(R.id.caseRecycler);
              BilaspurAdapter adapter = new BilaspurAdapter(districtCases, CasesFragment.this);
              recyclerView.setLayoutManager(new LinearLayoutManager(CasesFragment.this));
              recyclerView.setAdapter(adapter);
              // recyclerView.setNestedScrollingEnabled(false);
              Log.d("Response", "Success" + response.code());
              TActive =
                  active + chActive + HaActive + KaA + kiA + KuA + LaA + MaA + ShA + SiA + SoA
                      + UnA;
              TCnf = cnf + chCnf + HaCnf + KaC + KiC + LaC + KuC + MaC + ShC + SiC + SoC + UnC;
              TRec = rec + chRec + HaRec + kaR + KiR + KuR + LaR + MaR + ShR + SiR + SoR + UnR;
              Tdth = dth + chDth + HaDth + KaD + KiD + KuD + LaD + MaD + ShD + SiD + SoD + UnD;

              tvA.setText("" + TActive);
              tvC.setText("" + TCnf);
              tvR.setText("" + TRec);
              tvD.setText("" + Tdth);

              preferences = getSharedPreferences("App", MODE_PRIVATE);
              editor = preferences.edit();

              editor.putInt("Total", TActive + TCnf + TRec + Tdth);
              editor.commit();
            }
          }

          @Override
          public void onFailure(Call<ModelCovidCase> call, Throwable t) {}
        });
  }
}
