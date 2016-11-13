package com.buradd.cloud7;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.buradd.cloud7.net.ConnectionParams;
import com.buradd.cloud7.net.FTPSingleFileTransferTask;
import com.buradd.cloud7.net.Transfer;
import com.buradd.cloud7.net.TransferDirection;
import com.buradd.cloud7.net.TransferTask;
import com.buradd.cloud7.net.TransferTaskProgressListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Filenames.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Filenames#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Filenames extends Fragment implements TransferTaskProgressListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static ArrayList<String> fileList = new ArrayList<>();
    private Transfer FileTransfer;

    private ListView theListView;

    // TODO: Rename and change types of parameters
    private String mUser;
    private String mPass;

    private MainActivity mainActivity = MainActivity.getInstance();

    private OnFragmentInteractionListener mListener;

    public Filenames() {
        // Required empty public constructor
    }

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_USER_PASS = "user_pass";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Filenames.
     */
    // TODO: Rename and change types and number of parameters
    public static Filenames newInstance(String aUser, String aPass) {
        Filenames fragment = new Filenames();
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, aUser);
        args.putString(ARG_USER_PASS, aPass);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        theListView = (ListView) view.findViewById(R.id.lvList);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, fileList);
        theListView.setAdapter(adapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar.make(view, "Starting download: " + theListView.getAdapter().getItem(i).toString(), Snackbar.LENGTH_LONG).show();
                final File file = new File(theListView.getAdapter().getItem(i).toString());
                FileTransfer = new Transfer(1);
                FileTransfer.setDestinationPath(Environment.getExternalStorageDirectory() + "/Cloud7");
                FileTransfer.setName(file.getName());
                FileTransfer.setSourcePath("/bcloud");
                FileTransfer.setDirection(TransferDirection.DOWNLOAD);
                TransferTask downLoad = new FTPSingleFileTransferTask(mainActivity, Filenames.this, Collections.singletonList(FileTransfer), getConnectionParams());
                downLoad.execute();
            }
        });

    }

    public ConnectionParams getConnectionParams(){
        final ConnectionParams connectionParams = new ConnectionParams();
        connectionParams.host = "ftp.buradd.com";
        connectionParams.username = mUser;
        connectionParams.password = mPass;
        return connectionParams;
    }

    @Override
    public void onDestroy() {
        fileList.clear();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getString(ARG_USER_NAME);
            mPass = getArguments().getString(ARG_USER_PASS);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filenames, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onBeginTransferTask(TransferTask task) {

    }

    @Override
    public void onBeginTransfer(TransferTask task, int transferId) {
        mainActivity.setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onProgressUpdate(TransferTask task, int transferId, int aProgress) {
        mainActivity.setProgress(aProgress);
    }

    @Override
    public void onEndTransfer(TransferTask task, int transferId) {
        mainActivity.setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onTransferFailed(TransferTask task, int transferId, Exception aException) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
