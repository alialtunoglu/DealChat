package com.alialtunoglu.dealchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alialtunoglu.dealchat.Fragment.ProfileFragment;
import com.alialtunoglu.dealchat.MessageActivity;
import com.alialtunoglu.dealchat.Model.Chat;
import com.alialtunoglu.dealchat.Model.Kullanici;
import com.alialtunoglu.dealchat.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class KullaniciAdapter extends RecyclerView.Adapter<KullaniciAdapter.ViewHolder> {
    private Context mContext;
    private List<Kullanici> mKullanicilar;
    private Boolean ischat; //Online offline durumu kontrol edici
    private String sonnmesajstring;
    private Boolean showLastMessage; //arama kısmında mesajı göstermeyip mesaj kısmında göstermesi için
    private Boolean isLikes=false;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public KullaniciAdapter(Context mContext, List<Kullanici> mKullanicilar,boolean ischat) {
        this.mContext = mContext;
        this.mKullanicilar = mKullanicilar;
        this.ischat=ischat;
    }

    public KullaniciAdapter(Context mContext, List<Kullanici> mKullanicilar,boolean ischat,boolean isLikes) {
        this.mContext = mContext;
        this.mKullanicilar = mKullanicilar;
        this.ischat=ischat;
        this.isLikes=isLikes;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.kullanici_ogesi, parent, false);
        return new KullaniciAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Kullanici kullanici = mKullanicilar.get(position);


        //Eğer son Mesaj gösterme True ise göstersin değilse gizlesin
        if (showLastMessage) {
            holder.sonmesaj.setVisibility(View.VISIBLE);
            //son mesaj varsa göster yoksa gösterme
            if(ischat){
                sonmesajj(kullanici.getId(),holder.sonmesaj);
            }else{
                holder.sonmesaj.setVisibility(View.GONE);
            }
            // Son mesajı gösterme işlemleri
        } else {
            holder.sonmesaj.setText(kullanici.getBio());
        }

        if(ischat){
            if(kullanici.getDurum().equals("online")){
                holder.online.setVisibility(View.VISIBLE);
                holder.offline.setVisibility(View.GONE);
            }else{
                holder.online.setVisibility(View.GONE);
                holder.offline.setVisibility(View.VISIBLE);
            }
        }


        if (isLikes){
            holder.btn_follow.setVisibility(View.GONE);
            holder.mesajgonder.setVisibility(View.GONE);
        }else{
            holder.btn_follow.setVisibility(View.VISIBLE);
        }

        holder.kullaniciAd.setText(kullanici.getUsername());
        holder.kullaniciAd.setText(kullanici.getFullname());
        //resim okuma işlevi
        Glide.with(mContext).load(kullanici.getImageUrl()).into(holder.profilresim);

        isFollowing(kullanici.getId(), holder.btn_follow);


        if (kullanici.getId().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        }

        //Kullanıcı üzerine tıkladığında yapılacaklar
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Buradaki yönlendirme profil sayfasını görmek için
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",kullanici.getId());
                editor.apply();
                //Buradaki kod kullanici id'sinin verilerini alıyor diğer sayfaya yönlendiriyor
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,new ProfileFragment()).addToBackStack(null).commit();
            }
        });

        //takip etme butonu işlevleri
        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btn_follow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(kullanici.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(kullanici.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotifications(kullanici.getId());
                }else{
                    Log.d("Burası","deneme");
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(kullanici.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(kullanici.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                    removeNotification(kullanici.getId());
                }
            }
        });

        //Profilin yanındaki mesaj gönderme butonuna basınca mesaj gönderme activitesi açılacak
        holder.mesajgonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mcontext burada kimin sayfasına tıklarsak onun sayfasını açmasını sağlıyor tıkladığım ögeden demek
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId",kullanici.getId());
                mContext.startActivity(intent);
            }
        });

    }
    private void addNotifications(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","seni takip etmeye başladı");
        hashMap.put("postid","");
        hashMap.put("ispost",false);
        hashMap.put("iscomment",false);
        hashMap.put("isfollow",true);

        reference.push().setValue(hashMap);


    }
    private void removeNotification(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        Log.d("Userid33",userid+" == "+firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Kullanıcı ID'sini ve ispost değerini kontrol et
                    if (snapshot.child("userid").getValue(String.class).equals(firebaseUser.getUid()) &&
                            snapshot.child("isfollow").getValue(Boolean.class)) {
                        snapshot.getRef().removeValue(); // Eşleşen ve ispost true olan bildirimi sil
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
    @Override
    public int getItemCount() {
        return mKullanicilar.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public Button btn_follow;
        public ImageView profilresim, mesajgonder;
        public TextView kullaniciAd, online, offline, sonmesaj;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilresim = itemView.findViewById(R.id.resimKullanici);
            kullaniciAd = itemView.findViewById(R.id.kullaniciAdi);
            mesajgonder = itemView.findViewById(R.id.mesajgonder);
            sonmesaj = itemView.findViewById(R.id.sonmesaj);

            online = itemView.findViewById(R.id.online);
            offline = itemView.findViewById(R.id.offline);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }



    private void isFollowing(String userid, Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()) {
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sonmesajj(String id, TextView sonmesaj) {
        sonnmesajstring = "default";
        FirebaseDatabase.getInstance().getReference("Mesajlar").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                            Chat chat = snapshot1.getValue(Chat.class);
                            if(chat.getAlici().equals(firebaseUser.getUid()) && chat.getGonderen().equals(id) ||
                                    chat.getAlici().equals(id)&& chat.getGonderen().equals(firebaseUser.getUid())){

                                sonnmesajstring = chat.getMesaj();
                            }
                        }
                        if(sonnmesajstring.equals("default")){
                            sonmesaj.setText("Mesaj Yok");
                        }
                        else{
                            sonmesaj.setText(sonnmesajstring);
                        }
                        sonnmesajstring = "default";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void setShowLastMessage(boolean showLastMessage) {
        this.showLastMessage = showLastMessage;
    }}
