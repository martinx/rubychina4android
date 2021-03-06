package org.rubychina.app.ui.fragment.topic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import org.rubychina.app.R;
import org.rubychina.app.model.Topic;
import org.rubychina.app.ui.MainActivity;
import org.rubychina.app.ui.adapter.ItemAnimationAdapter;
import org.rubychina.app.ui.adapter.TopicAdapter;
import org.rubychina.app.utils.ApiParams;
import org.rubychina.app.utils.ApiUtils;
import org.rubychina.app.utils.UserUtils;
import org.rubychina.app.view.LoadingFooter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.loopj.android.http.AsyncHttpResponseHandler;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by mac on 14-1-28.
 */
public class TopicsFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener{

    private PullToRefreshAttacher mPullToRefreshAttacher;

    private ListView mListView;

    private LoadingFooter mLoadingFooter;

    private TopicAdapter mAdapter;

    int page = 1;

    Gson gson = new Gson();
    Type listType = new TypeToken<List<Topic>>(){}.getType();
    List<Topic> topics = new ArrayList<Topic>();

    public TopicsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hot, container, false);
        mListView = (ListView)rootView.findViewById(R.id.listView);

        mPullToRefreshAttacher = ((MainActivity)getActivity()).getPullToRefreshAttacher();

        mPullToRefreshAttacher.setRefreshableView(mListView, this);
        mLoadingFooter = new LoadingFooter(getActivity());
        mListView.addFooterView(mLoadingFooter.getView());

        mAdapter = new TopicAdapter(topics, getActivity());
        AnimationAdapter animationAdapter = new ItemAnimationAdapter(mAdapter);
        animationAdapter.setListView(mListView);
        mListView.setAdapter(animationAdapter);
        bindListView();

        loadCacheData();
        return rootView;
    }

    private void bindListView(){
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (mLoadingFooter.getState() == LoadingFooter.State.Loading
                        || mLoadingFooter.getState() == LoadingFooter.State.TheEnd) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount
                        && totalItemCount != 0
                        && totalItemCount != mListView.getHeaderViewsCount()
                        + mListView.getFooterViewsCount() && mAdapter.getCount() > 0) {
                    loadNextPage();
                }
            }
        });
    }

    private void loadNextPage(){
        mLoadingFooter.setState(LoadingFooter.State.Loading);
        page++;
        loadData(page);
    }

    private void loadFirstPage(){
        page = 1;
        loadData(1);
    }

    private void loadCacheData(){
        String cache = UserUtils.loadTopic();
        if (cache.length() > 0){
            List<Topic> ts = gson.fromJson(UserUtils.loadTopic(), listType);
            for (Topic t : ts){
                topics.add(t);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            loadFirstPage();
        }
    }

    private void loadData(final int page) {
        ApiUtils.get(ApiUtils.TOPICS, new ApiParams().with("page", page + "").with("per_page","10"), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
                List<Topic> ts = gson.fromJson(response, listType);
                if (page == 1){
                    topics.clear();
                    mPullToRefreshAttacher.setRefreshComplete();
                    UserUtils.cacheTopic(response);
                }
                for (Topic t : ts){
                    topics.add(t);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRefreshStarted(View view) {
        loadFirstPage();
    }
}
