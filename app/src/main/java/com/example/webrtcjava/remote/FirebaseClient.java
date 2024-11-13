package com.example.webrtcjava.remote;

import androidx.annotation.NonNull;

import com.example.webrtcjava.utils.DataModel;
import com.example.webrtcjava.utils.ErrorCallback;
import com.example.webrtcjava.utils.NewEventCallback;
import com.example.webrtcjava.utils.SuccessCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;

public class FirebaseClient {
    private final Gson gson=new Gson();
    private final DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference();
    private String currentUsername;
    private static final String LATEST_EVENT_FIELD_NAME="lastest_event";



    public void login(String username, SuccessCallback callback){
        dbRef.child(username).setValue("").addOnCompleteListener(task -> {
           currentUsername = username;
           callback.onSuccess();
        });

    }

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallback errorCallback){
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(dataModel.getTarget()).exists()){
                    dbRef.child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(gson.toJson(dataModel));
                }else {
                    errorCallback.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCallback.onError();
            }
        });
    }

    public void observeIncomingLatestEvent(NewEventCallback callback){
       dbRef.child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(
               new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       try{
                           String data= Objects.requireNonNull(snapshot.getValue()).toString();
                           DataModel dataModel = gson.fromJson(data,DataModel.class);
                           callback.onNewEventReceived(dataModel);
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               }
       );
    }
}
