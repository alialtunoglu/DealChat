package com.alialtunoglu.dealchat.Adapter;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.alialtunoglu.dealchat.Model.Chat;

import com.alialtunoglu.dealchat.R;
import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int Mesaj_Sag = 0;
    public static final int Mesaj_Sol = 1;

    private Context mcontext;
    private List <Chat> mMesajlar;

    int mesajkonumu=-1;


    private FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();


    public ChatAdapter(Context mcontext, List<Chat> mMesajlar){
        this.mcontext=mcontext;
        this.mMesajlar=mMesajlar;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == Mesaj_Sag){
            //sağ tasarımı gösterecek
            View view = LayoutInflater.from(mcontext).inflate(R.layout.sag,parent,false);
            return new ChatAdapter.ViewHolder(view);
        }else{
            //sol tasarımı getircek
            View view = LayoutInflater.from(mcontext).inflate(R.layout.sol,parent,false);
            return new ChatAdapter.ViewHolder(view);
        }

    }

    //Kullanıcılar onBindViewHolder içerisinden okunacak
    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        //Model içindeki kullanici girilen verileri tutar.
        final Chat chat = mMesajlar.get(position);

        //Mesaj pozisyonu -1 deyse diğerlerini gizliyor diğerlerindeki görüldü ibaresini kaldırıyor
        if(position == mMesajlar.size()-1){
            if(chat.getGoruldu()){
                holder.goruldu.setVisibility(View.VISIBLE);
                holder.gorulmedi.setVisibility(View.GONE);
            }else{
                holder.gorulmedi.setVisibility(View.VISIBLE);
                holder.goruldu.setVisibility(View.GONE);
            }
        }else{
            holder.goruldu.setVisibility(View.GONE);
        }

        holder.mesaj.setText(chat.getMesaj());
        holder.saat.setText(chat.getSaat());
        holder.tarih.setText(chat.getTarih());

        Glide.with(mcontext).load(chat.getResim()).into(holder.mesajresim);

        if(chat.getResim().equals("")){
            holder.mesajresim.setVisibility(View.GONE);
        }


    }
    private void mesajSil(int position) {
        String messageId = mMesajlar.get(position).getMessageId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Mesajlar").child(firebaseUser.getUid());
        reference.child(messageId).setValue(null);
        mesajkonumu = -1;

        notifyDataSetChanged();


    }

    @Override
    public int getItemCount() {
        return mMesajlar.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView goruldu,gorulmedi,mesajresim,sil,copy;
        public TextView mesaj,tarih,saat;
        public CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mesaj = itemView.findViewById(R.id.mesaj);
            tarih= itemView.findViewById(R.id.tarih);
            goruldu = itemView.findViewById(R.id.goruldu);
            gorulmedi = itemView.findViewById(R.id.gorulmedi);
            saat = itemView.findViewById(R.id.saat);
            mesajresim = itemView.findViewById(R.id.mesajresim);
            sil = itemView.findViewById(R.id.sil);
            card = itemView.findViewById(R.id.card);
            copy = itemView.findViewById(R.id.copy);


            final int defaultBackgroundColor = card.getCardBackgroundColor().getDefaultColor();
            System.out.println(defaultBackgroundColor);

            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    card.setCardBackgroundColor(Color.parseColor("#b078ff"));
                    sil.setVisibility(View.VISIBLE);
                    copy.setVisibility(View.VISIBLE);


                    return true;
                }
            });


            sil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mesajSil(getAdapterPosition());

                    card.setCardBackgroundColor(defaultBackgroundColor);

                    sil.setVisibility(View.GONE);
                    copy.setVisibility(View.GONE);
                }
            });

            // Set click listener for 'copy' button
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Copy the message to clipboard
                    ClipboardManager clipboardManager = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("", mesaj.getText());
                    clipboardManager.setPrimaryClip(clipData);

                    // Show a toast message
                    Toast.makeText(itemView.getContext(), "kopyalandı", Toast.LENGTH_SHORT).show();
                    card.setCardBackgroundColor(defaultBackgroundColor);
                    sil.setVisibility(View.GONE);
                    copy.setVisibility(View.GONE);
                }
            });

        }
    }


    @Override
    public int getItemViewType(int position) {
        //gonderenMesaj benim id'me eşitse mesajı sağ yap
        if(mMesajlar.get(position).getGonderen().equals(firebaseUser.getUid())){
            return Mesaj_Sag;
        }
        else {
            return Mesaj_Sol;
        }
    }


}
