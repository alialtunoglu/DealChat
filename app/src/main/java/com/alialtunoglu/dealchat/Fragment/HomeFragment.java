package com.alialtunoglu.dealchat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alialtunoglu.dealchat.Adapter.PostAdapter;
import com.alialtunoglu.dealchat.Model.Post;
import com.alialtunoglu.dealchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    // ImageView'ı bul
    ImageView mesajbox;
    private List<String> followingList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        mesajbox = view.findViewById(R.id.mesajbox);

        // ImageView'a tıklama dinleyici ekle
        mesajbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mesajlar Fragment'ına geçiş yap
                openMesajlarFragment();
            }
        });

        recyclerView = view.findViewById(R.id.recycler_mainpage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postLists);

        recyclerView.setAdapter(postAdapter);

        checkFollowing();




        return view;
    }
    // Mesajlar Fragment'ına geçiş yapan metot
    private void openMesajlarFragment() {
        // Fragment yöneticisi al
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        // Fragment transaction başlat
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Mesajlar Fragment'ını oluştur
        MessageFragment mesajlarFragment = new MessageFragment();

        // Fragment transaction ile Mesajlar Fragment'ını başlat
        fragmentTransaction.replace(R.id.fragmentacilacagicerceve, mesajlarFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    followingList.add(snapshot1.getKey());
                }
                postlariOku();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void postlariOku(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    // Check if the post's publisher is in the followingList or if it's your own post
                    if (followingList.contains(post.getPublisher()) || post.getPublisher().equals(firebaseUser.getUid())) {
                        postLists.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}