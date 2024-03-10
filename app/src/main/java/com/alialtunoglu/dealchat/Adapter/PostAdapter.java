package com.alialtunoglu.dealchat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alialtunoglu.dealchat.CommentsActivity;
import com.alialtunoglu.dealchat.Fragment.FollowersFragment;
import com.alialtunoglu.dealchat.Fragment.LikesFragment;
import com.alialtunoglu.dealchat.Fragment.PostDetailFragment;
import com.alialtunoglu.dealchat.Fragment.ProfileFragment;
import com.alialtunoglu.dealchat.Model.Comment;
import com.alialtunoglu.dealchat.Model.Kullanici;
import com.alialtunoglu.dealchat.Model.Post;
import com.alialtunoglu.dealchat.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;
    private FirebaseUser firebaseUser;


    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        Post post = mPost.get(position);

        Glide.with(mContext).load(post.getPostimage()).into(holder.post_image);

        if(post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        }else{
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        System.out.println(post.getPublisher());
        publisherInfo(holder.image_profile,holder.username,holder.publisher,post.getPublisher());
        isLiked(post.getPostid(),holder.like);
        nrLikes(holder.likes,post.getPostid());
        getComments(post.getPostid(),holder.comments);
        isSaved(post.getPostid(),holder.save);

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,
                        new ProfileFragment()).addToBackStack(null).commit();

            }
        });

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostid());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,
                        new LikesFragment()).addToBackStack(null).commit();

            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,
                        new ProfileFragment()).addToBackStack(null).commit();

            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostid());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,
                        new PostDetailFragment()).commit();

            }
        });
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });


        holder.like.setOnClickListener(new View.OnClickListener() {
            String notificationId; // Bildirim ID'sini saklamak için
            @Override
            public void onClick(View v) {
                //eğer like ifadesinin tagı like ise işlemleri yapıyoruz
                if(holder.like.getTag().equals("like")){
                    //like butonuna tıklandığı an o anki kullanıcının id'sini giriyor ve true yapıyor
                    // Beğenme işlemi
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    //beğendiğinde bildirim ekliyor
                     addNotifications(post.getPublisher(),post.getPostid());
                }else{
                    // Beğeni iptali işlemi
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                    // Beğeni iptal edildiğinde ilgili bildirimi kaldır

                    removeNotification(post.getPublisher(), post.getPostid());

                }
            }
        });

        //yorum resmine tıkladığımda
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        //Yorumları göster yazısına tıkladığımızda
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext,v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.edit){
                            editPost(post.getPostid());
                            return true;
                        }else if(item.getItemId()==R.id.delete){
                            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentacilacagicerceve,
                                    new ProfileFragment()).commit();
                            FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostid()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(mContext, "Silindi!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            return true;
                        }else if (item.getItemId()==R.id.report){
                            Toast.makeText(mContext, "Şikayet edildi", Toast.LENGTH_SHORT).show();
                            return true;
                        }else{
                            return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                //Eğer kullanıcı ben değilsem düzenleme ve silme kısımlarını kapat
                if(!post.getPublisher().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }else if(post.getPublisher().equals(firebaseUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.report).setVisible(false);
                }
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile,post_image,like,comment,save,more;
        public TextView username,likes,publisher,description,comments;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile= itemView.findViewById(R.id.image_profile);
            post_image= itemView.findViewById(R.id.post_image);
            like= itemView.findViewById(R.id.like);
            comment= itemView.findViewById(R.id.comment);
            save= itemView.findViewById(R.id.save);
            username=itemView.findViewById(R.id.username);
            likes=itemView.findViewById(R.id.likes);
            publisher=itemView.findViewById(R.id.publisher);
            description=itemView.findViewById(R.id.description);
            comments=itemView.findViewById(R.id.comments);
            more=itemView.findViewById(R.id.more);


        }
    }
    //Yorum sayısını gösterir
    private void getComments(String postid,final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText(snapshot.getChildrenCount()+" Yorumun tümünü gör");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //isLiked metodu çağrılarak kullanıcının gönderiyi beğenip beğenmediği kontrol edilir. Bu duruma göre like butonunun görüntüsü ve etiketi değiştirilir.
    //Beğenildi mi kontrolü ?
    private void isLiked(String postid,ImageView imageView){
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid); //Likes altında post id ve onun altında beğenen kişinin id'si olacak
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser1.getUid()).exists()){ //eğer bir uid varsa liked yapıyor ifadeyi
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    //yoksa da tagi değiştiriyor
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications(String userid,String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        String notificationId = reference.push().getKey(); // Benzersiz bir anahtar oluştur

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","gönderini beğendi");
        hashMap.put("postid",postid);
        hashMap.put("ispost",true);
        hashMap.put("iscomment",false);
        hashMap.put("isfollow",false);

        reference.push().setValue(hashMap);
    }
    //Bildirim silme işlemi burada postid ve userid eşleşince o bildirimi siliyor
    private void removeNotification(String userid, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        Log.d("Userid",userid+" == "+firebaseUser.getUid());
        Query query = reference.orderByChild("postid").equalTo(postid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Kullanıcı ID'sini ve ispost değerini kontrol et
                    if (snapshot.child("userid").getValue(String.class).equals(firebaseUser.getUid()) &&
                            snapshot.child("ispost").getValue(Boolean.class)) {
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

    //Post altındaki beğeni sayısını alır ve kaç tane olduğunu yazdırır
    //Beğeni sayısını gösterir
    private void nrLikes(TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount()+" beğenme");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Fotoyu paylaşan kişi bilgileri düzenleme
    private void publisherInfo(final ImageView image_profile, final TextView username,final TextView publisher,final String userid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Kullanici kullanici = snapshot.getValue(Kullanici.class);
                System.out.println(kullanici.getImageUrl());
                System.out.println(kullanici.getUsername());
                System.out.println(kullanici.getFullname());

                Glide.with(mContext).load(kullanici.getImageUrl()).into(image_profile);
                username.setText(kullanici.getUsername());
                publisher.setText(kullanici.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isSaved(final String postid,ImageView imageView){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference =FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_savee_black);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void editPost(String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Gönderi Düzenle");

        EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams Ip = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(Ip);
        alertDialog.setView(editText);

        getText(postid,editText);

        alertDialog.setPositiveButton("Düzenle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap <String,Object> hashMap = new HashMap<>();
                hashMap.put("description",editText.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts").child(postid).updateChildren(hashMap);
            }
        });
        alertDialog.setNegativeButton("İptal et", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
    private void getText(String postid,final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
