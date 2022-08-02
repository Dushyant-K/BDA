package com.dushyant.bda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.dushyant.bda.Adapter.UserAdapter;
import com.dushyant.bda.Model.User;

import java.util.ArrayList;
import java.util.List;

public class SentEmailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    List<String> idList;
    List<User> userList;
    UserAdapter userAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sent_email);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("People Sent Emails");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        userList=new ArrayList<>();
        userAdapter=new UserAdapter(SentEmailActivity.this,userList, null);
        recyclerView.setAdapter(userAdapter);
    }
}