package com.example.hp_awareness_app;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewActivity extends AppCompatActivity {
  Toolbar toolbar;
  DrawerLayout drawerLayout;
  ActionBarDrawerToggle actionBarDrawerToggle;
  NavigationView navigationView;
  CardView cardView;
  CardView cardView3;
  CardView helpCard;
  ImageView bell;
  TextView msg;
  Button back;
  DatabaseReference reference, userRef;
  SharedPreferences preferences;
  SharedPreferences.Editor editor;
  Boolean started;
  Integer Total;
  TextView hp;

  HashMap<String, Object> map;
  static String type = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_nav_drawer);
    if (android.os.Build.VERSION.SDK_INT >= 21) {
      Window window = this.getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      window.setStatusBarColor(this.getResources().getColor(R.color.black));
    }
    toolbar = findViewById(R.id.mytoolbar);
    drawerLayout = findViewById(R.id.drawer);
    type = getIntent().getStringExtra("type");
    // hp = findViewById(R.id.hp);
    setSupportActionBar(toolbar);
    actionBarDrawerToggle =
        new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
    drawerLayout.addDrawerListener(actionBarDrawerToggle);
    actionBarDrawerToggle.syncState();
    navigationView = findViewById(R.id.nav_view);
    Menu menu = navigationView.getMenu();

    MenuItem add_item = menu.findItem(R.id.admin_add);

    if (Objects.equals(type, "Admin")) {
      add_item.setVisible(true);
    } else {
      add_item.setVisible(false);
    }

    MenuItem dashboard = menu.findItem(R.id.admin_dashboard);

    if (Objects.equals(type, "Admin")) {
      dashboard.setVisible(true);
    } else {
      dashboard.setVisible(false);
    }

    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase.getInstance()
        .getReference()
        .child("Users")
        .child(uid)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                map = (HashMap<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                  TextView nameTV = findViewById(R.id.user);
                  nameTV.setText(String.valueOf(map.get("name")));
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Register", databaseError.getMessage());
              }
            });

    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
              case R.id.faq:
                startActivity(new Intent(NewActivity.this, FaqActivity.class));
                break;
              case R.id.developers:
                startActivity(new Intent(NewActivity.this, activity_contributors.class));
                break;
              case R.id.edit_prifile:
                startActivity(new Intent(NewActivity.this, RegisterActivity.class));
                break;
              case R.id.admin_add:
                startActivity(new Intent(NewActivity.this, AddAdminActivity.class));
                break;
              case R.id.admin_dashboard:
                startActivity(new Intent(NewActivity.this, AdminDashboard.class));
                break;
              case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(NewActivity.this, PhoneActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            }

            return false;
          }
        });

    cardView = findViewById(R.id.cardView2);
    cardView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(NewActivity.this, CasesFragment.class);
            startActivity(intent);
          }
        });

    cardView3 = findViewById(R.id.cardView3);
    cardView3.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(NewActivity.this, GeofenceActivity.class);
            startActivity(intent);
          }
        });

    helpCard = findViewById(R.id.helpCard3);
    helpCard.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(NewActivity.this, HelpMessegeActivity.class);
            startActivity(intent);
          }
        });

    bell = findViewById(R.id.msgBell);
    bell.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            OpenDialog();
          }
        });

    preferences = getSharedPreferences("App", MODE_PRIVATE);
    editor = preferences.edit();
    started = preferences.getBoolean("First Message Sent", false);
    Total = preferences.getInt("total", 0);
    //  hp.setText("HIMACHAL PRADESH : "+Total );

    String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    userRef = FirebaseDatabase.getInstance().getReference().child("User").child(uID);
    Map<String, String> map = new HashMap<>();
    map.put("Message", "No new message");
  }

  private void OpenDialog() {
    Dialog dialog = new Dialog(NewActivity.this);
    dialog.setContentView(R.layout.msg_dialog);
    msg = dialog.findViewById(R.id.adminMsg);
    back = dialog.findViewById(R.id.backBtn);

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uiD = user.getUid();
    reference = FirebaseDatabase.getInstance().getReference().child("User").child(uiD);

    if (started == true) {
      reference.addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              String message = dataSnapshot.child("Message").getValue().toString();
              if (message != null) {
                msg.setText(message);

              } else if (message == null) {
                msg.setText("No new messege");
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
          });
    }

    back.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            dialog.dismiss();
          }
        });

    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.show();
  }

  public void adminDashClick(View v) {
    startActivity(new Intent(NewActivity.this, AdminDashboard.class));
  }
}
