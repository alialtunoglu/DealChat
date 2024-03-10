package com.alialtunoglu.dealchat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alialtunoglu.dealchat.Adapter.KullaniciAdapter;
import com.alialtunoglu.dealchat.Model.Kullanici;
import com.alialtunoglu.dealchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private KullaniciAdapter kullaniciAdapter;
    private List<Kullanici> mKullanicilar;
    private EditText search_bar;
    private FirebaseUser mevcutKullanici = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_arama);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);

        mKullanicilar=new ArrayList<>();
        kullaniciAdapter = new KullaniciAdapter(getContext(),mKullanicilar,true);
        recyclerView.setAdapter(kullaniciAdapter);


        kullaniciOku();
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Arama(s.toString().toLowerCase());//tüm aramalar küçük harfle olacak

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        return view;
    }
    private void Arama(String s) {
        Query sorgu =  FirebaseDatabase.getInstance().getReference("Kullanıcılar").orderByChild("username")
                .startAt(s).endAt(s+"\uf8ff");
        sorgu.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mKullanicilar.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Kullanici kullanici = snapshot1.getValue(Kullanici.class);
                    if (kullanici != null && !kullanici.getId().equals(mevcutKullanici.getUid())) {
                        mKullanicilar.add(kullanici);
                    }
                }
                kullaniciAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Kullanıcıları listeliyoruz
    private void kullaniciOku(){
        //addValue ile okuma yapabiliriz Firebase içerisindeki kullanıcılar ile ilgili her şeyi okuyabiliriz
        FirebaseDatabase.getInstance().getReference("Kullanıcılar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (search_bar.getText().toString().equals("")) {
                            mKullanicilar.clear();
                            //database'in içerisindeki çocukları oku Id,ad,mail,parola
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                //kullanici.classdan okuycağız
                                Kullanici kullanici = snapshot1.getValue(Kullanici.class);
                                // Kendi kullanıcımızı filtreliyoruz
                                if (kullanici != null && !kullanici.getId().equals(mevcutKullanici.getUid())) {
                                    mKullanicilar.add(kullanici);
                                }
                            }
                        }
                        //sayfayı yenile demek bu sürekli
                        kullaniciAdapter.setShowLastMessage(false);
                        kullaniciAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}