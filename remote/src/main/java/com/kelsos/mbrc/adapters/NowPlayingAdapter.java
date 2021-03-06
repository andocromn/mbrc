package com.kelsos.mbrc.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.data.MusicTrack;
import java.util.ArrayList;

public class NowPlayingAdapter extends ArrayAdapter<MusicTrack> {
  private Context mContext;
  private int mResource;
  private ArrayList<MusicTrack> nowPlayingList;
  private int playingTrackIndex;
  private Typeface robotoRegular;

  public NowPlayingAdapter(Context context, int resource, ArrayList<MusicTrack> objects) {
    super(context, resource, objects);
    this.mResource = resource;
    this.mContext = context;
    this.nowPlayingList = objects;
    robotoRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/roboto_regular.ttf");
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    TrackHolder holder;

    if (row == null) {
      LayoutInflater layoutInflater = ((Activity) mContext).getLayoutInflater();
      row = layoutInflater.inflate(mResource, parent, false);

      holder = new TrackHolder();
      holder.title = (TextView) row.findViewById(R.id.trackTitle);
      holder.artist = (TextView) row.findViewById(R.id.trackArtist);
      holder.trackPlaying = (ImageView) row.findViewById(R.id.track_indicator_view);
      holder.title.setTypeface(robotoRegular);
      holder.artist.setTypeface(robotoRegular);

      row.setTag(holder);
    } else {
      holder = (TrackHolder) row.getTag();
    }

    MusicTrack track = nowPlayingList.get(position);
    holder.title.setText(track.getTitle());
    holder.artist.setText(track.getArtist());
    if (position == playingTrackIndex) {
      holder.trackPlaying.setImageResource(R.drawable.ic_media_now_playing);
    } else {
      holder.trackPlaying.setImageResource(android.R.color.transparent);
    }

    return row;
  }

  public int getPlayingTrackIndex() {
    return this.playingTrackIndex;
  }

  public void setPlayingTrackIndex(int index) {
    this.playingTrackIndex = index;
  }

  static class TrackHolder {
    TextView title;
    TextView artist;
    ImageView trackPlaying;
  }
}
