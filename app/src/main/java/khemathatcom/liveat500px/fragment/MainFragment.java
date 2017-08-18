package khemathatcom.liveat500px.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import khemathatcom.liveat500px.R;
import khemathatcom.liveat500px.adapter.PhotoListAdapter;
import khemathatcom.liveat500px.dao.PhotoItemCollectionDao;
import khemathatcom.liveat500px.datatype.MutableInteger;
import khemathatcom.liveat500px.manager.HttpManager;
import khemathatcom.liveat500px.manager.PhotoListManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by nuuneoi on 11/16/2014.
 */
public class MainFragment extends Fragment {

    /*************
     * Variables
     *************/
    ListView listview;
    PhotoListAdapter listAdapter;
    PhotoListManager photoListManager;
    Button btnNewPhotos;

    SwipeRefreshLayout swipeRefreshLayout;

    MutableInteger lastPositionInteger;

    /*************
     * Functions
     *************/

    public MainFragment() {
        super();
    }


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(savedInstanceState);



        if (savedInstanceState != null)
            restoreInstanceState(savedInstanceState); //Restore Instance


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initInstances(rootView , savedInstanceState);
        return rootView;
    }


    private void init(Bundle savedInstanceState) {
        //Initialize Fregment level's variables
        photoListManager = new PhotoListManager();

        lastPositionInteger = new MutableInteger(-1);

    }

    private void initInstances(View rootView , Bundle savedInstanceState) {
        btnNewPhotos = (Button) rootView.findViewById(R.id.btnNewPhotos);
        btnNewPhotos.setOnClickListener(buttonClickListener);


        // Init 'View' instance(s) with rootView.findViewById here
        listview = (ListView) rootView.findViewById(R.id.listview);
        listAdapter = new PhotoListAdapter(lastPositionInteger);
        listAdapter.setDao(photoListManager.getDao());
        listview.setAdapter(listAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(pullToRefreshListener);

        listview.setOnScrollListener(listViewScrollListener);

        if (savedInstanceState == null)
            refreshData();

    }

    private void refreshData(){
        if (photoListManager.getCount() == 0)
            reloadData();
        else
            reloadDataNewer();
    }

    private void reloadDataNewer() {
        int maxId = photoListManager.MaximumId();
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance()
                .getService().loadPhotoListAfter(maxId);
        call.enqueue(new PhotoListCallBack(PhotoListCallBack.MODE_RELOAD_NEWER));

    }

    private void reloadData() {
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance().getService().loadPhotoList();
        call.enqueue(new PhotoListCallBack(PhotoListCallBack.MODE_RELOAD));
    }


    boolean isLoadingMore = false; //ใช้จริงใส่ต้นไฟล์ดีกว่า

    private void loadMoreData() {
        if (isLoadingMore)
            return;
        isLoadingMore = true;
        int minId = photoListManager.MinimumId();
        Call<PhotoItemCollectionDao> call = HttpManager.getInstance()
                .getService().loadPhotoListBeforeId(minId);
        call.enqueue(new PhotoListCallBack(PhotoListCallBack.MODE_LOAD_MORE));

    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
        outState.putBundle("PhotoListManager",
                photoListManager.onSavedInstanceState());

        outState.putBundle("lastPositionInteger" ,
                lastPositionInteger.onSaveInstanceState());

    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        //Restore instance state here
        photoListManager.onRestoreInstanceState(
                savedInstanceState.getBundle("PhotoListManager"));

        lastPositionInteger.onRestoreInstanceState(
                savedInstanceState.getBundle("lastPositionInteger"));
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void showButtonNewPhotos(){
        btnNewPhotos.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation
                (Contextor.getInstance().getContext(),R.anim.zoom_fade_in);
        btnNewPhotos.startAnimation(anim);
    }
    private void hideButtonNewPhotos(){
        btnNewPhotos.setVisibility(View.GONE);
        Animation anim = AnimationUtils.loadAnimation
                (Contextor.getInstance().getContext(),R.anim.zoom_fade_out);
        btnNewPhotos.startAnimation(anim);
    }


    private void showToast(String text) {
        Toast.makeText(Contextor.getInstance().getContext(),
                text,
                Toast.LENGTH_SHORT)
                .show();
    }

    /*****************
     * Listener Zone
     *****************/

    final View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnNewPhotos) {
                listview.smoothScrollToPosition(0);
                hideButtonNewPhotos();
            }
        }
    };


    final SwipeRefreshLayout.OnRefreshListener pullToRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refreshData();
        }
    };


    final AbsListView.OnScrollListener listViewScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view,
                             int firstVisibleItem,
                             int visibleItemCount,
                             int totalItemCount) {
            if (view == listview) {
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (photoListManager.getCount() > 0) {
                        //Load More
                        loadMoreData();

                    }
                }
            }



        }
    };

    /***************
     * Inner Class
     ***************/

    class PhotoListCallBack implements Callback<PhotoItemCollectionDao>{

        public static final int MODE_RELOAD = 1;
        public static final int MODE_RELOAD_NEWER = 2;
        public static final int MODE_LOAD_MORE = 3;


        int mode;

        public PhotoListCallBack(int mode){
            this.mode = mode;
        }


        @Override
        public void onResponse(Call<PhotoItemCollectionDao> call, Response<PhotoItemCollectionDao> response) {
            swipeRefreshLayout.setRefreshing(false);
            if(response.isSuccessful()){
                PhotoItemCollectionDao dao = response.body();

                int firstVisiblePosition = listview.getFirstVisiblePosition();
                View c =listview.getChildAt(0);
                int top = c == null ? 0 : c.getTop();

                if (mode == MODE_RELOAD_NEWER) {
                    photoListManager.insertDaoAtTopPosition(dao);
                }
                else if (mode == MODE_LOAD_MORE) {
                    photoListManager.appendDaoAtBottomPosition(dao);
                    clerLoadingMoreFlagTfCapable(mode);
                }
                else {
                    photoListManager.setDao(dao);
                }
                listAdapter.setDao(photoListManager.getDao());
                listAdapter.notifyDataSetChanged();

                if (mode == MODE_RELOAD_NEWER){
                    //Maintain Scroll Position
                    int addtionalSize =
                            (dao != null && dao.getData() != null) ? dao.getData().size() : 0;
                    listAdapter.increaseLastPosition(addtionalSize);
                    listview.setSelectionFromTop(firstVisiblePosition + addtionalSize, top);
                    if (addtionalSize > 0)
                        showButtonNewPhotos();
                }else {

                }


                showToast("Load Completed");
            }else {
                //Handle
                clerLoadingMoreFlagTfCapable(mode);
                try {
                    showToast(response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call<PhotoItemCollectionDao> call, Throwable t) {
            //Handle
            clerLoadingMoreFlagTfCapable(mode);
            swipeRefreshLayout.setRefreshing(false);
            showToast(t.toString());
        }

        private void clerLoadingMoreFlagTfCapable(int mode){
            if (mode == MODE_LOAD_MORE)
                isLoadingMore = false;
        }
    }
}
