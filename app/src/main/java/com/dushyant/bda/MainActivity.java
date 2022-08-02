package com.dushyant.bda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dushyant.bda.Adapter.UserAdapter;
import com.dushyant.bda.Model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UserAdapter.IListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView nav_view;

    private CircleImageView nav_profile_image;
    private TextView nav_fullname, nav_email, nav_bloodgroup, nav_type;


    private DatabaseReference userRef;

    private RecyclerView recyclerView;
    private ProgressBar progressbar;

    private List<User> userlist;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blood Donation App");
        drawerLayout = findViewById(R.id.drawerlayout);
        nav_view = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);

        progressbar=findViewById(R.id.progressbar);

        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        userlist=new ArrayList<>();
        userAdapter=new UserAdapter(MainActivity.this,userlist, this);

        recyclerView.setAdapter(userAdapter);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type=snapshot.child("type").getValue().toString();
                if(type.equals(Constants.DONOR)){
                    readRecipients();
                }
                else{
                    readDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        nav_profile_image = nav_view.getHeaderView(0).findViewById(R.id.nav_user_image);
        nav_fullname = nav_view.getHeaderView(0).findViewById(R.id.nav_user_fullname);
        nav_email = nav_view.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_bloodgroup = nav_view.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);
        nav_type = nav_view.getHeaderView(0).findViewById(R.id.nav_user_type);

        userRef = FirebaseDatabase.getInstance().getReference().child("users").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    nav_fullname.setText(name);

                    String email = snapshot.child("email").getValue().toString();
                    nav_email.setText(email);

                    String bloodgroup = snapshot.child("bloodgroup").getValue().toString();
                    nav_bloodgroup.setText(bloodgroup);

                    String type = snapshot.child("type").getValue().toString();
                    nav_type.setText(type);

                    if (snapshot.hasChild("profilepictureurl")) {
                        String imageurl = snapshot.child("profilepictureurl").getValue().toString();
                        Glide.with(getApplicationContext()).load(imageurl).into(nav_profile_image);
                    } else {
                        nav_profile_image.setImageResource(R.drawable.profile_image);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readDonors() {
        DatabaseReference refrence=FirebaseDatabase.getInstance().getReference().child("users");
        Query query=refrence.orderByChild("type").equalTo("donor");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user=dataSnapshot.getValue(User.class);
                    userlist.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if(userlist.isEmpty()){
                    Toast.makeText(MainActivity.this, "No Donors", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readRecipients() {
        DatabaseReference refrence=FirebaseDatabase.getInstance().getReference().child("users");
        Query query=refrence.orderByChild("type").equalTo("recipient");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user=dataSnapshot.getValue(User.class);
                    userlist.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if(userlist.isEmpty()){
                    Toast.makeText(MainActivity.this, "No Recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //switch (item.getItemId()) {
        //  case R.id.profile:
        //    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        //  startActivity(intent);
        //}
        int id=item.getItemId();
        if(id==R.id.profile){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
              startActivity(intent);
        }
        else if(id==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }
        else if(id==R.id.aplus){
            Intent intent3=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent3.putExtra("group","A+");
            startActivity(intent3);
        }
        else if(id==R.id.aminus){
            Intent intent4=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent4.putExtra("group","A-");
            startActivity(intent4);
        }
        else if(id==R.id.bplus){
            Intent intent5=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent5.putExtra("group","B+");
            startActivity(intent5);
        }
        else if(id==R.id.bminus){
            Intent intent6=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent6.putExtra("group","B-");
            startActivity(intent6);
        }
        else if(id==R.id.abplus){
            Intent intent7=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent7.putExtra("group","AB+");
            startActivity(intent7);
        }
        else if(id==R.id.abminus){
            Intent intent8=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent8.putExtra("group","AB-");
            startActivity(intent8);
        }
        else if(id==R.id.oplus){
            Intent intent9=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent9.putExtra("group","O+");
            startActivity(intent9);
        }
        else if(id==R.id.ominus){
            Intent intent10=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent10.putExtra("group","O-");
            startActivity(intent10);
        }
        else if(id==R.id.compatible){
            Intent intent11=new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent11.putExtra("group","Compatible with me");
            startActivity(intent11);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSendClick(String mail, String subject, String msg) {
        composeEmail(mail, subject, msg);
    }

    private void composeEmail(String mail, String subject, String msg) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"+mail)); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}