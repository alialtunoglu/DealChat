package com.alialtunoglu.dealchat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alialtunoglu.dealchat.Adapter.KullaniciAdapter;
import com.alialtunoglu.dealchat.Model.Chat;
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


public class MessageFragment extends Fragment {

    private RecyclerView recyclerView;
    private KullaniciAdapter kullaniciAdapter;
    private List<Kullanici> mKullanicilar= new ArrayList<>();
    private List<String> userList= new ArrayList<>();

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        recyclerView = view.findViewById(R.id.mesajlistesirecyler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        kullaniciAdapter = new KullaniciAdapter(getContext(),mKullanicilar,true);
        recyclerView.setAdapter(kullaniciAdapter);


        FirebaseDatabase.getInstance().getReference("Mesajlar").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        //Eğer snapshot varsa
                        if(snapshot.exists()){
                            for(DataSnapshot snapshot1: snapshot.getChildren()){
                                Chat chat= snapshot1.getValue(Chat.class);
                                //benim gönderdiğim kişilerin mesajlarını göster
                                if(chat.getGonderen().equals(firebaseUser.getUid())){
                                    userList.add(chat.getAlici());
                                }
                                //bana mesaj gönderen kişilerin mesaj listesini göster
                                if(chat.getAlici().equals(firebaseUser.getUid())){
                                    userList.add(chat.getGonderen());
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        kullaniciOku();

        return view;
    }

    private void kullaniciOku() {
        FirebaseDatabase.getInstance().getReference("Kullanıcılar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mKullanicilar.clear();
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            Kullanici kullanici= snapshot1.getValue(Kullanici.class);
                            for(String id : userList){
                                //contains ne demek bir bak
                                if (kullanici.getId().equals(id) && !mKullanicilar.contains(kullanici)){
                                    mKullanicilar.add(kullanici);
                                    if(mKullanicilar.size()!=0){

                                    }else{
                                        mKullanicilar.add(kullanici);
                                    }
                                }
                            }

                        }
                        // Eğer arama yapıyorsak, son mesajı gizle
                        kullaniciAdapter.setShowLastMessage(true);
                        kullaniciAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}