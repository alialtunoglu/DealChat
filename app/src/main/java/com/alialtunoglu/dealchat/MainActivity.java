package com.alialtunoglu.dealchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.alialtunoglu.dealchat.Fragment.HomeFragment;
import com.alialtunoglu.dealchat.Fragment.NotificationFragment;
import com.alialtunoglu.dealchat.Fragment.ProfileFragment;
import com.alialtunoglu.dealchat.Fragment.SearchFragment;
import com.alialtunoglu.dealchat.Model.Chat;
import com.alialtunoglu.dealchat.Model.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment=null;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();


        if(intent!= null){
            String publisher = intent.getString("publisherid");
            SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
            editor.putString("profileid",publisher);
            editor.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve, new ProfileFragment()).commit();
        }else{
            // Aktivite oluşturulduğunda başlangıç fragment'ını ayarla
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve, new HomeFragment()).commit();
        }


    }
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {


            if(item.getItemId()==R.id.nav_home){
                selectedFragment = new HomeFragment();
            }
            else if(item.getItemId()==R.id.nav_search){
                selectedFragment = new SearchFragment();
            }
            else if(item.getItemId()==R.id.nav_add){
                selectedFragment=null;
                Intent intent = new Intent(MainActivity.this,PostActivity.class);
                startActivity(intent);
            }
            else if(item.getItemId()==R.id.nav_heart){
                selectedFragment=new NotificationFragment();
            }
            else if(item.getItemId()==R.id.nav_profile){
                SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.apply();
                selectedFragment= new ProfileFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve, selectedFragment).commit();
            }

            return true;
        }
    };
    private void online(final String durum) {
        // Aktivite görünür durumdaysa veya bir döngüye neden olmaması için başka bir kontrol ekleyin
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("durum", durum);
            db.updateChildren(hashMap);

    }


    @Override
    protected void onStop() {
        super.onStop();
        online("offline");
    }

    @Override
    protected void onStart() {
        super.onStart();
        online("online");
    }

}