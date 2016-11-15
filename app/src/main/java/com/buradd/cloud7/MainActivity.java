package com.buradd.cloud7;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.buradd.cloud7.net.ConnectionParams;
import com.buradd.cloud7.net.FTPSingleFileTransferTask;
import com.buradd.cloud7.net.RefreshLists;
import com.buradd.cloud7.net.Transfer;
import com.buradd.cloud7.net.TransferDirection;
import com.buradd.cloud7.net.TransferTask;
import com.buradd.cloud7.net.TransferTaskProgressListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;

import static com.buradd.cloud7.Filenames.fileList;
import static com.buradd.cloud7.Filenames.imageList;
import static com.buradd.cloud7.Filenames.videoList;

public class MainActivity extends AppCompatActivity implements Filenames.OnFragmentInteractionListener, TransferTaskProgressListener {

    public static String LOGIN_USER_NAME = "com.buradd.cloud7.LOGIN_USER_NAME";
    public static String LOGIN_USER_PASS = "com.buradd.cloud7.LOGIN_USER_PASS";
    ProgressDialog mDownloadProgress;
    private String mUser;
    private String mPass;
    public Transfer FileTransfer;
    private AdView mAdView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private static MainActivity _instance = null;

    public static MainActivity getInstance(){
        return _instance;
    }

    @Override
    protected void onDestroy() {
        fileList.clear();
        imageList.clear();
        videoList.clear();
        super.onDestroy();
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mUser = getIntent().getStringExtra(LOGIN_USER_NAME);
        mPass = getIntent().getStringExtra(LOGIN_USER_PASS);
        _instance = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               final View theView = view;

                SimpleFileDialog sfd = new SimpleFileDialog(MainActivity.this, "FileOpen..", new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(chosenDir.substring(chosenDir.lastIndexOf(".")+1));
                        final File file = new File(chosenDir);
                        FileTransfer = new Transfer(1);
                        String filename = chosenDir.substring(chosenDir.lastIndexOf("/"));
                        String sourcepath = chosenDir.replaceAll(filename, "");
                        FileTransfer.setSourcePath(sourcepath);
                        FileTransfer.setDestinationPath("");
                        FileTransfer.setName(file.getName());
                        FileTransfer.setDirection(TransferDirection.UPLOAD);
                        TransferTask upLoad = new FTPSingleFileTransferTask(MainActivity.this, MainActivity.this, Collections.singletonList(FileTransfer), getConnectionParams());
                        upLoad.execute();

                    }
                });
                sfd.default_file_name = "";
                sfd.chooseFile_or_Dir();
            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LoginAct", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LoginAct", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    public void refreshLocalLists(){

        mViewPager.getAdapter().notifyDataSetChanged();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void refreshRemoteLists(){
        RefreshLists refreshLists = new RefreshLists();
        refreshLists.execute(mUser, mPass);
    }


    public ConnectionParams getConnectionParams(){
        final ConnectionParams connectionParams = new ConnectionParams();
        connectionParams.host = "ftp.buradd.com";
        connectionParams.username = mUser;
        connectionParams.password = mPass;
        return connectionParams;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
    //    if (id == R.id.action_settings) {
     //       startActivity(new Intent(this, SettingsActivity.class));
      //  }
        switch(id){
            case R.id.change_email:

                break;
            case R.id.change_password:
                Snackbar.make(findViewById(android.R.id.content), "Password reset email has been sent.", Snackbar.LENGTH_LONG).setAction("OKAY", null).show();
                mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail());
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBeginTransferTask(TransferTask task) {

    }

    @Override
    public void onBeginTransfer(TransferTask task, int transferId) {
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onProgressUpdate(TransferTask task, int transferId, int aProgress) {
        setProgress(aProgress);
    }

    @Override
    public void onEndTransfer(TransferTask task, int transferId) {
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onTransferFailed(TransferTask task, int transferId, Exception aException) {

    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position){
                case 0:
                    return Filenames.newInstance(mUser, mPass, 0);
                case 1:
                    return Filenames.newInstance(mUser, mPass, 1);
                case 2:
                    return Filenames.newInstance(mUser, mPass, 2);
            }
         return null;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "FILES";
                case 1:
                    return "IMAGES";
                case 2:
                    return "VIDEOS";
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public void showDownloadProgressDialog()
    {
        if(mDownloadProgress == null)
        {
            mDownloadProgress = new ProgressDialog(this);
            mDownloadProgress.setCancelable(false);
            mDownloadProgress.setIndeterminate(false);
            mDownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDownloadProgress.setMessage("");
            mDownloadProgress.getWindow().setGravity(Gravity.BOTTOM);
            mDownloadProgress.setMax(100);
            mDownloadProgress.setProgress(0);
        }
        if(!mDownloadProgress.isShowing()) {
            mDownloadProgress.show();
        }
    }

    /**
     * hideDownloadProgressDialog
     *
     * hide the download progress dialog
     */
    public void hideDownloadProgressDialog()
    {
        if(mDownloadProgress != null)
        {
            mDownloadProgress.dismiss();
        }
        mDownloadProgress = null;
    }

    /**
     * updateDownloadProgress
     *
     * change the progress Dialog status meg
     *
     * @param msg
     */
    public void updateDownloadProgress(final String msg, final int progress)
    {
        if(mDownloadProgress == null || !mDownloadProgress.isShowing())
        {
            return;
        }
        mDownloadProgress.setMessage(msg);
        mDownloadProgress.setProgress(progress);
    }
}
