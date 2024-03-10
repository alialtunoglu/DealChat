package com.alialtunoglu.dealchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alialtunoglu.dealchat.Adapter.ChatAdapter;
import com.alialtunoglu.dealchat.Fragment.HomeFragment;
import com.alialtunoglu.dealchat.Fragment.MessageFragment;
import com.alialtunoglu.dealchat.Model.Chat;
import com.alialtunoglu.dealchat.Model.Kullanici;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private ImageButton geritusu;
    private CircleImageView profilresim;
    private TextView kullaniciadi;
    private EditText mesajgirdi;
    private ImageView fotoekle,gonder;
    private Intent intent;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private StringBuilder saat,tarih;

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> mMesajlar= new ArrayList<>();

    private ValueEventListener value;
    DatabaseReference reference;

    String userid;
    Uri resimUri;
    String myuri="";
    StorageTask yuklegorevi;
    StorageReference resimyukleyolu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        intent= getIntent();
        userid = intent.getStringExtra("userId");//Bu alıcı kişinin bilgilerini alıyor

        saat = new StringBuilder();
        tarih = new StringBuilder();

        Date bugun = Calendar.getInstance().getTime();
        SimpleDateFormat formatte = new SimpleDateFormat("dd.MM.yyyy");
        String date = formatte.format(bugun);
        tarih.append(date);

        Date saatzaman = Calendar.getInstance().getTime();
        SimpleDateFormat saatformat = new SimpleDateFormat("hh:mm");
        String saatt = saatformat.format(saatzaman);
        saat.append(saatt);

        recyclerView = findViewById(R.id.mesajrecyler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        chatAdapter = new ChatAdapter(getApplicationContext(),mMesajlar);
        recyclerView.setAdapter(chatAdapter);

        geritusu = findViewById(R.id.geritusu);
        profilresim = findViewById(R.id.profilresim);
        kullaniciadi = findViewById(R.id.kullaniciadmesaj);
        mesajgirdi = findViewById(R.id.mesajgirdialani);
        fotoekle = findViewById(R.id.fotoeklemesaj);
        gonder = findViewById(R.id.gonderbtn);


        geritusu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
        });

        fotoekle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        //.setAspectRatio(400,300)
                        .start(MessageActivity.this);
            }
        });

        kullaniciBilgisiAl();

    }
    //Sohbet ekranında kullanıcı bilgilerini okuyor
    private void kullaniciBilgisiAl() {
        //önceki sayfadan gelecek bilgiyi alıyor
        intent= getIntent();
        final String userId = intent.getStringExtra("userId");//Bu alıcı kişinin bilgilerini alıyor ->bilgiler önceki yerden intentle geliyor

        //alıcı kişinin sohbet edeceğimiz kişinin kullanıcı bilgilerini okuycaz
        FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Kullanici kullanici = snapshot.getValue(Kullanici.class);
                        kullaniciadi.setText(kullanici.getUsername());
                        Glide.with(getApplication()).load(kullanici.getImageUrl()).into(profilresim);
                        mesajOku(firebaseUser.getUid(),userId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        gonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mesajgirdisi = mesajgirdi.getText().toString();

                if(!mesajgirdisi.equals("")){
                    //userId -> sohbet edilcek alıcının kullanıcı İd'si 55. satırda alındı
                    MesajGonderen(firebaseUser.getUid(),userId,mesajgirdisi);
                    MesajAlan(firebaseUser.getUid(),userId,mesajgirdisi);

                }else{
                    //Buradaki ifadeyi sesli mesaj yapabilirim -> mesaj boşsa göndermiyor
                    Toast.makeText(MessageActivity.this, "Mesaj Kısmı Boş", Toast.LENGTH_SHORT).show();
                }
                mesajgirdi.setText("");
            }
        });
    }

    private void mesajOku(String benimid, String aliciId) {
        //Mesajı nerden okuyacak -> Mesajlardan Oku
        FirebaseDatabase.getInstance().getReference("Mesajlar").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mMesajlar.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            Chat chat = snapshot1.getValue(Chat.class);
                            chat.setMessageId(snapshot1.getKey());
                            if (chat.getAlici().equals(benimid) && chat.getGonderen().equals(aliciId) ||
                                    chat.getAlici().equals(aliciId) && chat.getGonderen().equals(benimid)) {
                                mMesajlar.add(chat);
                            }

                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


    private void MesajGonderen(String gonderen,String alici,String mesaj) {
        HashMap<String,Object> hashMap=new HashMap();
        hashMap.put("gonderen",gonderen);
        hashMap.put("alici",alici); //Buradaki değerlere dikkat etmek lazım alici ise alici şeklinde değişken oluşturmalıyız
        hashMap.put("mesaj",mesaj);
        hashMap.put("resim","");
        hashMap.put("goruldu",false);
        hashMap.put("saat",saat.toString());
        hashMap.put("tarih",tarih.toString());

        //Burada gönderen kullanıcının kendine ait bir alt düğümü olacak şekilde mesajları saklanır
        FirebaseDatabase.getInstance().getReference().child("Mesajlar").child(firebaseUser.getUid()).push().setValue(hashMap);

    }
    private void MesajAlan(String gonderen,String alici,String mesaj) {
        HashMap<String,Object> hashMap=new HashMap();
        hashMap.put("gonderen",gonderen);
        hashMap.put("alici",alici);
        hashMap.put("mesaj",mesaj);
        hashMap.put("resim","");
        hashMap.put("goruldu",false);
        hashMap.put("saat",saat.toString());
        hashMap.put("tarih",tarih.toString());

        //Burada alici kullanıcının kendine ait bir alt düğümü olacak şekilde mesajları saklanır
        FirebaseDatabase.getInstance().getReference().child("Mesajlar").child(alici).push().setValue(hashMap);

    }

    private void goruldu(){
        //Burada yapılan işlem sayfa üzerindeyse true değerin al onresume ile bu fonksiyon çalışıyor
        intent= getIntent();
        final String userId = intent.getStringExtra("userId");

        //kullanıcı mesaj activitynin üzerine geldiyse bunu yaz
        reference = FirebaseDatabase.getInstance().getReference("Mesajlar").child(userId);
        value= reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Chat chat = snapshot1.getValue(Chat.class);
                    if(chat.getAlici().equals(firebaseUser.getUid())){
                        HashMap<String,Object> hashMap=new HashMap();
                        hashMap.put("goruldu",true);
                        snapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        goruldu();
    }

    @Override
    protected void onPause() {
        //sayfa üzerinde değilse değeri iptal et ilk değer neyse o kalsın
        super.onPause();
        reference.removeEventListener(value);
    }


    private String dosyauzantisiAl(Uri uri) {
        //Dosya uzantısını uri şeklinde al
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void resimYukle(){

        resimyukleyolu = FirebaseStorage.getInstance().getReference("Mesaj Resimleri");

        if(resimUri != null){

            final StorageReference dosyayolu = resimyukleyolu.child(System.currentTimeMillis()+"."+dosyauzantisiAl(resimUri));

            yuklegorevi= dosyayolu.putFile(resimUri);
            yuklegorevi.addOnSuccessListener(taskSnapshot -> {
                        // Yükleme başarılıysa, URL'yi al
                        dosyayolu.getDownloadUrl().addOnSuccessListener(uri -> {
                            // URL'yi Realtime Database'e kaydet
                            myuri = uri.toString();

                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Mesajlar").child(userid);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Mesajlar").child(firebaseUser.getUid());
                            String gonderiid = reference1.push().getKey();
                            String gonderiid2 = reference2.push().getKey();

                            HashMap<String, Object> hashMap = new HashMap();
                            hashMap.put("gonderen", firebaseUser.getUid());
                            hashMap.put("alici", userid);
                            hashMap.put("mesaj", "");
                            hashMap.put("resim", myuri); // Yüklenen resmin URL'si buraya ekleniyor
                            hashMap.put("goruldu", false);
                            hashMap.put("saat", saat.toString());
                            hashMap.put("tarih", tarih.toString());

                            reference2.child(gonderiid).setValue(hashMap);
                            reference1.child(gonderiid).setValue(hashMap);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Yükleme başarısız olduğunda işlemleri burada ele al
                    });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            resimUri = result.getUri();
            resimYukle();
        }
    }
}