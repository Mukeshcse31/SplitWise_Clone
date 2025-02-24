package com.android.app.splitwise_clone.groups;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.splitwise_clone.R;
import com.android.app.splitwise_clone.model.Group;
import com.android.app.splitwise_clone.utils.AppUtils;
import com.android.app.splitwise_clone.utils.FirebaseUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.android.app.splitwise_clone.SummaryActivity;
import com.android.app.splitwise_clone.model.SingleBalance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AddGroup extends AppCompatActivity implements GroupMembersAdapter.OnClickListener,
        NonGroupMembersAdapter.OnClickListener {

    private AutoCompleteTextView mGroupName;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private static final int RC_PHOTO_PICKER = 2;
    public static String GROUP_NAME = "group_name";
    public static String GROUP_EDIt = "group_edit";
    public static String GROUP_ADDED = "group_added";
    public static String GROUP_EDITED = "group_edited";
    public static String GROUP_DELETED = "group_deleted";
    public static String ACTION_CANCEL = "ACTION_CANCEL";
    private static final String TAG = AddGroup.class.getSimpleName();
    private Uri selectedImageUri;
    private RecyclerView members_rv;
    private RecyclerView nonmembers_rv;
    private ImageView mPhotoPickerButton;
    TextView noFriend_tv, tv_members, tv_nonmembers;
    private String group_name, userName;
    private Group mGroup, grp;
    float friendsCount;
    int friendsCounter;
    private String photoUrl = "";
    Map<String, String> userFriends = new HashMap<>();
    private Map<String, String> group_members = new HashMap<>();
    private Map<String, String> nongroup_members = new HashMap<>();
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 201;
    String db_users, db_balances, db_groups, db_archivedExpenses, db_expenses, db_members, db_nonMembers,
            db_totalAmount, db_dateSpent, db_splitDues, db_images, db_category, db_owner, db_photoUrl, db_amount, db_status, db_friends, db_email, db_name, db_imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.new_group));

        initDBValues();
        mFirebaseStorage = AppUtils.getDBStorage();
        mPhotosStorageReference = mFirebaseStorage.getReference().child(db_images + "/" + db_groups);
        mDatabaseReference = AppUtils.getDBReference();
        mGroupName = findViewById(R.id.group_name);
        members_rv = findViewById(R.id.members_rv);
        nonmembers_rv = findViewById(R.id.nonmembers_rv);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        noFriend_tv = findViewById(R.id.noFriend_tv);
        tv_nonmembers = findViewById(R.id.tv_nonmembers);
        tv_members = findViewById(R.id.tv_members);


        userName = FirebaseUtils.getUserName();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        members_rv.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        nonmembers_rv.setLayoutManager(layoutManager1);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else
            setphotoClickListener();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(GROUP_NAME)) {
            group_name = bundle.getString(GROUP_NAME);
            getSupportActionBar().setTitle(group_name);
            if (bundle.containsKey(GROUP_EDIt)) {
                mGroup = bundle.getParcelable(GROUP_EDIt);
            }
        }
        membersViews();
    }


    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .asBitmap()
                    .placeholder(R.drawable.group)
                    .into(mPhotoPickerButton);
        }
    }

    private void setphotoClickListener() {
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check if the group Name is empty
                if (TextUtils.isEmpty(mGroupName.getText().toString())) {
                    mGroupName.setError(getString(R.string.groupname_warning));
                } else {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    setphotoClickListener();
                } else {
//                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                        }
                    }
                }
                break;
        }
    }

    private void membersViews() {
        final String path1 = getString(R.string.db_users);

        //get all the friends of the user
        Query query = mDatabaseReference.child(path1 + "/" + userName + "/" + db_friends);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userFriends = new HashMap<>();
                    userFriends.put(userName, "");
                    friendsCount = dataSnapshot.getChildrenCount() + 1;//include the user
                    for (DataSnapshot i : dataSnapshot.getChildren()) {
                        final String friend = i.getKey();
                        if (friend != null)
                            userFriends.put(friend, "");
                    }

                    Iterator it = userFriends.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        final String friend = (String) pair.getKey();
                        //Get the friends' email id
                        Query query = mDatabaseReference.child(path1 + "/" + friend + "/" + db_email);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                friendsCounter += 1;
                                if (dataSnapshot.exists()) {
                                    String email = (String) dataSnapshot.getValue();
                                    userFriends.put(friend, email);
                                    getGroupMembers();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                } else {
                    noFriend_tv.setVisibility(View.VISIBLE);

                    tv_members.setVisibility(View.GONE);
                    tv_nonmembers.setVisibility(View.GONE);
                    members_rv.setVisibility(View.GONE);
                    nonmembers_rv.setVisibility(View.GONE);
                    mPhotoPickerButton.setVisibility(View.GONE);
                    mGroupName.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getGroupMembers() {

        if (friendsCount == friendsCounter) {

            //for editing the existing group
            if (mGroup != null) {
                mGroupName.setText(mGroup.getName());
                Map<String, SingleBalance> members = mGroup.getMembers();
                Iterator it = members.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String name = (String) pair.getKey();
                    SingleBalance sb = (SingleBalance) pair.getValue();
                    String email = sb.getEmail();
                    group_members.put(name, email);
                }
                Map<String, SingleBalance> nonMembers = mGroup.getNonMembers();
                it = nonMembers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String name = (String) pair.getKey();
                    SingleBalance sb = (SingleBalance) pair.getValue();
                    String email = sb.getEmail();
                    nongroup_members.put(name, email);
                }


                //load image
                final StorageReference imageRef = mPhotosStorageReference.child(group_name);
                final long ONE_MEGABYTE = 1024 * 1024;
                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Glide.with(AddGroup.this)
                                .load(bytes)
                                .asBitmap()
                                .placeholder(R.drawable.group)
                                .into(mPhotoPickerButton);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        Log.i(TAG, exception.getMessage());
                        // Handle any errors
                    }
                });

            } else {//add the username by default
                group_members.put(userName, userFriends.get(userName));
                nongroup_members = new HashMap<>(userFriends);
                nongroup_members.remove(userName);
            }
            updateAdapters();

        }
    }

    private void updateAdapters() {
        GroupMembersAdapter mGroupMembersAdapter;
        NonGroupMembersAdapter mNonGroupMembersAdapter;
        mGroupMembersAdapter = new GroupMembersAdapter(group_members, AddGroup.this);
        members_rv.setAdapter(mGroupMembersAdapter);

        mNonGroupMembersAdapter = new NonGroupMembersAdapter(nongroup_members, AddGroup.this);
        nonmembers_rv.setAdapter(mNonGroupMembersAdapter);
    }

    @Override
    public void removeFriendFromGroup(final String name) {

        String friend = group_members.get(name);
        if (friend != null) {
            nongroup_members.put(name, friend);
            group_members.remove(name);
            updateAdapters();
        }
    }

    @Override
    public void addFriendToGroup(final String name) {

        String friend = nongroup_members.get(name);
        if (friend != null) {
            group_members.put(name, friend);
            nongroup_members.remove(name);
            updateAdapters();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mnu_add_group, menu);

        AppUtils.hideOption(menu, new int[]{R.id.deleteGroup, R.id.saveGroup});
        if (mGroup == null) {
            AppUtils.showMenuOption(menu, new int[]{R.id.addGroup});
        } else {
            AppUtils.hideOption(menu, new int[]{R.id.addGroup});
            if (TextUtils.equals(mGroup.getOwner(), userName)) {//only the group owner should be allowed to edit
                AppUtils.showMenuOption(menu, new int[]{R.id.deleteGroup, R.id.saveGroup});
            }
        }
        return true;
    }

    private void gotoSummaryActivity(String name, String value) {

        final Intent intent = new Intent(AddGroup.this, SummaryActivity.class);
        intent.putExtra(GROUP_NAME, group_name);
        intent.putExtra(name, value);
        startActivity(intent);
        finish();
    }

    private boolean checkGroup(String name) {

        boolean cancel = false;

        //check the fields
        if (!AppUtils.checkGroupName(name)) {
            mGroupName.setError(getString(R.string.error_username));
            mGroupName.requestFocus();
            cancel = true;
        }

        if (group_members.size() == 0) {
            cancel = true;
//                Toast.makeText(this,getString(R.string.error_add_member), Toast.LENGTH_LONG).show();
            AppUtils.showSnackBar(this, findViewById(android.R.id.content), getString(R.string.error_add_member));
        }

        //check if the owner is a member
        if (!group_members.containsKey(userName)) {
            cancel = true;
//                Toast.makeText(this, getString(R.string.error_add_owner), Toast.LENGTH_LONG).show();
            AppUtils.showSnackBar(this, findViewById(android.R.id.content), getString(R.string.error_add_owner));
        }
        return cancel;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final String name = mGroupName.getText().toString().trim();

        switch (item.getItemId()) {

            case R.id.addGroup:

                if (!checkGroup(name)) {

                    Query query = mDatabaseReference.child(db_groups).orderByKey().startAt(name).endAt(name);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mGroupName.setError(getString(R.string.group_exists));
                                Toast.makeText(AddGroup.this, getString(R.string.group_exists), Toast.LENGTH_LONG).show();

                            } else {
                                grp = new Group(name);

//                          // add all the group members to the group
                                Iterator it = group_members.entrySet().iterator();
                                while (it.hasNext()) {

                                    Map.Entry pair = (Map.Entry) it.next();
                                    String groupMemberName = (String) pair.getKey();
                                    String email = (String) pair.getValue();
                                    SingleBalance sb = new SingleBalance(groupMemberName);
                                    sb.setEmail(email);
                                    grp.addMember(groupMemberName, sb);

                                }

                                // add all the non group members to the group
                                it = nongroup_members.entrySet().iterator();
                                while (it.hasNext()) {

                                    Map.Entry pair = (Map.Entry) it.next();
                                    String groupMemberName = (String) pair.getKey();
                                    String email = (String) pair.getValue();
                                    SingleBalance sb = new SingleBalance(groupMemberName);
                                    sb.setEmail(email);
                                    grp.addNonMember(groupMemberName, sb);

                                }

                                grp.setOwner(userName);
                                mDatabaseReference.child(db_groups + "/" + name).setValue(grp, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference dataReference) {

                                        //store image in the Storage
                                        if (!(selectedImageUri == null)) {
                                            //add the group creator as a member when the group is created
                                            grp.setPhotoUrl("/" + db_images + "/" + db_groups + "/" + name);
                                            final StorageReference photoRef = mPhotosStorageReference.child(name);

                                            // Upload file to Firebase Storage
                                            photoRef.putFile(selectedImageUri)
                                                    .addOnSuccessListener(AddGroup.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                            gotoSummaryActivity(GROUP_ADDED, String.format("%s %s", getString(R.string.group_added), name));

                                                            Log.i(TAG, "image uploaded");
                                                        }
                                                    });
                                        } else
                                            gotoSummaryActivity(GROUP_ADDED, String.format("%s %s", getString(R.string.group_added), name));
                                    }
                                });
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                break;

            case R.id.saveGroup:
                if (!checkGroup(name)) {
                    Group newGroup = new Group();
                    try {
                        newGroup = (Group) mGroup.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    //expenses & archived expenses are just cloned
                    //only name, members, image can be edited and are checked below
                    final String newGroupNname = mGroupName.getText().toString().trim();
                    newGroup.setName(newGroupNname);

                    // add all the new group members to the groupmembers of group
                    Iterator it = group_members.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        final String groupMemberName = (String) pair.getKey();
                        String email = (String) pair.getValue();

                        //if the member is not found in the prev groupmembers, then get it from non-group members
                        if (!mGroup.getMembers().containsKey(groupMemberName)) {

                            newGroup.getNonMembers().remove(groupMemberName);
                            if (mGroup.getNonMembers().containsKey(groupMemberName)) {

//                                //for newly added member init split dues
//                                Map<String, Float> spl = mGroup.getNonMembers().get(groupMemberName).getSplitDues();
//                                if(spl ==null)
//                                    mGroup.getNonMembers().get(groupMemberName).addMemberBalance(userName);

                                newGroup.addMember(groupMemberName, mGroup.getNonMembers().get(groupMemberName));

                            } else { //this won't happen
                                SingleBalance sb = new SingleBalance(groupMemberName);
                                sb.setEmail(email);
                                newGroup.addMember(groupMemberName, sb);
                            }
                        }
                    }

                    // add all the non group members to the non-groupmembers of group and remove from groupmembers
                    it = nongroup_members.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        String nonGroupMemberName = (String) pair.getKey();
                        String email = (String) pair.getValue();

                        if (!mGroup.getNonMembers().containsKey(nonGroupMemberName)) {
                            newGroup.getMembers().remove(nonGroupMemberName);
                            if (mGroup.getMembers().containsKey(nonGroupMemberName)) {
                                newGroup.addNonMember(nonGroupMemberName, mGroup.getMembers().get(nonGroupMemberName));
                            } else {
                                SingleBalance sb = new SingleBalance(nonGroupMemberName);
                                sb.setEmail(email);
                                newGroup.getNonMembers().put(nonGroupMemberName, sb);
                            }
                        }
                    }


                    final String prev_imageName = mGroup.getName();

                    //Add the clone of the old group with the updated values
                    mDatabaseReference.child(db_groups + "/" + newGroupNname).setValue(newGroup, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference dataReference) {
                        }
                    });

                    //delete the old group object
                    if (!TextUtils.isEmpty(prev_imageName) && !TextUtils.equals(prev_imageName, newGroupNname)) {
                        mDatabaseReference.child(db_groups + "/" + prev_imageName).removeValue();
                    }

                    if (!(selectedImageUri == null)) {
                        //image changed
                        newGroup.setPhotoUrl("/" + db_images + "/" + db_groups + "/" + newGroupNname);
                        // Get a reference to store file at chat_photos/<FILENAME>
                        final StorageReference photoRef = mPhotosStorageReference.child(newGroupNname);

                        // Upload file to Firebase Storage
                        photoRef.putFile(selectedImageUri)
                                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        if (!TextUtils.equals(prev_imageName, newGroupNname))
                                            mPhotosStorageReference.child(prev_imageName).delete();
                                        gotoSummaryActivity(GROUP_EDITED, String.format("%s %s", getString(R.string.saved_group), newGroupNname));
                                        //TODO exit here
                                    }
                                });
                    } else {//image not changed
                        if (!TextUtils.equals(prev_imageName, newGroupNname)) {//only for a new name
                            final StorageReference imageRef = mPhotosStorageReference.child(prev_imageName);
                            final long ONE_MEGABYTE = 1024 * 1024;
                            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    final StorageReference photoRef = mPhotosStorageReference.child(newGroupNname);

                                    // Upload file to Firebase Storage
                                    photoRef.putBytes(bytes)
                                            .addOnSuccessListener(AddGroup.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    mPhotosStorageReference.child(prev_imageName).delete();
                                                    gotoSummaryActivity(GROUP_EDITED, String.format("%s %s", getString(R.string.saved_group), newGroupNname));
                                                    Log.i(TAG, "new Image uploaded");
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                    gotoSummaryActivity(GROUP_EDITED, String.format("%s %s", getString(R.string.saved_group), newGroupNname));
                                    Log.i(TAG, exception.getMessage());
                                    // Handle any errors
                                }
                            });
                        } else
                            gotoSummaryActivity(GROUP_EDITED, String.format("%s %s", getString(R.string.saved_group), newGroupNname));
                    }
                }
                break;


            case R.id.deleteGroup:

                //code to delete the group
                //https://www.tutorialspoint.com/android/android_alert_dialoges.htm
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(getString(R.string.group_delete_warning));
                alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mDatabaseReference.child(db_groups + "/" + group_name).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        gotoSummaryActivity(GROUP_DELETED, String.format("%s %s", getString(R.string.group_deleted), group_name));
                                    }
                                });

                            }
                        });

                alertDialogBuilder.setNegativeButton(getString(R.string.no), null);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;

            case R.id.gotoGroupList:
                gotoSummaryActivity(ACTION_CANCEL, "");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {

        bundle.putString(getResources().getString(R.string.group_name), group_name);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabaseReference = AppUtils.getDBReference();
        mFirebaseStorage = AppUtils.getDBStorage();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "listener cleared");
        AppUtils.closeDBReference(mDatabaseReference);
        mFirebaseStorage = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        AppUtils.closeDBReference(mDatabaseReference);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "config changed");

    }

    private void initDBValues() {
        db_users = getString(R.string.db_users);
        db_balances = getString(R.string.db_balances);
        db_groups = getString(R.string.db_groups);
        db_archivedExpenses = getString(R.string.db_archivedExpenses);
        db_expenses = getString(R.string.db_expenses);
        db_members = getString(R.string.db_members);
        db_nonMembers = getString(R.string.db_nonMembers);
        db_owner = getString(R.string.db_owner);
        db_photoUrl = getString(R.string.db_photoUrl);
        db_amount = getString(R.string.db_amount);
        db_status = getString(R.string.db_status);
        db_friends = getString(R.string.db_friends);
        db_email = getString(R.string.db_email);
        db_name = getString(R.string.db_name);
        db_imageUrl = getString(R.string.db_imageUrl);
        db_totalAmount = getString(R.string.db_totalAmount);
        db_dateSpent = getString(R.string.db_dateSpent);
        db_category = getString(R.string.db_category);
        db_splitDues = getString(R.string.db_splitDues);
        db_images = getString(R.string.db_images);
        db_email = getString(R.string.db_email);

    }
}
