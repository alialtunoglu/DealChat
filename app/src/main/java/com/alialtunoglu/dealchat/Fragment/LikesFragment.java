package com.alialtunoglu.dealchat.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alialtunoglu.dealchat.Adapter.KullaniciAdapter;
import com.alialtunoglu.dealchat.Model.Kullanici;
import com.alialtunoglu.dealchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class LikesFragment extends Fragment {

    private RecyclerView recyclerView;
    private KullaniciAdapter kullaniciAdapter;
    private List<Kullanici> mKullanicilar= new ArrayList<>();
    private List<String> likesList = new ArrayList<>();

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view =  inflater.inflate(R.layout.fragment_likes, container, false);

        recyclerView = view.findViewById(R.id.recycler_likes);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        kullaniciAdapter = new KullaniciAdapter(getContext(),mKullanicilar,true,true);
        recyclerView.setAdapter(kullaniciAdapter);

        // Retrieve profileid from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String postid = prefs.getString("postid", "none");


        FirebaseDatabase.getInstance().getReference("Likes").child(postid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        likesList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                // Burada snapshot1.getKey() ile takip edilen kullanıcı ID'sine ulaşabilirsiniz
                                String followedUserId = snapshot1.getKey();
                                Log.d("likes",followedUserId);
                                likesList.add(followedUserId);
                            }
                            // Şimdi followerList içindeki takipçi ID'leri ile ilgili kullanıcıları getirebilirsiniz
                            getFollowingUsers(likesList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return view;
    }


    private void getFollowingUsers(List<String> likesList) {

        FirebaseDatabase.getInstance().getReference("Kullanıcılar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mKullanicilar.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Kullanici kullanici = snapshot.getValue(Kullanici.class);
                            // Eğer followerList içindeki ID'ye sahip bir kullanıcı varsa, listeye ekle
                            if (likesList.contains(kullanici.getId())) {
                                mKullanicilar.add(kullanici);
                                Log.d("kullanici",kullanici.getUsername());
                            }
                        }
                        //sayfayı yenile demek bu sürekli
                        kullaniciAdapter.setShowLastMessage(false);
                        kullaniciAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
}