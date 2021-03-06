package com.kelsos.mbrc.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.inject.Inject;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.adapters.AlbumEntryAdapter;
import com.kelsos.mbrc.constants.Const;
import com.kelsos.mbrc.constants.Protocol;
import com.kelsos.mbrc.constants.ProtocolEventType;
import com.kelsos.mbrc.data.AlbumEntry;
import com.kelsos.mbrc.data.Queue;
import com.kelsos.mbrc.data.UserAction;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.events.general.SearchDefaultAction;
import com.kelsos.mbrc.events.ui.AlbumSearchResults;
import com.kelsos.mbrc.utilities.ScrollListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.fragment.RoboFragment;

public class SearchAlbumFragment extends RoboFragment implements AlbumEntryAdapter.MenuItemSelectedListener {
  @Inject Bus bus;
  @Bind(R.id.search_recycler_view) RecyclerView recycler;
  @Bind(R.id.empty_view) LinearLayout emptyView;
  @Inject private ScrollListener scrollListener;
  private String mDefault;
  @Inject private AlbumEntryAdapter adapter;

  @Subscribe public void handleSearchDefaultAction(SearchDefaultAction action) {
    mDefault = action.getAction();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.ui_fragment_library_search, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    recycler.addOnScrollListener(scrollListener);
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
    recycler.removeOnScrollListener(scrollListener);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recycler.setHasFixedSize(true);
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
    recycler.setLayoutManager(mLayoutManager);
    adapter.setMenuItemSelectedListener(this);
    recycler.setAdapter(adapter);
    displayProperView(true);
  }

  @Subscribe public void handleAlbumResults(AlbumSearchResults results) {
    boolean noData = results.getList().isEmpty();
    displayProperView(noData);
    adapter.update(results.getList());
  }

  public void displayProperView(boolean noData) {
    if (noData) {
      emptyView.setVisibility(View.VISIBLE);
      recycler.setVisibility(View.GONE);
    } else {
      emptyView.setVisibility(View.GONE);
      recycler.setVisibility(View.VISIBLE);
    }
  }

  @Override public void onMenuItemSelected(MenuItem menuItem, AlbumEntry entry) {

    final String qContext = Protocol.LibraryQueueAlbum;
    final String gSub = Protocol.LibraryAlbumTracks;
    String query = entry.getAlbum();

    UserAction ua = null;
    switch (menuItem.getItemId()) {
      case R.id.popup_album_queue_next:
        ua = new UserAction(qContext, new Queue(Queue.NEXT, query));
        break;
      case R.id.popup_album_queue_last:
        ua = new UserAction(qContext, new Queue(Queue.LAST, query));
        break;
      case R.id.popup_album_play:
        ua = new UserAction(qContext, new Queue(Queue.NOW, query));
        break;
      case R.id.popup_album_tracks:
        ua = new UserAction(gSub, query);
        break;
      default:
        break;
    }

    if (ua != null) {
      bus.post(new MessageEvent(ProtocolEventType.UserAction, ua));
    }
  }

  @Override public void onItemClicked(AlbumEntry album) {
    if (!mDefault.equals(Const.SUB)) {
      bus.post(new MessageEvent(ProtocolEventType.UserAction,
          new UserAction(Protocol.LibraryQueueAlbum, new Queue(mDefault, album.getAlbum()))));
    } else {
      bus.post(new MessageEvent(ProtocolEventType.UserAction,
          new UserAction(Protocol.LibraryAlbumTracks, album.getAlbum())));
    }
  }
}
