package com.alialtunoglu.dealchat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alialtunoglu.dealchat.MainActivity;
import com.alialtunoglu.dealchat.Model.Comment;
import com.alialtunoglu.dealchat.Model.Kullanici;
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

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComment;
    private FirebaseUser firebaseUser;
    private String postid;
    private String postPublisherId;

    public CommentAdapter(Context mContext, List<Comment> mComment,String postid,String postPublisherId) {
        this.postid=postid;
        this.postPublisherId=postPublisherId;
        this.mContext = mContext;
        this.mComment = mComment;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item,parent,false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = mComment.get(position);

        holder.comment.setText(comment.getComment());

        kullaniciBilgileriGetir(holder.image_profile,holder.username,comment.getPublisher());


        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid",comment.getPublisher());
                mContext.startActivity(intent);

            }
        });
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid",comment.getPublisher());
                mContext.startActivity(intent);

            }
        });
        //Yoruma uzun tıklayınca ne olması gerektiğini söylüyor
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(comment.getPublisher().equals(firebaseUser.getUid())){
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Silmek istiyor musun ? ");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Hayır", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference("Comments").child(postid).child(comment.getCommentid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            removeNotification(postPublisherId,postid);
                                            Toast.makeText(mContext, "Silindi!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                return  true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile;
        public TextView username,comment;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile= itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);

        }
    }

    private void removeNotification(String userid, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        Query query = reference.orderByChild("postid").equalTo(postid);
        Log.d("Userid",userid+" == "+firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("Userid",userid+" == "+firebaseUser.getUid());
                    // Kullanıcı ID'sini ve iscomment değerini kontrol et
                    if (snapshot.child("userid").getValue(String.class).equals(firebaseUser.getUid()) &&
                            snapshot.child("iscomment").getValue(Boolean.class)) {
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

    private void kullaniciBilgileriGetir(ImageView imageView,TextView username, String publisherid){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Kullanıcılar").child(publisherid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Kullanici kullanici = snapshot.getValue(Kullanici.class);
                Glide.with(mContext.getApplicationContext()).load(kullanici.getImageUrl()).into(imageView);
                System.out.println(kullanici.getUsername());
                username.setText(kullanici.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
