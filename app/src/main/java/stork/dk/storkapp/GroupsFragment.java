package stork.dk.storkapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import stork.dk.storkapp.utils.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import stork.dk.storkapp.communicationObjects.CommunicationErrorHandling;
import stork.dk.storkapp.communicationObjects.CommunicationsHandler;
import stork.dk.storkapp.communicationObjects.Constants;
import stork.dk.storkapp.communicationObjects.GroupsResponse;
import stork.dk.storkapp.friendsSpinner.Friend;
import stork.dk.storkapp.friendsSpinner.Group;


/**
 * @author Mathias, Morten
 */
public class GroupsFragment extends Fragment {
    private View rootView;
    private HashMap<String, List<String>> listDataChild;
    private ArrayList<String> listDataHeader;
    private ExpandableListAdapter adapter;
    private String sessionId;
    private int userId;
    private ExpandableListView groupList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        Bundle args = getArguments();
        userId = args.getInt(Constants.CURRENT_USER_KEY);
        sessionId = args.getString(Constants.CURRENT_SESSION_KEY);

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        groupList = rootView.findViewById(R.id.groupList);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        populateList();

    }

    public void populateList(){
        HashMap<String, String> params = new HashMap<>();
        params.put("sessionId", sessionId);
        params.put("userId", String.valueOf(userId));

        CommunicationsHandler.getGroups(params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                GroupsResponse resp = new Gson().fromJson(new String(responseBody), GroupsResponse.class);
                int i = 0;
                if(listDataHeader.isEmpty()) {
                    Collections.sort(resp.getGroups());
                    for (Group group : resp.getGroups()) {
                        listDataHeader.add(group.getName());

                        List<String> grp = new ArrayList<>();
                        for (Friend friend : group.getFriends()) {
                            grp.add(friend.getName());
                        }
                        listDataChild.put(listDataHeader.get(i), grp);
                        i++;
                    }
                }
                setAdapter();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (statusCode == 403) {
                    CommunicationErrorHandling.handle403(getActivity());
                }
            }
        });

        adapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

    }

    public void setAdapter(){
        adapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
        groupList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            populateList();
            adapter.notifyDataSetChanged();
        }
    }
}